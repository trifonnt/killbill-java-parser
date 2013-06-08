package com.ning.killbill.generators.ruby;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.jruby.javasupport.JavaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.killbill.JavaLexer;
import com.ning.killbill.JavaParser;
import com.ning.killbill.KillbillListener;
import com.ning.killbill.com.ning.killbill.args.KillbillParserArgs;
import com.ning.killbill.generators.Generator;
import com.ning.killbill.generators.GeneratorException;
import com.ning.killbill.objects.ClassEnumOrInterface;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.io.Resources;

public abstract class BaseGenerator implements Generator {

    protected Logger log = LoggerFactory.getLogger(BaseGenerator.class);

    final List<ClassEnumOrInterface> allClasses;
    protected int curIndent = 0;

    public BaseGenerator() {
        this.allClasses = new LinkedList<ClassEnumOrInterface>();
        resetIndentation();
    }

    protected void resetIndentation() {
        curIndent = 0;
    }

    @Override
    public void generate(final KillbillParserArgs args) throws GeneratorException {

        final List<ClassEnumOrInterface> allGeneratedClasses = new ArrayList<ClassEnumOrInterface>();

        startGeneration(allClasses, args.getOutputDir());
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
                    log.info("Generating file for object " + cur.getName());
                    generateClass(cur, allClasses, args.getOutputDir());
                    allGeneratedClasses.add(cur);
                }
            }
        } catch (IOException e) {
            throw new GeneratorException("Failed to generate code: ", e);
        }
        completeGeneration(allGeneratedClasses, args.getOutputDir());
    }

    private void generateFromDirectory(final File inputDir, final File outputDir, final List<String> packageFilters) throws IOException {
        final List<File> files = generateOutputFiles(inputDir, packageFilters);
        for (File f : files) {
            generateFromFile(f, outputDir);
        }
    }

    private void generateFromFile(final File input, final File outputDir) throws IOException {
        log.info("********************************   Parsing file " + input.getAbsoluteFile());

        final KillbillListener killbillListener = parseFile(input.getAbsolutePath());
        allClasses.addAll(killbillListener.getAllClassesEnumOrInterfaces());
    }

    protected void writeAppend(final String str, final Writer w) throws IOException {
        w.write(str);
    }

    protected void writeWithIndentation(final String str, final Writer w, int curIndentOffest) throws IOException {
        writeWithIndentationInternal(str, w, curIndentOffest, false);
    }

    protected void writeNewLine(final Writer w) throws IOException {
        w.write("\n");
    }

    protected void writeWithIndentationAndNewLine(final String str, final Writer w, int curIndentOffest) throws IOException {
        writeWithIndentationInternal(str, w, curIndentOffest, true);
    }

    private void writeWithIndentationInternal(final String str, final Writer w, final int curIndentOffest, boolean newLine) throws IOException {
        curIndent += curIndentOffest;
        for (int i = 0; i < curIndent; i++) {
            w.write(" ");
        }
        w.write(str);
        if (newLine) {
            w.write("\n");
        }
    }


    protected void writeLicense(final File output) throws GeneratorException {
        try {

            final URL licenseUrl = Resources.getResource(getLicense());

            final OutputStream out = new FileOutputStream(output);
            Resources.copy(licenseUrl, out);

            /*
            System.out.println("**************************  LICENSE URL " +  licenseUrl + "**************************** " + licenseUrl.getFile());
            final File license = new File(licenseUrl.getFile());
            Files.copy(license, output);
            */
        } catch (IllegalArgumentException e) {
            throw new GeneratorException("Cannot find license file " + getLicense(), e);
        } catch (IOException e) {
            throw new GeneratorException("Failed to write license file " + getLicense(), e);
        }
    }

    protected void writeHeader(final Writer w) throws IOException {
        w.write("\n");
        w.write("\n");
        w.write("#\n");
        w.write("#                       DO NOT EDIT!!!\n");
        w.write("#    File automatically generated by killbill-java-parser (git@github.com:killbill/killbill-java-parser.git)\n");
        w.write("#\n");
        w.write("\n");
        w.write("\n");
    }


    protected static final String camelToUnderscore(final String input) {
        //return UPPER_CAMEL.to(LOWER_UNDERSCORE, input);
        return JavaUtil.getRubyCasedName(input);
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

    protected abstract void startGeneration(final List<ClassEnumOrInterface> classes, final File outputDir) throws GeneratorException;

    protected abstract void generateClass(final ClassEnumOrInterface obj, final List<ClassEnumOrInterface> allClasses, final File outputDir) throws GeneratorException;

    protected abstract void completeGeneration(final List<ClassEnumOrInterface> classes, final File outputDir) throws GeneratorException;

    protected abstract String getLicense();

}
