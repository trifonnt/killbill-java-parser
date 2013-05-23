package com.ning.killbill;

import java.io.IOException;
import java.net.URISyntaxException;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.testng.annotations.BeforeClass;

import com.ning.killbill.objects.KillbillListener;


public abstract class TestBase {

    @BeforeClass(groups = "fast")
    public void setup() throws IOException, URISyntaxException {
        ANTLRInputStream input = new ANTLRFileStream(getResourceFileName());
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
    }

    public abstract String getResourceFileName() throws IOException, URISyntaxException;
}
