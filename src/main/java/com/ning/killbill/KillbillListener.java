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

import com.ning.killbill.JavaParser.AnnotationContext;
import com.ning.killbill.JavaParser.ClassDeclarationContext;
import com.ning.killbill.JavaParser.EnumConstantContext;
import com.ning.killbill.JavaParser.FormalParameterDeclsRestContext;
import com.ning.killbill.JavaParser.ImportDeclarationContext;
import com.ning.killbill.JavaParser.InterfaceDeclarationContext;
import com.ning.killbill.JavaParser.InterfaceMethodOrFieldDeclContext;
import com.ning.killbill.JavaParser.MemberDeclContext;
import com.ning.killbill.JavaParser.MemberDeclarationContext;
import com.ning.killbill.JavaParser.ModifierContext;
import com.ning.killbill.JavaParser.PackageDeclarationContext;
import com.ning.killbill.JavaParser.TypeContext;
import com.ning.killbill.JavaParser.VariableDeclaratorContext;
import com.ning.killbill.JavaParser.VariableDeclaratorsContext;
import com.ning.killbill.objects.Annotation;
import com.ning.killbill.objects.Field;
import com.ning.killbill.objects.ClassEnumOrInterface;
import com.ning.killbill.objects.ClassEnumOrInterface.ClassEnumOrInterfaceType;
import com.ning.killbill.objects.Constructor;
import com.ning.killbill.objects.Method;
import com.ning.killbill.objects.MethodOrCtor;

import com.google.common.base.Joiner;

public class KillbillListener extends JavaBaseListener {

    public static final String JAVA_LANG = "java.lang.";

    Logger log = LoggerFactory.getLogger(KillbillListener.class);

    /*
     *  Tracks current state as we walk through the tree.
     */
    private final Deque<ClassEnumOrInterface> currentClassesEnumOrInterfaces;
    private MethodOrCtor currentMethodOrCtor;
    private List<String> currentMethodOrCtorModifiers;
    private List<Annotation> currentNonParameterAnnotations;
    private List<Annotation> currentParametersAnnotations;
    private int curInMethodBodyLevel;


    /**
     * Import statements.
     */
    private final Map<String, String> allImports;

    /**
     * Package Name
     */
    private String packageName;

    /**
     * Root for all classes/interfaces/enum per java file.
     */
    private final List<ClassEnumOrInterface> allClassesEnumOrInterfaces;


    public KillbillListener() {
        this.allClassesEnumOrInterfaces = new ArrayList<ClassEnumOrInterface>();
        this.allImports = new HashMap<String, String>();
        this.currentClassesEnumOrInterfaces = new ArrayDeque<ClassEnumOrInterface>();
        this.currentMethodOrCtor = null;
        this.currentMethodOrCtorModifiers = null;
        this.currentParametersAnnotations = null;
        this.currentNonParameterAnnotations = null;
        this.packageName = null;
        this.curInMethodBodyLevel = 0;

    }

    public List<ClassEnumOrInterface> getAllClassesEnumOrInterfaces() {
        return allClassesEnumOrInterfaces;
    }

    public String getPackageName() {
        return packageName;
    }

    /*
    *
    * *************************************************  IMPORTS *********************************************
    *
    */

    @Override
    public void enterImportDeclaration(ImportDeclarationContext ctx) {
        log.info("** Entering enterImportDeclaration " + ctx.getText());
        final List<TerminalNode> identifiers = ctx.qualifiedName().Identifier();
        identifiers.get(identifiers.size() - 1).getText();
        allImports.put(identifiers.get(identifiers.size() - 1).getText(), Joiner.on(".").skipNulls().join(identifiers));
    }

    /*
    *
    * *************************************************  IMPORTS *********************************************
    *
    */
    @Override
    public void enterPackageDeclaration(PackageDeclarationContext ctx) {
        this.packageName = ctx.qualifiedName().getText();
    }


    /*
     *
     * *************************************************  INTERFACE *********************************************
     *
     */
    @Override
    public void enterInterfaceDeclaration(InterfaceDeclarationContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }

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
        if (curInMethodBodyLevel > 0) {
            return;
        }


        final ClassEnumOrInterface ifce = currentClassesEnumOrInterfaces.pop();
        allClassesEnumOrInterfaces.add(ifce);
        log.debug("** Exiting enterInterfaceDeclaration " + ctx.getText());

    }

   /*
    *
    * *************************************************  CLASS *********************************************
    *
    */
    @Override
    public void enterClassDeclaration(ClassDeclarationContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }

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
    public void exitClassDeclaration(ClassDeclarationContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }

        if (ctx.normalClassDeclaration() != null) {
            final ClassEnumOrInterface claz = currentClassesEnumOrInterfaces.pop();
            allClassesEnumOrInterfaces.add(claz);
        }
    }


    /*
    *
    * *************************************************  ENUM *********************************************
    *
    */
    @Override
    public void enterEnumDeclaration(JavaParser.EnumDeclarationContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }

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
        if (curInMethodBodyLevel > 0) {
            return;
        }

        final ClassEnumOrInterface claz = currentClassesEnumOrInterfaces.pop();
        allClassesEnumOrInterfaces.add(claz);
    }



    /*
    *
    * *************************************************  METHODS IFCE *********************************************
    *
    */
    @Override
    public void enterInterfaceMethodOrFieldDecl(InterfaceMethodOrFieldDeclContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }

        log.debug("** Entering enterInterfaceMethodOrFieldDecl" + ctx.getText());
        currentMethodOrCtor = new Method(ctx.Identifier().getText(), currentNonParameterAnnotations);
    }

    @Override
    public void exitInterfaceMethodOrFieldDecl(InterfaceMethodOrFieldDeclContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }

        currentClassesEnumOrInterfaces.peekFirst().addMethod((Method) currentMethodOrCtor);
        currentMethodOrCtor = null;
        log.debug("** Exiting exitInterfaceMethodOrFieldDecl" + ctx.getText());
    }

    /*
    *
    * *************************************************  CONSTRUCTOR *********************************************
    *
    */
    @Override public void enterConstructorDeclaratorRest(JavaParser.ConstructorDeclaratorRestContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }
        log.debug("** Entering enterConstructorDeclaratorRest" + ctx.getText());
        final MemberDeclContext ctor = (MemberDeclContext) ctx.getParent();
        currentMethodOrCtor = new Constructor(ctor.Identifier().getText(), currentNonParameterAnnotations);
    }

    @Override public void exitConstructorDeclaratorRest(JavaParser.ConstructorDeclaratorRestContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }

        log.debug("** Exiting exitConstructorDeclaratorRest" + ctx.getText());
        if (currentMethodOrCtor != null) {
            currentClassesEnumOrInterfaces.peekFirst().addConstructor((Constructor) currentMethodOrCtor);
            currentMethodOrCtor = null;
        }
    }



    /*
    *
    * ************************************************* MEMBER FIELDS *********************************************
    *
    */
    @Override public void enterFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }
        log.debug("** Entering enterFieldDeclaration" + ctx.getText());
        if (!isIncludedInModifier("static")) {
            final VariableDeclaratorsContext variableDeclaratorsContext = ctx.variableDeclarators();
            final MemberDeclarationContext memberDeclarationContext = (MemberDeclarationContext) ctx.getParent();
            final String type = (memberDeclarationContext.type().primitiveType() != null) ?
                                memberDeclarationContext.type().primitiveType().getText() :
                                memberDeclarationContext.type().classOrInterfaceType().Identifier().get(0).getText();
            for (VariableDeclaratorContext cur : variableDeclaratorsContext.variableDeclarator()) {
                final Field field = new Field(cur.variableDeclaratorId().getText(), getFullyQualifiedType(type), currentNonParameterAnnotations);
                currentClassesEnumOrInterfaces.peekFirst().addField(field);
            }
        }
    }

    @Override public void exitFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }
        // Nothing to do.
        log.debug("** Exiting exitFieldDeclaration" + ctx.getText());
    }


    /*
    *
    * *************************************************  METHODS CLASS *********************************************
    *
    */
    @Override
    public void enterMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }
        log.debug("** Entering enterMethodDeclaration" + ctx.getText());
        if (!isIncludedInModifier("private", "protected", "static")) {
            currentMethodOrCtor = new Method(ctx.Identifier().getText(), currentNonParameterAnnotations);
        }
    }

    @Override
    public void exitMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }
        if (currentMethodOrCtor != null) {
            currentClassesEnumOrInterfaces.peekFirst().addMethod((Method) currentMethodOrCtor);
            currentMethodOrCtor = null;
        }
        log.debug("** Exiting exitMethodDeclaration" + ctx.getText());
    }

    @Override
    public void enterVoidMethodDeclaratorRest(JavaParser.VoidMethodDeclaratorRestContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }
        log.debug("** Entering enterMethodDeclaration" + ctx.getText());

        if (!isIncludedInModifier("private", "protected", "static")) {
            MemberDeclContext meberDecl = (MemberDeclContext) ctx.getParent();
            currentMethodOrCtor = new Method(meberDecl.Identifier().getText(), currentNonParameterAnnotations);
        }

    }

    @Override
    public void exitVoidMethodDeclaratorRest(JavaParser.VoidMethodDeclaratorRestContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }

        if (currentMethodOrCtor != null) {
            currentClassesEnumOrInterfaces.peekFirst().addMethod((Method) currentMethodOrCtor);
            currentMethodOrCtor = null;
        }
        log.debug("** Exiting exitVoidMethodDeclaratorRest" + ctx.getText());
    }


    /*
    *
    * *************************************************  CURRENT MODIFIERS FOR METHODS /CTOR *********************************************
    *
    */
    @Override
    public void enterClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }
        log.debug("** Entering enterClassBodyDeclaration" + ctx.getText());

        if (ctx.modifiers() != null && ctx.modifiers().modifier().size() > 0) {
            currentNonParameterAnnotations = new ArrayList<Annotation>();
            currentMethodOrCtorModifiers = new ArrayList<String>();
            for (ModifierContext cur : ctx.modifiers().modifier()) {
                if (cur.annotation() != null) {
                    currentNonParameterAnnotations.add(createAnnotationFromAnnotationContext(cur.annotation()));
                } else {
                    currentMethodOrCtorModifiers.add(cur.getText());
                }
            }
        }
    }

    @Override
    public void exitClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }

        currentMethodOrCtorModifiers = null;
        currentNonParameterAnnotations = null;
        log.debug("** Exiting exitClassBodyDeclaration" + ctx.getText());
    }

    /*
    *
    * *************************************************  CURRENT ANNOTATIONS FOR METHOD/CTOR PARAMETERS *********************************************
    *
    */

    @Override
    public void enterVariableModifier(JavaParser.VariableModifierContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }

        log.debug("** Entering enterVariableModifier" + ctx.getText());

        if (currentParametersAnnotations != null && ctx.annotation() != null) {
            currentParametersAnnotations.add(createAnnotationFromAnnotationContext(ctx.annotation()));
        }
    }

    /*
    *
    * *************************************************  METHOD BODY. CAPTURE STATE TO REMOVE ANONYMOUS FUNCTIONS *********************************************
    *
    */


    @Override
    public void enterMethodBody(JavaParser.MethodBodyContext ctx) {
        curInMethodBodyLevel++;
    }

    @Override
    public void exitMethodBody(JavaParser.MethodBodyContext ctx) {
        curInMethodBodyLevel--;
    }


    /*
    *
    * *************************************************  METHOD/CTOR PARAMETERS *********************************************
    *
    * The rules for arguments are a bit tricky; we set currentParametersAnnotations in the Decls and reset it in the DeclsRest
    *
    * formalParameters
    *     :   '(' formalParameterDecls? ')'
    *     ;
    *
    *   formalParameterDecls
    *     :      variableModifiers type formalParameterDeclsRest
    *     ;
    *
    *   formalParameterDeclsRest
    *      :   variableDeclaratorId (',' formalParameterDecls)?
    *      |   '...' variableDeclaratorId
    *      ;
    *
    *
    */


    @Override
    public void enterFormalParameterDecls(JavaParser.FormalParameterDeclsContext ctx) {

        if (currentMethodOrCtor == null) {
            log.warn("enterFormalParameters : no curentMethod ");
            return;
        }

        currentParametersAnnotations = new ArrayList<Annotation>();

        final TypeContext typeContext = ctx.type();

        if (typeContext.classOrInterfaceType() != null && typeContext.classOrInterfaceType().Identifier().size() > 1) {
            log.warn("enterFormalParameters : Found " + typeContext.classOrInterfaceType().Identifier().size() + " classOrInterfaceType for argument");
        }

        final String parameterType = (typeContext.primitiveType() != null) ? typeContext.primitiveType().getText() : typeContext.classOrInterfaceType().Identifier().get(0).getText();
        final FormalParameterDeclsRestContext formalParameterDeclsRestContext = ctx.formalParameterDeclsRest();


        final String parameterVariableName = formalParameterDeclsRestContext.variableDeclaratorId().Identifier().getText();

        log.debug("enterFormalParameters : parameter " + parameterType + ":" + parameterVariableName);

        if (currentMethodOrCtor != null) {
            currentMethodOrCtor.addArgument(new Field(parameterVariableName, getFullyQualifiedType(parameterType), currentParametersAnnotations));
        }
    }

    @Override
    public void exitFormalParameterDeclsRest(JavaParser.FormalParameterDeclsRestContext ctx) {
        currentParametersAnnotations = null;
        log.debug("** Exiting exitFormalParameterDeclsRest" + ctx.getText());
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

    private Annotation createAnnotationFromAnnotationContext(final AnnotationContext annotationContext) {
        final String annotationName = annotationContext.annotationName().getText();
        final String value = annotationContext.elementValue() != null ? annotationContext.elementValue().expression().primary().literal().getText() : null;
        return new  Annotation(annotationName, value);
    }

    private boolean isIncludedInModifier(final String... modifiers) {
        if (currentMethodOrCtorModifiers == null) {
            return false;
        }
        for (String cur : currentMethodOrCtorModifiers) {
            for (String m : modifiers)
                if (m.equals(cur)) {
                    return true;
                }
        }
        return false;
    }

    private String getFullyQualifiedType(final String type) {

        // If primitive type nothing to do:
        if (type.equals("byte") ||
            type.equals("short") ||
            type.equals("int") ||
            type.equals("long") ||
            type.equals("float") ||
            type.equals("double") ||
            type.equals("boolean") ||
            type.equals("char")) {
            return type;
        }

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
