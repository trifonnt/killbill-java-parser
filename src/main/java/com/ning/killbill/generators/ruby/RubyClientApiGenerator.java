package com.ning.killbill.generators.ruby;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.util.List;

import com.ning.killbill.generators.GeneratorException;
import com.ning.killbill.objects.Annotation;
import com.ning.killbill.objects.ClassEnumOrInterface;
import com.ning.killbill.objects.Constructor;
import com.ning.killbill.objects.Field;

import com.google.common.io.Files;
import com.google.common.io.Resources;

public class RubyClientApiGenerator extends BaseGenerator {

    private final static String LICENSE_NAME = "RubyLicense.txt";

    private final static int INDENT_LEVEL = 2;
    private final static String DEFAULT_BASE_CLASS = "Resource";
    private final static String REQUIRE_PREFIX = "killbill_client/models/gen/";

    private final static String REQUIRE_FILE_NAME = "require_gen.rb";

    private final String[] MODULES = {"KillBillClient", "Model"};

    private int curIndent = 0;

    public RubyClientApiGenerator() {
        this.curIndent = 0;
    }

    @Override
    protected void startGeneration(final List<ClassEnumOrInterface> classes, final File outputDir) throws GeneratorException {
    }

    @Override
    protected void generateClass(final ClassEnumOrInterface obj, final File outputDir) throws GeneratorException {
        final File output = new File(outputDir, createFileName(obj.getName(), true));

        writeLicense(output);

        Writer w = null;
        try {
            w = new FileWriter(output, true);

            writeHeader(w);

            boolean first = true;
            for (int i = 0; i < MODULES.length; i++) {
                if (first) {
                    writeWithIndentation("module " + MODULES[i], w, 0);
                    first = false;
                } else {
                    writeWithIndentation("module " + MODULES[i], w, INDENT_LEVEL);
                }
            }
            //final String baseClass = obj.getSuperBaseClass() != null ? obj.getSuperBaseClass() : DEFAULT_BASE_CLASS;
            final String baseClass = DEFAULT_BASE_CLASS;
            writeWithIndentation("class " + createClassName(obj.getName()) + " < " + baseClass, w, INDENT_LEVEL);
            final Constructor ctor = getJsonCreatorCTOR(obj);
            first = true;
            for (Field f : ctor.getOrderedArguments()) {
                final String attribute = camelToUnderscore(getJsonPropertyAnnotationValue(obj, f));
                if (first) {
                    first = false;
                    writeWithIndentation("attribute :" + attribute, w, INDENT_LEVEL);
                } else {
                    writeWithIndentation("attribute :" + attribute, w, 0);
                }
            }
            writeWithIndentation("end", w, -INDENT_LEVEL);
            for (int i = 0; i < MODULES.length; i++) {
                writeWithIndentation("end", w, -INDENT_LEVEL);
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
    protected void completeGeneration(final List<ClassEnumOrInterface> classes, final File outputDir) throws GeneratorException {

        final File output = new File(outputDir, REQUIRE_FILE_NAME);

        writeLicense(output);
        try {
            final Writer w = new FileWriter(output, true);
            writeHeader(w);
            for (ClassEnumOrInterface cur : classes) {
                w.write("require '" + REQUIRE_PREFIX + createFileName(cur.getName(), false) + "'\n");
            }
            w.flush();
            w.close();
        } catch (IOException e) {
            throw new GeneratorException("Failed to create require file", e);
        }
    }

    private void writeLicense(final File output) throws GeneratorException {
        try {

            final URL licenseUrl = Resources.getResource(LICENSE_NAME);

            final OutputStream out = new FileOutputStream(output);
            Resources.copy(licenseUrl, out);

            /*
            System.out.println("**************************  LICENSE URL " +  licenseUrl + "**************************** " + licenseUrl.getFile());
            final File license = new File(licenseUrl.getFile());
            Files.copy(license, output);
            */
        } catch (IllegalArgumentException e) {
            throw new GeneratorException("Cannot find license file " + LICENSE_NAME, e);
        } catch (IOException e) {
            throw new GeneratorException("Failed to write license file " + LICENSE_NAME, e);
        }
    }

    private void writeHeader(final Writer w) throws IOException {
        w.write("\n");
        w.write("\n");
        w.write("#\n");
        w.write("#                       DO NOT EDIT!!!\n");
        w.write("#    File automatically generated by killbill-java-parser (git@github.com:killbill/killbill-java-parser.git)\n");
        w.write("#\n");
        w.write("\n");
        w.write("\n");
    }

    private void writeWithIndentation(final String str, final Writer w, int curIndentOffest) throws IOException {
        curIndent += curIndentOffest;
        for (int i = 0; i < curIndent; i++) {
            w.write(" ");
        }
        w.write(str);
        w.write("\n");
    }

    private static String createFileName(final String objName, boolean addRubyExtension) {
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
