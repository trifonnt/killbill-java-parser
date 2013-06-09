package com.ning.killbill.generators.ruby;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import com.ning.killbill.generators.GeneratorException;
import com.ning.killbill.objects.ClassEnumOrInterface;
import com.ning.killbill.objects.Field;
import com.ning.killbill.objects.Method;

import com.google.common.collect.Lists;

public class JRubyPluginGenerator extends BaseGenerator {

    private final static String LICENSE_NAME = "RubyLicense.txt";
    private final static int INDENT_LEVEL = 2;

    private final String[] MODULES = {"Killbill", "Plugin", "Model"};

    public JRubyPluginGenerator() {
        super();
    }

    @Override
    protected void startGeneration(final List<ClassEnumOrInterface> classes, final File outputDir) throws GeneratorException {

    }


    @Override
    protected void generateClass(final ClassEnumOrInterface obj, final List<ClassEnumOrInterface> allClasses, final File outputDir) throws GeneratorException {

        resetIndentation();

        final File output = new File(outputDir, createFileName(obj.getName(), true));

        writeLicense(output);

        Writer w = null;
        try {
            w = new FileWriter(output, true);

            writeHeader(w);

            boolean first = true;
            for (int i = 0; i < MODULES.length; i++) {
                if (first) {
                    writeWithIndentationAndNewLine("module " + MODULES[i], w, 0);
                    first = false;
                } else {
                    writeWithIndentationAndNewLine("module " + MODULES[i], w, INDENT_LEVEL);
                }
            }

            final List<Method> flattenedMethods = new ArrayList<Method>();
            flattenedMethods.addAll(getTopMethods(obj, allClasses));
            flattenedMethods.addAll(obj.getMethods());

            dedupPreserveOrder(flattenedMethods);

            writeNewLine(w);
            writeWithIndentationAndNewLine("class " + obj.getName(), w, INDENT_LEVEL);
            writeNewLine(w);
            writeWithIndentationAndNewLine("include " + obj.getPackageName() + "." + obj.getName(), w, INDENT_LEVEL);
            writeNewLine(w);

            if (obj.isInterface() && isApiFile(obj.getName())) {
                generateForApi(obj, w, flattenedMethods);
            } else {
                generateForPojo(obj, w, flattenedMethods);
            }


            writeWithIndentationAndNewLine("end", w, -INDENT_LEVEL);
            for (int i = 0; i < MODULES.length; i++) {
                writeWithIndentationAndNewLine("end", w, -INDENT_LEVEL);
            }
            w.flush();
            w.close();

        } catch (FileNotFoundException e) {
            throw new GeneratorException("Failed to generate file " + obj.getName(), e);
        } catch (IOException e) {
            throw new GeneratorException("Failed to generate file " + obj.getName(), e);
        }
    }

    private void generateForApi(final ClassEnumOrInterface obj, final Writer w, final List<Method> flattenedMethods) throws IOException, GeneratorException {
        writeWithIndentationAndNewLine("def initialize(real_java_api)", w, 0);
        writeWithIndentationAndNewLine("@real_java_api = real_java_api", w, INDENT_LEVEL);
        writeWithIndentationAndNewLine("end", w, -INDENT_LEVEL);
        writeNewLine(w);

        for (final Method m : flattenedMethods) {
            final String returnValue = m.getReturnValueType().getBaseType();
            writeNewLine(w);
            writeWithIndentation("java_signature 'Java::" + returnValue + " " + m.getName() + "(", w, 0);
            boolean first = true;
            for (Field f : m.getOrderedArguments()) {
                if (!first) {
                    writeAppend(", ", w);
                }
                writeAppend("Java::" + f.getType().getBaseType(), w);
                first = false;
            }
            writeAppend(")'", w);
            writeNewLine(w);

            final String method_name = camelToUnderscore(m.getName());
            writeWithIndentation("def " + method_name + "(", w, 0);
            first = true;
            for (Field f : m.getOrderedArguments()) {
                if (!first) {
                    writeAppend(", ", w);
                }
                writeAppend(f.getName(), w);
                first = false;
            }
            writeAppend(")", w);
            writeNewLine(w);

            boolean firstArg = true;
            for (Field f : m.getOrderedArguments()) {
                if (firstArg) {
                    writeWithIndentationAndNewLine("if !" + f.getName() + ".nil? && " + f.getName() + ".respond_to? :to_java", w, INDENT_LEVEL);
                    firstArg = false;
                } else {
                    writeWithIndentationAndNewLine("if !" + f.getName() + ".nil? && " + f.getName() + ".respond_to? : to_java", w, 0);
                }
                writeWithIndentationAndNewLine(f.getName() + " = " + f.getName() + ".to_java", w, INDENT_LEVEL);
                writeWithIndentationAndNewLine("end", w, -INDENT_LEVEL);
                writeNewLine(w);

            }

            writeWithIndentationAndNewLine("begin", w, 0);
            writeWithIndentation("res = @real_java_api." + method_name + "(", w, INDENT_LEVEL);
            first = true;
            for (Field f : m.getOrderedArguments()) {
                if (!first) {
                    writeAppend(", ", w);
                }
                writeAppend(f.getName(), w);
                first = false;
            }
            writeAppend(")'", w);
            writeNewLine(w);
            // conversion
            generateToRuby("res", m.getReturnValueType().getBaseType(), m.getReturnValueType().getGenericType(), flattenedMethods, w, 0);
            writeWithIndentationAndNewLine("return res", w, 0);

            writeWithIndentationAndNewLine("rescue java.lang.Exception => e" , w, -INDENT_LEVEL);

            writeWithIndentationAndNewLine("if e.respond_to? :to_ruby", w, INDENT_LEVEL);
            writeWithIndentationAndNewLine("raise e.to_ruby", w, INDENT_LEVEL);
            writeWithIndentationAndNewLine("else", w, -INDENT_LEVEL);
            // TODO
            writeWithIndentationAndNewLine("raise ApiException.new(\"TODO\")", w, INDENT_LEVEL);
            writeWithIndentationAndNewLine("end", w, -INDENT_LEVEL);

            writeWithIndentationAndNewLine("end", w, -INDENT_LEVEL);

            writeWithIndentationAndNewLine("end" , w, -INDENT_LEVEL);
        }
    }

    private boolean isApiFile(final String fileName) {
        return fileName.endsWith("Api");
    }

    private void generateForPojo(final ClassEnumOrInterface obj, final Writer w, final List<Method> flattenedMethods) throws IOException, GeneratorException {
        generateAttributeAccessorFromGetterMethods(obj, flattenedMethods, w);

        //generateInitializeFromGetterMethods(obj, flattenedMethods, w);
        //generateInitializeBlockFromGetterMethods(obj, flattenedMethods, w);

        writeWithIndentationAndNewLine("def initialize()", w, 0);
        writeWithIndentationAndNewLine("end", w, 0);
        writeNewLine(w);

        generateToJava(obj, flattenedMethods, w);
    }


    private void generateToRuby(final String member, final String returnValueType, final String returnValueGeneric, final List<Method> flattenedMethods, final Writer w, int indentOffset) throws IOException, GeneratorException {
        // TODO
        writeWithIndentationAndNewLine(member + " = " + member, w, 0);

    }

    private void generateToJava(final ClassEnumOrInterface obj, final List<Method> flattenedMethods, final Writer w) throws IOException, GeneratorException {
        boolean first = true;
        writeWithIndentationAndNewLine("def to_java()", w, 0);
        for (final Method m : flattenedMethods) {
            if (!m.isGetter()) {
                continue;
            }
            if (first) {
                writeConversionToJava(m, allClasses, w, INDENT_LEVEL);
                first = false;
            } else {
                writeNewLine(w);
                writeConversionToJava(m, allClasses, w, 0);
            }
        }
        writeWithIndentationAndNewLine("end", w, -INDENT_LEVEL);
        writeNewLine(w);
    }

    private void writeConversionToJava(final Method m, final List<ClassEnumOrInterface> allClasses, final Writer w, int indentOffset) throws GeneratorException, IOException {
        final String member = camelToUnderscore(convertGetterMethodToFieldName(m.getName()));
        final String returnValueType = m.getReturnValueType().getBaseType();
        final String returnValueGeneric = m.getReturnValueType().getGenericType();
        writeConversionToJava(member, returnValueType, returnValueGeneric, allClasses, w, indentOffset);
    }

    private void writeConversionToJava(final String member, final String returnValueType, final String returnValueGeneric, final List<ClassEnumOrInterface> allClasses, final Writer w, int indentOffset) throws GeneratorException, IOException {

        writeWithIndentationAndNewLine("# conversion for " + member + " [type = " + returnValueType + "]", w, indentOffset);
        if (returnValueType.equals("byte")) {
            // default jruby conversion should be fine
        } else if (returnValueType.equals("short") || returnValueType.equals("java.lang.Short")) {
            // default jruby conversion should be fine
        } else if (returnValueType.equals("int") || returnValueType.equals("java.lang.Integer")) {
            // default jruby conversion should be fine
            writeWithIndentationAndNewLine(member + " = " + member, w, 0);
        } else if (returnValueType.equals("long") || returnValueType.equals("java.lang.Long")) {
            // default jruby conversion should be fine
            writeWithIndentationAndNewLine(member + " = " + member, w, 0);
        } else if (returnValueType.equals("float") || returnValueType.equals("java.lang.Float")) {
            // default jruby conversion should be fine
            writeWithIndentationAndNewLine(member + " = " + member, w, 0);
        } else if (returnValueType.equals("double") || returnValueType.equals("java.lang.Double")) {
            // default jruby conversion should be fine
            writeWithIndentationAndNewLine(member + " = " + member, w, 0);
        } else if (returnValueType.equals("char") || returnValueType.equals("java.lang.Char")) {
            // default jruby conversion should be fine
            writeWithIndentationAndNewLine(member + " = " + member, w, 0);
        } else if (returnValueType.equals("boolean") || returnValueType.equals("java.lang.Boolean")) {
            writeWithIndentationAndNewLine(member + " = " + member + ".nil? ? java.lang.Boolean.new(false) : java.lang.Boolean.new(" + member + ")", w, 0);
        } else if (returnValueType.equals("java.lang.Throwable")) {
            writeWithIndentationAndNewLine(member + " = " + member + ".to_s if ! " + member + ".nil?", w, 0);
        } else {
            if ("java.lang.String".equals(returnValueType)) {
                // default jruby conversion should be fine
                writeWithIndentationAndNewLine(member + " = " + member, w, 0);
            } else if ("java.util.UUID".equals(returnValueType)) {
                writeWithIndentationAndNewLine(member + " = java.util.UUID.fromString(" + member + ".to_s) if ! " + member + ".nil?", w, 0);
            } else if ("java.math.BigDecimal".equals(returnValueType)) {
                writeWithIndentationAndNewLine("if " + member + ".nil?", w, 0);
                writeWithIndentationAndNewLine(member + " = java.math.BigDecimal::ZERO", w, INDENT_LEVEL);
                writeWithIndentationAndNewLine("else", w, -INDENT_LEVEL);
                writeWithIndentationAndNewLine(member + " = java.math.BigDecimal.new(" + member + ".respond_to?(:cents) ? " + member + ".cents : " + member + ".to_i)", w, INDENT_LEVEL);
                writeWithIndentationAndNewLine("end", w, -INDENT_LEVEL);
            } else if ("java.util.Date".equals(returnValueType)) {
                // TODO STEPH
            } else if ("org.joda.time.DateTime".equals(returnValueType)) {
                writeWithIndentationAndNewLine("if ! " + member + ".nil?", w, 0);
                writeWithIndentationAndNewLine(member + " =  (" + member + ".kind_of? Time) ? DateTime.parse(" + member + ".to_s) : " + member, w, INDENT_LEVEL);
                writeWithIndentationAndNewLine(member + " = org.joda.time.DateTime.new(" + member + ".to_s, org.joda.time.DateTimeZone::UTC)", w, 0);
                writeWithIndentationAndNewLine("end", w, -INDENT_LEVEL);
            } else if ("org.joda.time.LocalDate".equals(returnValueType)) {
                writeWithIndentationAndNewLine("if ! " + member + ".nil?", w, 0);
                writeWithIndentationAndNewLine(member + " = org.joda.time.LocalDate.parse(" + member + ".to_s)", w, 0);
                writeWithIndentationAndNewLine("end", w, -INDENT_LEVEL);
            } else if ("org.joda.time.DateTimeZone".equals(returnValueType)) {
                writeWithIndentationAndNewLine("if ! " + member + ".nil?", w, 0);
                writeWithIndentationAndNewLine(member + " = Java::org.joda.time.DateTimeZone.forID(" + member + ".respond_to?(:identifier) ? " + member + ".identifier : " + member + ".to_s)", w, INDENT_LEVEL);
                writeWithIndentationAndNewLine("end", w, -INDENT_LEVEL);
            } else if ("java.util.List".equals(returnValueType) ||
                       "java.util.Collection".equals(returnValueType) ||
                       "java.util.Iterator".equals(returnValueType)) {
                writeWithIndentationAndNewLine("tmp = java.util.ArrayList.new", w, 0);
                writeWithIndentationAndNewLine(member + ".each do |m|", w, 0);
                writeConversionToJava("m", returnValueGeneric, null, allClasses, w, INDENT_LEVEL);
                writeWithIndentationAndNewLine("tmp.add(m)", w, 0);
                writeWithIndentationAndNewLine("end", w, -INDENT_LEVEL);
                writeWithIndentationAndNewLine(member + " = tmp", w, 0);
            } else {
                // At this point if we can't find the class we throw
                findClassEnumOrInterface(returnValueType, allClasses);
                writeWithIndentationAndNewLine(member + " = " + member + ".to_java if ! " + member + ".nil?", w, 0);
            }
        }
    }

    private void dedupPreserveOrder(final List<Method> flattenedMethods) {

        final TreeSet<String> currentMethodNames = new TreeSet<String>();
        final Iterator<Method> it = flattenedMethods.iterator();
        while (it.hasNext()) {
            final String methodName = it.next().getName();
            if (currentMethodNames.contains(methodName)) {
                it.remove();
                continue;
            }
            currentMethodNames.add(methodName);
        }
    }

    private void generateInitializeBlockFromGetterMethods(final ClassEnumOrInterface obj, final List<Method> flattenedMethods, final Writer w) throws GeneratorException, IOException {
        boolean first = true;
        for (final Method m : flattenedMethods) {
            if (!m.isGetter()) {
                continue;
            }
            final String member = camelToUnderscore(convertGetterMethodToFieldName(m.getName()));
            final String baseString = "@" + member + " = " + member;
            if (first) {
                first = false;
                writeWithIndentationAndNewLine(baseString, w, INDENT_LEVEL);
            } else {
                writeWithIndentationAndNewLine(baseString, w, 0);
            }
        }
    }

    private void generateInitializeFromGetterMethods(final ClassEnumOrInterface obj, final List<Method> flattenedMethods, final Writer w) throws IOException, GeneratorException {
        boolean first = true;
        writeWithIndentation("def initialize(", w, 0);
        for (final Method m : flattenedMethods) {
            if (!m.isGetter()) {
                continue;
            }
            final String member = camelToUnderscore(convertGetterMethodToFieldName(m.getName()));
            if (!first) {
                writeAppend(", ", w);
            }
            writeAppend(member, w);
            first = false;
        }
        writeAppend(")", w);
        writeNewLine(w);
    }

    private void generateAttributeAccessorFromGetterMethods(final ClassEnumOrInterface obj, final List<Method> flattenedMethods, final Writer w) throws IOException, GeneratorException {
        boolean first = true;
        writeWithIndentation("attr_accessor ", w, 0);
        for (final Method m : flattenedMethods) {
            if (!m.isGetter()) {
                continue;
            }
            final String member = camelToUnderscore(convertGetterMethodToFieldName(m.getName()));
            final String baseString = ":" + member;
            if (!first) {
                writeAppend(", ", w);
            }
            writeAppend(baseString, w);
            first = false;
        }
        writeNewLine(w);
        writeNewLine(w);
    }

    private String convertGetterMethodToFieldName(final String input) throws GeneratorException {
        if (input.startsWith("get")) {
            return input.substring(3);
        } else if (input.startsWith("is")) {
            //return input.substring(2);
            return input;
        }
        throw new GeneratorException("Unexpected getter method :" + input);
    }

    private String createFileName(final String name, final boolean b) {
        final String extension = ".rb";
        return camelToUnderscore(name + extension);
    }


    @Override
    protected void completeGeneration(final List<ClassEnumOrInterface> classes, final File outputDir) throws GeneratorException {
    }

    @Override
    protected String getLicense() {
        return LICENSE_NAME;
    }

    private List<Method> getTopMethods(final ClassEnumOrInterface obj, final List<ClassEnumOrInterface> allClasses) throws GeneratorException {
        if (obj.isEnum()) {
            return Collections.emptyList();
        }
        final List<Method> result = new ArrayList<Method>();
        if (obj.isClass()) {
            getMethodsFromExtendedClasses(obj, allClasses, result);
        } else if (obj.isInterface()) {
            getMethodsFromExtendedInterfaces(obj, allClasses, result);
        } else {
            throw new GeneratorException("Unexpected obj with no class/enum/interface:" + obj.getName());
        }
        return result;
    }

    private void getMethodsFromExtendedInterfaces(final ClassEnumOrInterface obj, final List<ClassEnumOrInterface> allClasses, final List<Method> result) throws GeneratorException {
        // Reverse list to match original algorithm from ruby parser
        final List<String> superInterfaces = Lists.reverse(obj.getSuperInterfaces());
        for (final String cur : superInterfaces) {
            // Don't expect to find those in our packages
            if (cur.startsWith("java.lang")) {
                continue;
            }

            final ClassEnumOrInterface ifce = findClassEnumOrInterface(cur, allClasses);
            result.addAll(ifce.getMethods());
            getMethodsFromExtendedInterfaces(ifce, allClasses, result);
        }
    }

    private void getMethodsFromExtendedClasses(final ClassEnumOrInterface obj, final List<ClassEnumOrInterface> allClasses, final List<Method> result) throws GeneratorException {
        final String superBaseClass = obj.getSuperBaseClass();
        if (superBaseClass == null) {
            return;
        }
        // Don't expect to find those in our packages
        if (superBaseClass.startsWith("java.lang")) {
            return;
        }
        final ClassEnumOrInterface superClass = findClassEnumOrInterface(superBaseClass, allClasses);
        result.addAll(superClass.getMethods());
        getMethodsFromExtendedClasses(superClass, allClasses, result);
    }

    private ClassEnumOrInterface findClassEnumOrInterface(final String fullyQualifiedName, final List<ClassEnumOrInterface> allClasses) throws GeneratorException {
        for (final ClassEnumOrInterface cur : allClasses) {
            if (cur.getFullName().equals(fullyQualifiedName)) {
                return cur;
            }
        }
        throw new GeneratorException("Cannot find classEnumOrInterface " + fullyQualifiedName);
    }
}
