package main.java;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import main.java.com.ning.killbill.grammar.JavaLexer;
import main.java.com.ning.killbill.grammar.JavaParser;
import main.java.com.ning.killbill.objects.KillbillListener;

public class KillbillParser {


    public static void main(String[] args) throws Exception {
        ANTLRInputStream input = new ANTLRFileStream(args[0]);
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
/*
        tree.inspect(parser); // show in gui
        System.out.println(tree.toStringTree(parser));
        */

    }
}
