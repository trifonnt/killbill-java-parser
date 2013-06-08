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

            generateAttributeReaderFromGetterMethods(obj, flattenedMethods, w);

            generateInitializeFromGetterMethods(obj, flattenedMethods, w);

            generateInitializeBlockFromGetterMethods(obj, flattenedMethods, w);
            writeWithIndentationAndNewLine("end", w, -INDENT_LEVEL);
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

    private void generateAttributeReaderFromGetterMethods(final ClassEnumOrInterface obj, final List<Method> flattenedMethods, final Writer w) throws IOException, GeneratorException {
        boolean first = true;
        writeWithIndentation("attr_reader ", w, 0);
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
