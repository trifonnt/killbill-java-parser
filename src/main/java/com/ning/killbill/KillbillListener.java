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
import com.ning.killbill.JavaParser.EnumConstantContext;
import com.ning.killbill.JavaParser.FormalParameterDeclsRestContext;
import com.ning.killbill.JavaParser.FormalParametersContext;
import com.ning.killbill.JavaParser.ImportDeclarationContext;
import com.ning.killbill.JavaParser.InterfaceDeclarationContext;
import com.ning.killbill.JavaParser.InterfaceMethodOrFieldDeclContext;
import com.ning.killbill.JavaParser.ModifierContext;
import com.ning.killbill.JavaParser.PackageDeclarationContext;
import com.ning.killbill.JavaParser.TypeContext;
import com.ning.killbill.objects.Argument;
import com.ning.killbill.objects.ClassEnumOrInterface;
import com.ning.killbill.objects.ClassEnumOrInterface.ClassEnumOrInterfaceType;
import com.ning.killbill.objects.Method;

import com.google.common.base.Joiner;

public class KillbillListener extends JavaBaseListener {

    public static final String JAVA_LANG = "java.lang.";
    Logger log = LoggerFactory.getLogger(KillbillListener.class);

    private final Deque<ClassEnumOrInterface> currentClassesEnumOrInterfaces;
    private Method currentMethod;
    private List<String> currentModifiers;


    private final Map<String, String> allImports;


    private String packageName;
    private final List<ClassEnumOrInterface> allClassesEnumOrInterfaces;


    public KillbillListener() {
        this.allClassesEnumOrInterfaces = new ArrayList<ClassEnumOrInterface>();
        this.allImports = new HashMap<String, String>();
        this.currentClassesEnumOrInterfaces = new ArrayDeque<ClassEnumOrInterface>();
        this.currentMethod = null;
        this.currentModifiers = null;
        this.packageName = null;

    }

    public List<ClassEnumOrInterface> getAllClassesEnumOrInterfaces() {
        return allClassesEnumOrInterfaces;
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

        final ClassEnumOrInterface classEnumOrInterface = new ClassEnumOrInterface(ctx.normalInterfaceDeclaration().Identifier().getText(), ClassEnumOrInterfaceType.INTERFACE);
        currentClassesEnumOrInterfaces.push(classEnumOrInterface);
        if (ctx.normalInterfaceDeclaration().typeList() != null) {
            for (TypeContext cur : ctx.normalInterfaceDeclaration().typeList().type()) {
                classEnumOrInterface.addSuperInterface(getFullyQualifiedType(cur.getText()));
            }
        }
    }

    @Override
    public void exitInterfaceDeclaration(InterfaceDeclarationContext ctx) {

        final ClassEnumOrInterface ifce = currentClassesEnumOrInterfaces.pop();
        allClassesEnumOrInterfaces.add(ifce);
        log.debug("** Exiting enterInterfaceDeclaration " + ctx.getText());

    }


    @Override
    public void enterClassDeclaration(ClassDeclarationContext ctx) {
        log.debug("** Entering enterClassDeclaration " + ctx.getText());
        if (ctx.normalClassDeclaration() != null) {
            final ClassEnumOrInterface classEnumOrInterface = new ClassEnumOrInterface(ctx.normalClassDeclaration().Identifier().getText(), ClassEnumOrInterfaceType.CLASS);
            currentClassesEnumOrInterfaces.push(classEnumOrInterface);
            final TypeContext superClass = ctx.normalClassDeclaration().type();
            if (superClass != null) {
                classEnumOrInterface.addSuperClass(getFullyQualifiedType(superClass.getText()));
            }
            if (ctx.normalClassDeclaration().typeList() != null) {
                for (TypeContext cur : ctx.normalClassDeclaration().typeList().type()) {
                    classEnumOrInterface.addSuperInterface(getFullyQualifiedType(cur.getText()));
                }
            }
        }
    }

    @Override
    public void enterEnumDeclaration(JavaParser.EnumDeclarationContext ctx) {
        log.debug("** Entering enterEnumDeclaration " + ctx.getText());

        final ClassEnumOrInterface classEnumOrInterface = new ClassEnumOrInterface(ctx.Identifier().getText(), ClassEnumOrInterfaceType.ENUM);
        currentClassesEnumOrInterfaces.push(classEnumOrInterface);
        final List<EnumConstantContext> enumValues = ctx.enumBody().enumConstants().enumConstant();
        for (EnumConstantContext cur : enumValues) {
            classEnumOrInterface.addEnumValue(cur.getText());
        }
    }

    @Override
    public void exitEnumDeclaration(JavaParser.EnumDeclarationContext ctx) {
        final ClassEnumOrInterface claz = currentClassesEnumOrInterfaces.pop();
        allClassesEnumOrInterfaces.add(claz);
    }


    @Override
    public void exitClassDeclaration(ClassDeclarationContext ctx) {
        if (ctx.normalClassDeclaration() != null) {
            final ClassEnumOrInterface claz = currentClassesEnumOrInterfaces.pop();
            allClassesEnumOrInterfaces.add(claz);
        }
    }


    @Override
    public void enterInterfaceMethodOrFieldDecl(InterfaceMethodOrFieldDeclContext ctx) {
        log.debug("** Entering enterInterfaceMethodOrFieldDecl" + ctx.getText());
        currentMethod = new Method(ctx.Identifier().getText());
    }

    @Override
    public void exitInterfaceMethodOrFieldDecl(InterfaceMethodOrFieldDeclContext ctx) {
        currentClassesEnumOrInterfaces.peekFirst().addMethod(currentMethod);
        currentMethod = null;
        log.debug("** Exiting exitInterfaceMethodOrFieldDecl" + ctx.getText());
    }


    @Override
    public void enterClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
        log.debug("** Entering enterClassBodyDeclaration" + ctx.getText());
        if (ctx.modifiers() != null && ctx.modifiers().modifier().size() > 0) {
            currentModifiers = new ArrayList<String>();
            for (ModifierContext cur : ctx.modifiers().modifier()) {
                currentModifiers.add(cur.getText());
            }
        }
    }

    @Override
    public void exitClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
        log.debug("** Exiting exitClassBodyDeclaration" + ctx.getText());
        currentModifiers = null;
    }


    @Override
    public void enterMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
        log.debug("** Entering enterMethodDeclaration" + ctx.getText());
        if (!isIncludedInModifier("private", "protected")) {
            currentMethod = new Method(ctx.Identifier().getText());
        }
    }

    @Override
    public void exitMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
        if (currentMethod != null) {
            currentClassesEnumOrInterfaces.peekFirst().addMethod(currentMethod);
            currentMethod = null;
        }
        log.debug("** Exiting exitMethodDeclaration" + ctx.getText());
    }


    @Override
    public void enterFormalParameters(FormalParametersContext ctx) {

        if (currentMethod == null) {
            // For instance CTOR
            log.debug("enterFormalParameters : no curentMethod ");
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

    @Override
    public String toString() {

        final StringBuilder tmp = new StringBuilder();
        tmp.append("******* PACKAGE " + packageName + " ***************");

        tmp.append("\n******* IMPORTS **********");
        for (String cur : allImports.keySet()) {
            tmp.append(cur + " -> " + allImports.get(cur) + "\n");
        }


        for (ClassEnumOrInterface cur : allClassesEnumOrInterfaces) {
            tmp.append("\n******* INTERFACES/CLASSES **********");
            tmp.append(cur);
        }
        return tmp.toString();
    }

    private boolean isIncludedInModifier(final String... modifiers) {
        if (currentModifiers == null) {
            return false;
        }
        for (String cur : currentModifiers) {
            for (String m : modifiers)
                if (cur.equals(m)) {
                    return true;
                }
        }
        return false;
    }

    private String getFullyQualifiedType(final String type) {

        // Is already fully qualified?
        String[] parts = type.split("\\.");
        if (parts.length > 1) {
            return type;
        }

        // Is that in the import List?
        if (allImports.get(type) != null) {
            return allImports.get(type);
        }

        // If not, is that a java.lang?
        try {

            final String className = JAVA_LANG + type;
            Class.forName(className);
            return className;
        } catch (ClassNotFoundException ignore) {
        }

        // Finally if we assume that file compiles, then add the current package
        return packageName + "." + type;
    }
}
