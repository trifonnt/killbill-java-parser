package com.ning.killbill.generators;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.ning.killbill.JavaLexer;
import com.ning.killbill.JavaParser;
import com.ning.killbill.KillbillListener;
import com.ning.killbill.com.ning.killbill.args.KillbillParserArgs;
import com.ning.killbill.generators.ruby.RubyBaseGenerator;
import com.ning.killbill.objects.ClassEnumOrInterface;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class BaseGenerator {

    protected final List<ClassEnumOrInterface> allClasses;
    protected Logger log = LoggerFactory.getLogger(RubyBaseGenerator.class);

    public BaseGenerator() {
        this.allClasses = new LinkedList<ClassEnumOrInterface>();
    }

    protected void parseAll(KillbillParserArgs args, List<URI> input) throws IOException, GeneratorException {
        for (final URI cur : input) {
            if (args.isInputFile(cur)) {
                generateFromFile(args.getInputFile(cur), args.getOutputDir());
            } else if (args.isInputDirectory(cur)) {
                generateFromDirectory(args.getInputFile(cur), args.getOutputDir(), args.getPackagesParserFilter());
            } else {
                throw new GeneratorException("Not yet supported scheme: " + cur.getScheme());
            }
        }
    }

    private void generateFromFile(final File input, final File outputDir) throws IOException {
        log.info("********************************   Parsing file " + input.getAbsoluteFile());

        final KillbillListener killbillListener = parseFile(input.getAbsolutePath());
        allClasses.addAll(killbillListener.getAllClassesEnumOrInterfaces());
    }


    private void generateFromDirectory(final File inputDir, final File outputDir, final List<String> packageFilters) throws IOException {
        final List<File> files = generateOutputFiles(inputDir, packageFilters);
        for (File f : files) {
            generateFromFile(f, outputDir);
        }
    }

    private KillbillListener parseFile(final String fileName) throws IOException {
        ANTLRInputStream input = new ANTLRFileStream(fileName);
        JavaLexer lexer = new JavaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);
        parser.setBuildParseTree(true);
        RuleContext tree = parser.compilationUnit();
        ParseTreeWalker walker = new ParseTreeWalker();
        KillbillListener listener = new KillbillListener();
        walker.walk(listener, tree);
        return listener;
    }

    private static final List<File> generateOutputFiles(final File inputDir, final List<String> packageFilter) {
        final Collection<String> pathFilters = Collections2.transform(packageFilter, new Function<String, String>() {
            @Override
            public String apply(final String input) {
                return input.replaceAll("\\.", File.separator);
            }
        });

        final FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                if (packageFilter == null || packageFilter.size() == 0) {
                    return true;
                }
                for (String cur : pathFilters) {
                    if (dir.getAbsolutePath().endsWith(cur)) {
                        return true;
                    }
                }
                return false;
            }
        };
        final List<File> output = new ArrayList<File>();
        generateRecursiveOutputFiles(inputDir, output, filter);
        return output;
    }


    private static void generateRecursiveOutputFiles(final File path, final List<File> output, final FilenameFilter filter) {

        if (path == null) {
            return;
        }
        if (path.exists()) {
            final File[] files = path.listFiles();
            if (files != null) {
                for (final File f : files) {
                    if (f.isDirectory()) {
                        generateRecursiveOutputFiles(f, output, filter);
                    } else {
                        if (filter.accept(f.getParentFile(), f.getName())) {
                            output.add(f);
                        }
                    }
                }
            }
        }
    }

    protected boolean isClassExcluded(final String className, final List<String> excludeClasses) {
        return excludeClasses.contains(className);
    }
}
