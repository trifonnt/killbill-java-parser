package com.ning.killbill.generators;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ning.killbill.generators.BaseGenerator;
import org.jruby.javasupport.JavaUtil;

import com.ning.killbill.com.ning.killbill.args.KillbillParserArgs;
import com.ning.killbill.com.ning.killbill.args.KillbillParserArgs.GENERATOR_MODE;
import com.ning.killbill.generators.Generator;
import com.ning.killbill.generators.GeneratorException;
import com.ning.killbill.objects.Annotation;
import com.ning.killbill.objects.ClassEnumOrInterface;
import com.ning.killbill.objects.Constructor;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.io.Resources;

public abstract class ClientLibraryBaseGenerator extends BaseGenerator implements Generator {


    protected int curIndent = 0;

    public ClientLibraryBaseGenerator() {
        super();
        resetIndentation();
    }

    protected void resetIndentation() {
        curIndent = 0;
    }

    @Override
    public void generate(final KillbillParserArgs args) throws GeneratorException {

        final List<ClassEnumOrInterface> allGeneratedClasses = new ArrayList<ClassEnumOrInterface>();

        startGeneration(allClasses, args.getOutputDir(), args.getMode());

        final List<URI> input = args.getInput();
        try {
            parseAll(args, input);

            for (ClassEnumOrInterface cur : allClasses) {
                if (!cur.isAbstract() && !cur.isEnum()) {

                    if (!isPackageIncluded(cur.getPackageName(), args.getPackagesGeneratorFilter()) ||
                        isClassExcluded(cur.getName(), args.getClassGeneratorExcludes())) {
                        continue;
                    }

                    log.info("Generating file for object " + cur.getName());
                    generateClass(cur, allClasses, args.getOutputDir(), args.getMode());
                    allGeneratedClasses.add(cur);
                }
            }
        } catch (IOException e) {
            throw new GeneratorException("Failed to generate code: ", e);
        }
        completeGeneration(allGeneratedClasses, args.getOutputDir(), args.getMode());
    }

    private boolean isPackageIncluded(final String curPackage, final List<String> allPackagesGenerator) {
        boolean res = (allPackagesGenerator.size() == 0) || (isClassExcluded(curPackage, allPackagesGenerator));
        return res;
    }

    protected void writeAppend(final String str, final Writer w) throws IOException {
        w.write(str);
    }

    protected void writeWithIndentation(final String str, final Writer w, int curIndentOffest) throws IOException {
        writeWithIndentationInternal(str, w, curIndentOffest, false);
    }

    protected void writeNewLine(final Writer w) throws IOException {
        w.write("\n");
    }

    protected void writeWithIndentationAndNewLine(final String str, final Writer w, int curIndentOffest) throws IOException {
        writeWithIndentationInternal(str, w, curIndentOffest, true);
    }

    private void writeWithIndentationInternal(final String str, final Writer w, final int curIndentOffest, boolean newLine) throws IOException {
        curIndent += curIndentOffest;
        for (int i = 0; i < curIndent; i++) {
            w.write(" ");
        }
        w.write(str);
        if (newLine) {
            w.write("\n");
        }
    }


    protected void writeLicense(final File output) throws GeneratorException {
        try {

            final URL licenseUrl = Resources.getResource(getLicense());

            final OutputStream out = new FileOutputStream(output);
            Resources.copy(licenseUrl, out);

        } catch (IllegalArgumentException e) {
            throw new GeneratorException("Cannot find license file " + getLicense(), e);
        } catch (IOException e) {
            throw new GeneratorException("Failed to write license file " + getLicense(), e);
        }
    }


    protected abstract void startGeneration(final List<ClassEnumOrInterface> classes, final File outputDir, final GENERATOR_MODE mode) throws GeneratorException;

    protected abstract void generateClass(final ClassEnumOrInterface obj, final List<ClassEnumOrInterface> allClasses, final File outputDir, final GENERATOR_MODE mode) throws GeneratorException;

    protected abstract void completeGeneration(final List<ClassEnumOrInterface> classes, final File outputDir, final GENERATOR_MODE mode) throws GeneratorException;

    protected abstract String createFileName(final String name, final boolean b);

    protected abstract String getRequirePrefix(final GENERATOR_MODE mode) throws GeneratorException;

    protected abstract String getRequireFileName();

    protected abstract String getLicense();

}
