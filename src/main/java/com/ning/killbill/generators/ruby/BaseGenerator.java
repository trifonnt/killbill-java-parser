package com.ning.killbill.generators.ruby;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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

                for (ClassEnumOrInterface cur : allClasses) {
                    generateClass(cur, args.getOutputDir());
                }
            }
        } catch (IOException e) {
            throw new GeneratorException("Failed to generate code: ", e);
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
