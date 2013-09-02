package com.ning.killbill;

import com.google.common.base.Joiner;
import com.ning.killbill.JavaParser.AnnotationContext;
import com.ning.killbill.JavaParser.ClassDeclarationContext;
import com.ning.killbill.JavaParser.ClassOrInterfaceModifierContext;
import com.ning.killbill.JavaParser.ClassOrInterfaceTypeContext;
import com.ning.killbill.JavaParser.EnumConstantContext;
import com.ning.killbill.JavaParser.FormalParameterDeclsRestContext;
import com.ning.killbill.JavaParser.ImportDeclarationContext;
import com.ning.killbill.JavaParser.InterfaceDeclarationContext;
import com.ning.killbill.JavaParser.InterfaceMemberDeclContext;
import com.ning.killbill.JavaParser.InterfaceMethodOrFieldDeclContext;
import com.ning.killbill.JavaParser.MemberDeclContext;
import com.ning.killbill.JavaParser.MemberDeclarationContext;
import com.ning.killbill.JavaParser.ModifierContext;
import com.ning.killbill.JavaParser.NormalInterfaceDeclarationContext;
import com.ning.killbill.JavaParser.PackageDeclarationContext;
import com.ning.killbill.JavaParser.QualifiedNameContext;
import com.ning.killbill.JavaParser.TypeContext;
import com.ning.killbill.JavaParser.TypeParameterContext;
import com.ning.killbill.JavaParser.VariableDeclaratorContext;
import com.ning.killbill.JavaParser.VariableDeclaratorsContext;
import com.ning.killbill.objects.Annotation;
import com.ning.killbill.objects.ClassEnumOrInterface;
import com.ning.killbill.objects.ClassEnumOrInterface.ClassEnumOrInterfaceType;
import com.ning.killbill.objects.Constructor;
import com.ning.killbill.objects.Field;
import com.ning.killbill.objects.Method;
import com.ning.killbill.objects.MethodOrCtor;
import com.ning.killbill.objects.Type;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KillbillListener extends JavaBaseListener {

    public static final String JAVA_LANG = "java.lang.";

    public static Pattern GENERIC_PATTERN = Pattern.compile("(\\w*)(?:<(.*)>){0,1}\\s*");

    public static final String UNDEFINED_GENERIC = "Undefined";


    private static final Type VOID_TYPE = new Type("void");

    Logger log = LoggerFactory.getLogger(KillbillListener.class);

    /*
     *  Tracks current state as we walk through the tree.
     */
    private final Deque<ClassEnumOrInterface> currentClassesEnumOrInterfaces;
    private MethodOrCtor currentMethodOrCtor;
    private List<String> currentMethodOrCtorModifiers;
    private List<Annotation> currentClassAnnotations;
    private List<Annotation> currentNonParameterAnnotations;
    private List<Annotation> currentParametersAnnotations;
    private Map<String, String> curTypeMethodParameters;
    private Map<String, String> curTypeClassEnumOrInterfaceParameters;

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
        this.currentClassAnnotations = null;
        this.curTypeMethodParameters = null;
        this.curTypeClassEnumOrInterfaceParameters = null;
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
        log.debug("** Entering enterImportDeclaration " + ctx.getText());
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

        if (ctx.annotationTypeDeclaration() != null) {
            // We skip @interface annotation -- until we need it
            return;
        }
        log.debug("** Entering enterInterfaceDeclaration " + ctx.getText());


        final ClassEnumOrInterface classEnumOrInterface = new ClassEnumOrInterface(ctx.normalInterfaceDeclaration().Identifier().getText(), ClassEnumOrInterfaceType.INTERFACE, packageName, currentClassAnnotations, false);
        currentClassesEnumOrInterfaces.push(classEnumOrInterface);
        if (ctx.normalInterfaceDeclaration().typeList() != null) {
            for (TypeContext cur : ctx.normalInterfaceDeclaration().typeList().type()) {
                classEnumOrInterface.addSuperInterface(getFullyQualifiedType(cur.getText()).getBaseType());
            }
        }
    }

    @Override
    public void exitInterfaceDeclaration(InterfaceDeclarationContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }

        if (ctx.annotationTypeDeclaration() != null) {
            // We skip @interface annotation -- until we need it
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
            final ClassEnumOrInterface classEnumOrInterface = new ClassEnumOrInterface(ctx.normalClassDeclaration().Identifier().getText(), ClassEnumOrInterfaceType.CLASS, packageName, currentClassAnnotations, isIncludedInMethodOrCtorModifier("abstract"));
            currentClassesEnumOrInterfaces.push(classEnumOrInterface);
            final TypeContext superClass = ctx.normalClassDeclaration().type();
            if (superClass != null) {
                classEnumOrInterface.addSuperClass(getFullyQualifiedType(superClass.getText()).getBaseType());
            }
            if (ctx.normalClassDeclaration().typeList() != null) {
                for (TypeContext cur : ctx.normalClassDeclaration().typeList().type()) {
                    classEnumOrInterface.addSuperInterface(getFullyQualifiedType(cur.getText()).getBaseType());
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

        final ClassEnumOrInterface classEnumOrInterface = new ClassEnumOrInterface(ctx.Identifier().getText(), ClassEnumOrInterfaceType.ENUM, packageName, currentClassAnnotations, false);
        currentClassesEnumOrInterfaces.push(classEnumOrInterface);
        final List<EnumConstantContext> enumValues = ctx.enumBody().enumConstants().enumConstant();
        for (EnumConstantContext cur : enumValues) {
            classEnumOrInterface.addEnumValue(cur.Identifier().getText());
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

        final String returnValueType = (ctx.type().primitiveType() != null) ? ctx.type().primitiveType().getText() : ctx.type().classOrInterfaceType().getText();
        currentMethodOrCtor = new Method(ctx.Identifier().getText(), getFullyQualifiedType(returnValueType), true, currentNonParameterAnnotations);
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


    @Override
    public void enterVoidInterfaceMethodDeclaratorRest(JavaParser.VoidInterfaceMethodDeclaratorRestContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }
        log.debug("** Entering enterVoidInterfaceMethodDeclaratorRest" + ctx.getText());
        currentMethodOrCtor = new Method(((InterfaceMemberDeclContext) ctx.getParent()).Identifier().getText(), VOID_TYPE, true, currentNonParameterAnnotations);
    }

    @Override
    public void exitVoidInterfaceMethodDeclaratorRest(JavaParser.VoidInterfaceMethodDeclaratorRestContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }
        currentClassesEnumOrInterfaces.peekFirst().addMethod((Method) currentMethodOrCtor);
        currentMethodOrCtor = null;
        log.debug("** Exiting exitVoidInterfaceMethodDeclaratorRest" + ctx.getText());
    }

    @Override
    public void enterInterfaceGenericMethodDecl(JavaParser.InterfaceGenericMethodDeclContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }
        log.debug("** Entering enterInterfaceGenericMethodDecl" + ctx.getText());
        String returnValueType = (ctx.type() == null) ? "void" :
                ((ctx.type().primitiveType() != null) ? ctx.type().primitiveType().getText() : ctx.type().classOrInterfaceType().getText());


        /*
         We don't need that, we only remove the Generic parameter and that is it.
        if (ctx.typeParameters() != null) {
            enterTypeParameters(ctx.typeParameters());
            if (curTypeMethodParameters.get(returnValueType) != null) {
                returnValueType = curTypeMethodParameters.get(returnValueType);
            }
            exitTypeParameters(ctx.typeParameters());
        }
        */
        currentMethodOrCtor = new Method(ctx.Identifier().getText(), getFullyQualifiedType(returnValueType), true, currentNonParameterAnnotations);
    }

    @Override
    public void exitInterfaceGenericMethodDecl(JavaParser.InterfaceGenericMethodDeclContext ctx) {
        log.debug("** Exiting exitInterfaceGenericMethodDecl" + ctx.getText());
        currentClassesEnumOrInterfaces.peekFirst().addMethod((Method) currentMethodOrCtor);
        currentMethodOrCtor = null;
        curTypeMethodParameters = null;
    }


    @Override
    public void enterInterfaceMethodDeclaratorRest(JavaParser.InterfaceMethodDeclaratorRestContext ctx) {
        log.debug("** Entering enterInterfaceMethodDeclaratorRest" + ctx.getText());
        if (ctx.qualifiedNameList() != null) {
            for (QualifiedNameContext cur : ctx.qualifiedNameList().qualifiedName()) {
                final String exception = cur.Identifier().get(0).getText();
                final String resolvedException = getFullyQualifiedType(exception).getBaseType();
                currentMethodOrCtor.addException(resolvedException);
            }
        }
    }

    @Override
    public void exitInterfaceMethodDeclaratorRest(JavaParser.InterfaceMethodDeclaratorRestContext ctx) {
        log.debug("** Exiting exitInterfaceMethodDeclaratorRest" + ctx.getText());
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
        if (!isIncludedInMethodOrCtorModifier("private", "protected", "static")) {
            final TypeContext typeContext = ((MemberDeclarationContext) ctx.getParent()).type();
            final String returnValueType = (typeContext.primitiveType() != null) ? typeContext.primitiveType().getText() : typeContext.classOrInterfaceType().getText();
            currentMethodOrCtor = new Method(ctx.Identifier().getText(), getFullyQualifiedType(returnValueType), isIncludedInMethodOrCtorModifier("abstract"), currentNonParameterAnnotations);
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

        if (!isIncludedInMethodOrCtorModifier("private", "protected", "static")) {
            MemberDeclContext meberDecl = (MemberDeclContext) ctx.getParent();
            currentMethodOrCtor = new Method(meberDecl.Identifier().getText(), VOID_TYPE, isIncludedInMethodOrCtorModifier("abstract"), currentNonParameterAnnotations);
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

    // TODO exceptions for class methods
    @Override
    public void enterMethodDeclaratorRest(JavaParser.MethodDeclaratorRestContext ctx) {
    }

    @Override
    public void exitMethodDeclaratorRest(JavaParser.MethodDeclaratorRestContext ctx) {
    }


    /*
    *
    * *************************************************  CONSTRUCTOR *********************************************
    *
    */
    @Override
    public void enterConstructorDeclaratorRest(JavaParser.ConstructorDeclaratorRestContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }
        log.debug("** Entering enterConstructorDeclaratorRest" + ctx.getText());
        final MemberDeclContext ctor = (MemberDeclContext) ctx.getParent();
        currentMethodOrCtor = new Constructor(ctor.Identifier().getText(), isIncludedInMethodOrCtorModifier("abstract"), currentNonParameterAnnotations);
    }

    @Override
    public void exitConstructorDeclaratorRest(JavaParser.ConstructorDeclaratorRestContext ctx) {
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
    @Override
    public void enterFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }
        log.debug("** Entering enterFieldDeclaration" + ctx.getText());
        if (!isIncludedInMethodOrCtorModifier("static")) {
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

    @Override
    public void exitFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }
        // Nothing to do.
        log.debug("** Exiting exitFieldDeclaration" + ctx.getText());
    }


    /*
    *
    * *************************************************  CURRENT MODIFIERS FOR CLASSES *********************************************
    *
    */
    @Override
    public void enterClassOrInterfaceDeclaration(JavaParser.ClassOrInterfaceDeclarationContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }
        log.debug("** Entering enterClassOrInterfaceDeclaration" + ctx.getText());
        currentMethodOrCtorModifiers = new ArrayList<String>();
        currentClassAnnotations = new ArrayList<Annotation>();
    }

    @Override
    public void exitClassOrInterfaceDeclaration(JavaParser.ClassOrInterfaceDeclarationContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }

        currentMethodOrCtorModifiers = null;
        currentClassAnnotations = null;
        log.debug("** Exiting exitClassOrInterfaceDeclaration" + ctx.getText());
    }


    @Override
    public void enterClassOrInterfaceModifiers(JavaParser.ClassOrInterfaceModifiersContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }
        log.debug("** Entering enterClassOrInterfaceModifier" + ctx.getText());

        if (ctx.classOrInterfaceModifier() != null && ctx.classOrInterfaceModifier().size() > 0) {
            for (ClassOrInterfaceModifierContext cur : ctx.classOrInterfaceModifier()) {
                if (cur.annotation() == null) {
                    currentMethodOrCtorModifiers.add(cur.getText());
                } else {
                    currentClassAnnotations.add(createAnnotationFromAnnotationContext(cur.annotation()));
                }
            }
        }
    }

    @Override
    public void exitClassOrInterfaceModifiers(JavaParser.ClassOrInterfaceModifiersContext ctx) {
        // Nothing to do
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
     * *************************************************  GENERIC TYPE PARAMETERS *********************************************
     *
     */
    @Override
    public void enterTypeParameters(JavaParser.TypeParametersContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }

        HashMap<String, String> tmp = new HashMap<String, String>();
        for (TypeParameterContext cur : ctx.typeParameter()) {
            final String curIdentifier = cur.Identifier().getText();
            final String curClassOrInterface = (cur.typeBound() == null) ? UNDEFINED_GENERIC :
                    cur.typeBound().type().get(0).classOrInterfaceType().Identifier().get(0).getText();
            tmp.put(curIdentifier, curClassOrInterface);
        }
        // We defer cases for classes or enum until we hit them
        if (ctx.getParent() instanceof NormalInterfaceDeclarationContext) {
            curTypeClassEnumOrInterfaceParameters = tmp;
        } else {
            curTypeMethodParameters = tmp;
        }
        log.debug("** Entering enterTypeParameters" + ctx.getText());


    }

    @Override
    public void exitTypeParameters(JavaParser.TypeParametersContext ctx) {
        if (curInMethodBodyLevel > 0) {
            return;
        }
        // Nothing to do, we cleanup in the exitInterfaceGenericMethodDecl
        log.debug("** Exiting exitTypeParameters" + ctx.getText());
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

        if (currentMethodOrCtor == null && !currentClassesEnumOrInterfaces.peekFirst().isEnum()) {
            log.warn("enterFormalParameters : no curentMethod ");
            return;
        }

        currentParametersAnnotations = new ArrayList<Annotation>();

        final TypeContext typeContext = ctx.type();

        if (typeContext.classOrInterfaceType() != null && typeContext.classOrInterfaceType().Identifier().size() > 1) {
            log.warn("enterFormalParameters : Found " + typeContext.classOrInterfaceType().Identifier().size() + " classOrInterfaceType for argument");
        }

        //final String parameterType = (typeContext.primitiveType() != null) ? typeContext.primitiveType().getText() : typeContext.classOrInterfaceType().Identifier().get(0).getText();

        String parameterType = null;
        if (typeContext.primitiveType() != null) {
            parameterType = typeContext.primitiveType().getText();
        } else {
            final ClassOrInterfaceTypeContext classOrInterfaceTypeContext = typeContext.classOrInterfaceType();
            parameterType = classOrInterfaceTypeContext.Identifier().get(0).getText();
            String bracketedParam = null;
            if (classOrInterfaceTypeContext.typeArguments() != null && classOrInterfaceTypeContext.typeArguments().size() > 0) {
                bracketedParam = classOrInterfaceTypeContext.typeArguments().get(0).typeArgument().get(0).type().classOrInterfaceType().Identifier().get(0).getText();
            }
            if (bracketedParam != null) {
                parameterType = parameterType + "<" + bracketedParam + ">";
            }
        }


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


    /*
    *
    * ************************************************ HELPERS *********************************************
    */

    private Annotation createAnnotationFromAnnotationContext(final AnnotationContext annotationContext) {
        final String annotationName = annotationContext.annotationName().getText();

        String value =  (annotationContext.elementValue() != null) ? annotationContext.elementValue().expression().getText() : null;
        /*
        if (annotationContext.elementValue() != null) {
            final List<String> valueParts = new LinkedList<String>();
            final JavaParser.ExpressionContext initialExpresion = annotationContext.elementValue().expression();
            for (int i = 0; i < initialExpresion.getChildCount(); i++) {
                final ParseTree el = initialExpresion.getChild(i);
                if (el instanceof  JavaParser.ExpressionContext) {
                    buildValueForExpression(annotationContext.elementValue().expression(), valueParts);
                } else {
                    valueParts.add(el.getText());
                }
            }
            final Joiner joiner = Joiner.on("");
            value = joiner.join(valueParts);
        }
*/
        return new Annotation(annotationName, stripQuoteFromValue(value));
    }

    private void buildValueForExpression(final JavaParser.ExpressionContext expression, final List<String> valueParts) {
        if (expression.primary() != null) {
            final String text = expression.primary().literal() != null ? expression.primary().literal().getText() : expression.primary().getText();
            valueParts.add(text);
        } else if (expression.primary() == null && expression.expression() == null) {
            final String text = expression.getText();
            valueParts.add(text);
        } else {
            final List<JavaParser.ExpressionContext> contexts = expression.expression();
            for (JavaParser.ExpressionContext cur : contexts) {
                buildValueForExpression(cur, valueParts);
            }
        }
    }


    private String stripQuoteFromValue(final String value) {
        if (value != null && value.startsWith("\"") && (value.endsWith("\""))) {
            return value.substring(1, value.length() - 1);
        } else {
            return value;
        }
    }

    private boolean isIncludedInMethodOrCtorModifier(final String... modifiers) {
        return isIncludedIModifier(currentMethodOrCtorModifiers, modifiers);
    }


    private static boolean isIncludedIModifier(final List<String> modiferList, final String... modifiers) {
        if (modiferList == null) {
            return false;
        }
        for (String cur : modiferList) {
            for (String m : modifiers)
                if (m.equals(cur)) {
                    return true;
                }
        }
        return false;
    }

    /**
     * - Generic simple type, e.g : List<String>
     * - Generic fully qualified type, e.g : List<java.lang.String>
     * - Generic Parameter type, e.g : T
     * - Generic Parameter Bracketed type, e.g : List<T>
     *
     * @param type the string type extracted from the parser
     * @return the Type computed
     */
    private Type getFullyQualifiedType(final String type) {
        String baseType;
        String bracketPartIfAny = null;
        Matcher m = KillbillListener.GENERIC_PATTERN.matcher(type);
        if (!m.matches()) {
            throw new RuntimeException(GENERIC_PATTERN + " does not match " + type);
        }
        baseType = m.group(1);
        bracketPartIfAny = m.group(2);

        final String resolvedBaseType = resolveNonBracketedType(baseType);
        final String resolvedBracketPartIfAny = bracketPartIfAny != null ? resolveNonBracketedType(bracketPartIfAny) : null;
        return new Type(resolvedBaseType, resolvedBracketPartIfAny);
    }

    /**
     * We can enter with the following:
     * - Non generic simple type, e.g:  String, boolean, Foo
     * - Non generic fully qualified type, e.g : java.lang.String
     * - Generic Parameter type, e.g : T
     *
     * @param type the type to be resolved
     * @return
     */
    private String resolveNonBracketedType(final String type) {

        // If primitive type nothing to do:
        if (type.equals("byte") ||
                type.equals("short") ||
                type.equals("int") ||
                type.equals("long") ||
                type.equals("float") ||
                type.equals("double") ||
                type.equals("boolean") ||
                type.equals("char") ||
                type.equals("void")) {
            return type;
        }

        // Is already fully qualified?
        String[] parts = type.split("\\.");
        if (parts.length > 1) {
            return type;
        }

        final String resolvedGenericType = resolveGenericIfNeeded(type);
        if (resolvedGenericType.equals(UNDEFINED_GENERIC)) {
            return resolvedGenericType;
        }

        // Is that in the import List?
        if (allImports.get(resolvedGenericType) != null) {
            return allImports.get(resolvedGenericType);
        }

        // If not, is that a java.lang?
        try {

            final String className = JAVA_LANG + resolvedGenericType;
            Class.forName(className);
            return className;
        } catch (ClassNotFoundException ignore) {
        }

        // Finally if we assume that file compiles, then add the current package
        return packageName + "." + resolvedGenericType;
    }


    //
    // This is not super smart, but just trying to figure if we have seen some generic declaration on our way through and if so, return the definition
    //
    private String resolveGenericIfNeeded(String input) {
        // Look first into the method generic map to see if it is there
        String result = input;
        if (curTypeMethodParameters != null && curTypeMethodParameters.containsKey(input)) {
            result = curTypeMethodParameters.get(input);
        } else if (curTypeClassEnumOrInterfaceParameters != null && curTypeClassEnumOrInterfaceParameters.containsKey(input)) {
            result = curTypeClassEnumOrInterfaceParameters.get(input);
            if (result.equals(UNDEFINED_GENERIC)) {
                //throw new RuntimeException("Undefined generic " + input);

            }
        }
        return result;
    }


    // DEBUG ONLY
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
}
