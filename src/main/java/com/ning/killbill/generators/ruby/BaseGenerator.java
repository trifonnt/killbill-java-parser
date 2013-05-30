package com.ning.killbill.generators.ruby;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import com.ning.killbill.JavaLexer;
import com.ning.killbill.JavaParser;
import com.ning.killbill.KillbillListener;
import com.ning.killbill.com.ning.killbill.args.KillbillParserArgs;
import com.ning.killbill.generators.Generator;
import com.ning.killbill.generators.GeneratorException;
import com.ning.killbill.objects.ClassEnumOrInterface;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

public abstract class BaseGenerator implements Generator {

    final List<ClassEnumOrInterface> allClasses;

    public BaseGenerator() {
        this.allClasses = new LinkedList<ClassEnumOrInterface>();
    }

    @Override
    public void generate(final KillbillParserArgs args) throws GeneratorException {

        try {
            if (args.isInputFile()) {
                generateFromFile(args.getInputFile(), args.getOutputDir());
            } else if (args.isInputDirectory()) {
                generateFromDirectory(args.getInputFile(), args.getOutputDir(), args.getPackagesFilter());
            } else {
                throw new GeneratorException("Not yet supported scheme: " + args.getInput().getScheme());
            }


            for (ClassEnumOrInterface cur : allClasses) {
                if (!cur.isAbstract()) {
                    generateClass(cur, args.getOutputDir());
                }
            }
        } catch (IOException e) {
            throw new GeneratorException("Failed to generate code: ", e);
        }
    }

    private void generateFromDirectory(final File inputDir, final File outputDir, final List<String> packageFilters) throws IOException {
        final List<File> files = generateOutputFiles(inputDir, packageFilters);
        for (File f : files) {
            generateFromFile(f, outputDir);
        }
    }

    private void generateFromFile(final File input, final File outputDir) throws IOException {
        final KillbillListener killbillListener = parseFile(input.getAbsolutePath());
        allClasses.addAll(killbillListener.getAllClassesEnumOrInterfaces());
    }

    protected abstract void generateClass(final ClassEnumOrInterface obj, final File outputDir) throws GeneratorException;


    protected static final String camelToUnderscore(final String input) {
        return UPPER_CAMEL.to(LOWER_UNDERSCORE, input);
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
}
