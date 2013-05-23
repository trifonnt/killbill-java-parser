package com.ning.killbill;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.testng.annotations.BeforeClass;

import com.ning.killbill.objects.Method;

import com.google.common.io.Resources;


public abstract class TestBase {

    protected KillbillListener listener;

    @BeforeClass(groups = "fast")
    public void setup() throws IOException, URISyntaxException {
        ANTLRInputStream input = new ANTLRFileStream(getResourceFileName());
        JavaLexer lexer = new JavaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);
        parser.setBuildParseTree(true);
        RuleContext tree = parser.compilationUnit();

        ParseTreeWalker walker = new ParseTreeWalker();
        listener = new KillbillListener();
        walker.walk(listener, tree);
    }

    protected Method getMethod(final String name, List<Method> methods) {
        for (Method cur : methods) {
            if (cur.getName().equals(name)) {
                return cur;
            }
        }
        return null;
    }

    protected boolean isSuperInterfaceDefined(String ifceName, List<String> ifces) {
        for (String cur : ifces) {
            if (cur.equals(ifceName)) {
                return true;
            }
        }
        return false;
    }

    public String getResourceFileName() throws IOException, URISyntaxException {
        URL resource = Resources.getResource(getResourceName());
        File resourceFile = new File(resource.toURI());
        return resourceFile.getAbsolutePath();
    }

    public abstract String getResourceName();
}
