package com.ning.killbill;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.killbill.com.ning.killbill.args.KillbillParserArgs;
import com.ning.killbill.generators.LanguageGenerator;

import com.beust.jcommander.JCommander;
import com.google.common.base.Joiner;


public class KillbillParser {


    public final static Logger logger = LoggerFactory.getLogger(KillbillParser.class);

    public static void main(String[] args) throws Exception {

        //parserDebug();
        final KillbillParserArgs kbParserArgs = parseArguments(args);

        logger.info("KillbillParser input = " + kbParserArgs.getInput() +
                    ", outputDir = " + kbParserArgs.getOutputDir().getAbsoluteFile() +
                    ", packagesFilter = " + Joiner.on(", ").join(kbParserArgs.getPackagesFilter()) +
                    ", language = " + kbParserArgs.getLanguage());


        final LanguageGenerator gen = new LanguageGenerator();
        gen.generate(kbParserArgs);
    }


    private static final KillbillParserArgs parseArguments(final String[] args) {
        final KillbillParserArgs result = new KillbillParserArgs();
        new JCommander(result, args);
        return result;
    }


    private static void parserDebug() throws Exception {
        ANTLRInputStream input = new ANTLRFileStream("/Users/stephanebrossier/Work/OpenSource/killbill/killbill-java-parser/src/test/resources/ClassWithAnnotation");
        JavaLexer lexer = new JavaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);
        parser.setBuildParseTree(true);
        RuleContext tree = parser.compilationUnit();
        ParseTreeWalker walker = new ParseTreeWalker();
        KillbillListener listener = new KillbillListener();
        walker.walk(listener, tree);
        System.out.println("**** RESULT: *****");
        System.out.println(listener.toString());

        tree.inspect(parser); // show in gui
        System.out.println(tree.toStringTree(parser));

    }
}
