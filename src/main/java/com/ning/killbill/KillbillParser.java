package com.ning.killbill;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.killbill.com.ning.killbill.args.KillbillParserArgs;
import com.ning.killbill.generators.DistpatchGenerator;

import com.beust.jcommander.JCommander;
import com.google.common.base.Joiner;


public class KillbillParser {


    public final static Logger logger = LoggerFactory.getLogger(KillbillParser.class);

    private final static String RESOURCE_DEBUG_FILE = "/Users/stephanebrossier/Work/Src/Killbill/killbill-java-parser/src/test/resources/ClassComplexAnnotations";

    private final static boolean DEBUG_MODE = false;


    public static void main(String[] args) throws Exception {

        if (DEBUG_MODE) {
            parserDebug();
            return;
        }


        final KillbillParserArgs kbParserArgs = new KillbillParserArgs();
        JCommander j = new JCommander(kbParserArgs, args);
        if (kbParserArgs.isHelp()) {
            j.usage();
            return;
        }

        logger.info("KillbillParser input = " + kbParserArgs.getInput() +
                    ", outputDir = " + kbParserArgs.getOutputDir().getAbsoluteFile() +
                    ", packagesFilter = " + Joiner.on(", ").join(kbParserArgs.getPackagesParserFilter()) +
                    ", target = " + kbParserArgs.getTargetGenerator());


        final DistpatchGenerator gen = new DistpatchGenerator();
        gen.generate(kbParserArgs);
    }


    private static void parserDebug() throws Exception {
        ANTLRInputStream input = new ANTLRFileStream(RESOURCE_DEBUG_FILE);
        JavaLexer lexer = new JavaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);
        parser.setBuildParseTree(true);
        RuleContext tree = parser.compilationUnit();

        tree.inspect(parser); // show in gui
        System.out.println(tree.toStringTree(parser));
        ParseTreeWalker walker = new ParseTreeWalker();
        KillbillListener listener = new KillbillListener();
        walker.walk(listener, tree);
        System.out.println("**** RESULT: *****");
        System.out.println(listener.toString());

    }
}
