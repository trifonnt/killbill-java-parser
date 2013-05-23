package com.ning.killbill;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.killbill.JavaParser.ClassDeclarationContext;
import com.ning.killbill.JavaParser.FormalParameterDeclsRestContext;
import com.ning.killbill.JavaParser.FormalParametersContext;
import com.ning.killbill.JavaParser.ImportDeclarationContext;
import com.ning.killbill.JavaParser.InterfaceBodyDeclarationContext;
import com.ning.killbill.JavaParser.InterfaceDeclarationContext;
import com.ning.killbill.JavaParser.InterfaceMethodOrFieldDeclContext;
import com.ning.killbill.JavaParser.PackageDeclarationContext;
import com.ning.killbill.JavaParser.TypeContext;
import com.ning.killbill.objects.Argument;
import com.ning.killbill.objects.ClassOrInterface;
import com.ning.killbill.objects.Method;

import com.google.common.base.Joiner;

public class KillbillListener extends JavaBaseListener {

    Logger log = LoggerFactory.getLogger(KillbillListener.class);

    private final Deque<ClassOrInterface> currentClassesOrInterfaces;
    private Method currentMethod;


    private final Map<String, String> allImports;


    private String packageName;
    private final List<ClassOrInterface> allClassesOrInterfaces;


    public KillbillListener() {
        this.allClassesOrInterfaces = new ArrayList<ClassOrInterface>();
        this.allImports = new HashMap<String, String>();
        this.currentClassesOrInterfaces = new ArrayDeque<ClassOrInterface>();
        this.currentMethod = null;
        packageName = null;
    }

    public List<ClassOrInterface> getAllClassesOrInterfaces() {
        return allClassesOrInterfaces;
    }

    public String getPackageName() {
        return packageName;
    }


    @Override
    public void enterImportDeclaration(ImportDeclarationContext ctx) {
        log.info("** Entering enterImportDeclaration " + ctx.getText());
        final List<TerminalNode> identifiers = ctx.qualifiedName().Identifier();
        identifiers.get(identifiers.size() - 1).getText();
        allImports.put(identifiers.get(identifiers.size() - 1).getText(), Joiner.on(".").skipNulls().join(identifiers));
    }


    @Override
    public void enterPackageDeclaration(PackageDeclarationContext ctx) {
        this.packageName = ctx.qualifiedName().getText();
    }


    @Override
    public void enterInterfaceDeclaration(InterfaceDeclarationContext ctx) {
        log.debug("** Entering enterInterfaceDeclaration " + ctx.getText());

        final ClassOrInterface classOrInterface = new ClassOrInterface(ctx.normalInterfaceDeclaration().Identifier().getText(), true);
        currentClassesOrInterfaces.push(classOrInterface);
        for (TypeContext cur : ctx.normalInterfaceDeclaration().typeList().type()) {
            classOrInterface.addSuperInterface(getFullyQualifiedType(cur.getText()));
        }
    }

    @Override
    public void exitInterfaceDeclaration(InterfaceDeclarationContext ctx) {

        final ClassOrInterface ifce = currentClassesOrInterfaces.pop();
        allClassesOrInterfaces.add(ifce);
        log.debug("** Exiting enterInterfaceDeclaration " + ctx.getText());

    }


    @Override
    public void enterClassDeclaration(ClassDeclarationContext ctx) {
        log.debug("** Entering enterClassDeclaration " + ctx.getText());
        currentClassesOrInterfaces.push(new ClassOrInterface(ctx.getText(), false));

    }

    @Override
    public void enterInterfaceBodyDeclaration(InterfaceBodyDeclarationContext ctx) {

    }

    @Override
    public void exitInterfaceBodyDeclaration(InterfaceBodyDeclarationContext ctx) {

    }


    @Override
    public void exitClassDeclaration(ClassDeclarationContext ctx) {
        final ClassOrInterface claz = currentClassesOrInterfaces.pop();
        allClassesOrInterfaces.add(claz);
    }


    @Override
    public void enterInterfaceMethodOrFieldDecl(InterfaceMethodOrFieldDeclContext ctx) {
        log.debug("** Entering enterInterfaceMethodOrFieldDecl" + ctx.getText());
        currentMethod = new Method(ctx.Identifier().getText());
    }

    @Override
    public void exitInterfaceMethodOrFieldDecl(InterfaceMethodOrFieldDeclContext ctx) {
        currentClassesOrInterfaces.peekFirst().addMethod(currentMethod);
        currentMethod = null;
    }

    @Override
    public void enterFormalParameters(FormalParametersContext ctx) {

        if (currentMethod == null) {
            log.warn("enterFormalParameters : no curentMethod ");
            return;
        }

        if (ctx.formalParameterDecls() != null) {
            final TypeContext typeContext = ctx.formalParameterDecls().type();

            if (typeContext.classOrInterfaceType().Identifier().size() > 1) {
                log.warn("enterFormalParameters : Found " + typeContext.classOrInterfaceType().Identifier().size() + " classOrInterfaceType for argument");
            }

            final String parameterType = typeContext.classOrInterfaceType().Identifier().get(0).getText();
            final FormalParameterDeclsRestContext formalParameterDeclsRestContext = ctx.formalParameterDecls().formalParameterDeclsRest();
            final String parameterVariableName = formalParameterDeclsRestContext.variableDeclaratorId().Identifier().getText();

            log.debug("enterFormalParameters : parameter " + parameterType + ":" + parameterVariableName);

            currentMethod.addArgument(new Argument(parameterVariableName, getFullyQualifiedType(parameterType)));
        }
    }



    /*

    @Override
    public void enterType(JavaParser.TypeContext ctx) {

        final String type = ctx.classOrInterfaceType().getText();

        // We are in the context of interface declaration
        if (currentMethod == null) {
            final ClassOrInterface current = currentClassesOrInterfaces.peekFirst();
            if (current.isInterface()) {
                current.addSuperInterface(fullyQualifiedType);
            }
        }
    }
    */

    @Override
    public void exitType(TypeContext ctx) {
    }


    @Override
    public String toString() {

        final StringBuilder tmp = new StringBuilder();
        tmp.append("******* PACKAGE " + packageName + " ***************");

        tmp.append("\n******* IMPORTS **********");
        for (String cur : allImports.keySet()) {
            tmp.append(cur + " -> " + allImports.get(cur) + "\n");
        }


        for (ClassOrInterface cur : allClassesOrInterfaces) {
            tmp.append("\n******* INTERFACES/CLASSES **********");
            tmp.append(cur);
        }
        return tmp.toString();
    }

    private String getFullyQualifiedType(final String type) {
        return allImports.get(type) != null ? allImports.get(type) : packageName + "." + type;
    }


}
