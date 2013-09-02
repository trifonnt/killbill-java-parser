package com.ning.killbill.generators.ruby;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import com.ning.killbill.com.ning.killbill.args.KillbillParserArgs.GENERATOR_MODE;
import com.ning.killbill.generators.GeneratorException;
import com.ning.killbill.objects.Annotation;
import com.ning.killbill.objects.ClassEnumOrInterface;
import com.ning.killbill.objects.Constructor;
import com.ning.killbill.objects.Field;

public class RubyClientApiGenerator extends RubyBaseGenerator {

    private final static String LICENSE_NAME = "RubyLicense.txt";

    private final static int INDENT_LEVEL = 2;
    private final static String DEFAULT_BASE_CLASS = "Resource";
    private final static String REQUIRE_PREFIX = "killbill_client/models/gen";

    private final String[] MODULES = {"KillBillClient", "Model"};

    public RubyClientApiGenerator() {
        super();
    }

    @Override
    protected void startGeneration(final List<ClassEnumOrInterface> classes, final File outputDir) throws GeneratorException {
    }

    @Override
    protected void generateClass(final ClassEnumOrInterface obj, final List<ClassEnumOrInterface> allClasses, final File outputDir, final GENERATOR_MODE mode) throws GeneratorException {
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
            //final String baseClass = obj.getSuperBaseClass() != null ? obj.getSuperBaseClass() : DEFAULT_BASE_CLASS;
            final String baseClass = DEFAULT_BASE_CLASS;
            writeWithIndentationAndNewLine("class " + createClassName(obj.getName()) + " < " + baseClass, w, INDENT_LEVEL);
            final Constructor ctor = getJsonCreatorCTOR(obj);
            first = true;
            for (Field f : ctor.getOrderedArguments()) {
                final String attribute = camelToUnderscore(getJsonPropertyAnnotationValue(obj, f));
                if (first) {
                    first = false;
                    writeWithIndentationAndNewLine("attribute :" + attribute, w, INDENT_LEVEL);
                } else {
                    writeWithIndentationAndNewLine("attribute :" + attribute, w, 0);
                }
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

    @Override
    protected void completeGeneration(final List<ClassEnumOrInterface> classes, final File outputDir, final GENERATOR_MODE mode) throws GeneratorException {
        generateRubyRequireFile(classes, outputDir, mode);
    }

    @Override
    protected String getRequirePrefix(final GENERATOR_MODE mode) {
        return REQUIRE_PREFIX;
    }

    @Override
    protected String getRequireFileName() {
        return REQUIRE_FILE_NAME;
    }

    @Override
    protected String getLicense() {
        return LICENSE_NAME;
    }

    @Override
    protected String createFileName(final String objName, boolean addRubyExtension) {
        final String extension = addRubyExtension ? ".rb" : "";
        return camelToUnderscore(createClassName(objName) + extension);
    }

    private static String createClassName(final String objName) {
        return objName.replace("Json", "Attributes");
    }

    private String getJsonPropertyAnnotationValue(final ClassEnumOrInterface obj, final Field f) throws GeneratorException {
        for (Annotation a : f.getAnnotations()) {
            if ("JsonProperty".equals(a.getName())) {
                return a.getValue();
            }
        }
        throw new GeneratorException("Could not find a JsonProperty annotation for object " + obj.getName() + " and field " + f.getName());
    }

    private Constructor getJsonCreatorCTOR(final ClassEnumOrInterface obj) throws GeneratorException {
        final List<Constructor> ctors = obj.getCtors();
        for (Constructor cur : ctors) {
            if (cur.getAnnotations() == null || cur.getAnnotations().size() == 0) {
                continue;
            }
            for (final Annotation a : cur.getAnnotations()) {
                if ("JsonCreator".equals(a.getName())) {
                    return cur;
                }
            }
        }
        throw new GeneratorException("Could not find a CTOR for " + obj.getName() + " with a JsonCreator annotation");
    }
}
