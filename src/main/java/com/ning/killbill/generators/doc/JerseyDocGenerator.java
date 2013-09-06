package com.ning.killbill.generators.doc;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.ning.killbill.com.ning.killbill.args.KillbillParserArgs;
import com.ning.killbill.generators.BaseGenerator;
import com.ning.killbill.generators.Generator;
import com.ning.killbill.generators.GeneratorException;
import com.ning.killbill.objects.Annotation;
import com.ning.killbill.objects.ClassEnumOrInterface;
import com.ning.killbill.objects.Constructor;
import com.ning.killbill.objects.Field;
import com.ning.killbill.objects.MethodOrDecl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JerseyDocGenerator extends BaseGenerator implements Generator {

    static final Pattern PATTERN_ID = Pattern.compile("\\{(\\w+).*\\}(?:\\+)*");
    static final Pattern PATTERN_PATH = Pattern.compile("(?:JaxrsResource\\.){0,1}(?:\\+){0,1}((?:\\w|_|1|\\.|0)+)(?:\\+){0,1}");

    @Override
    public void generate(KillbillParserArgs args) throws GeneratorException {


        final List<ClassEnumOrInterface> allGeneratedClasses = new ArrayList<ClassEnumOrInterface>();

        final List<URI> input = args.getInput();
        Writer w = null;
        try {

            try {
                final File docFile = createDocFile(args.getOutputDir());
                w = new FileWriter(docFile, true);

                parseAll(args, input);

                for (ClassEnumOrInterface cur : allClasses) {

                    if (isClassExcluded(cur.getFullName(), args.getClassGeneratorExcludes())) {
                        continue;
                    }

                    final List<Annotation> annotations = cur.getAnnotations();
                    if (!containsPathAnnotation(annotations)) {
                        continue;
                    }
                    generateAsciiDoctorClass(cur, allClasses, w);
                    allGeneratedClasses.add(cur);
                }
            } finally {
                w.close();
            }
        } catch (IOException io) {
            throw new GeneratorException("Failed to generate code: ", io);
        }
    }

    private File createDocFile(File outputDir) throws IOException {
        final File doc = new File(outputDir, "rest.adoc");
        doc.createNewFile();
        return doc;
    }

    private void generateAsciiDoctorClass(ClassEnumOrInterface cur, List<ClassEnumOrInterface> allClasses, Writer writer) throws IOException, GeneratorException {

        final List<MethodOrDecl> candidates = new LinkedList<MethodOrDecl>();
        for (MethodOrDecl m : cur.getMethodOrDecls()) {
            Annotation httpVerbAnnotation = getHttpMethodAnnotation(m.getAnnotations());
            if (httpVerbAnnotation == null) {
                continue;
            }
            candidates.add(m);
        }
        if (candidates.size() == 0) {
            return;
        }

        generateAsciiDoctorResourceTitle(cur.getName(), writer);

        final List<Annotation> classAnnotations = cur.getAnnotations();
        final Annotation classPathAnnotation = getPathAnnotation(classAnnotations);
        final String pathPrefix = classPathAnnotation.getValue();

        generateAsciiDoctorAPIHeader(writer);

        generateAsciiDoctorTableMarker(writer);
        generateAsciiDoctorTableHeaders(ImmutableList.<String>of("HTTP VERB", "URI", "Description"), writer);
        final List<Field> jsonFields = new LinkedList<Field>();
        for (MethodOrDecl m : candidates) {
            Annotation httpVerbAnnotation = getHttpMethodAnnotation(m.getAnnotations());
            generateAsciiDoctorAPIResource(cur, m, pathPrefix, httpVerbAnnotation.getName(), writer);
            final Field jsonField = getJsonFieldIfExists(m, httpVerbAnnotation.getName());
            if (jsonField != null) {
                jsonFields.add(jsonField);
            }
        }
        generateAsciiDoctorTableMarker(writer);



        for (Field f : jsonFields) {
            final String [] parts = f.getType().getBaseType().split("\\.");

            generateAsciiDoctorJsonHeader(parts[parts.length - 1], writer);

            generateAsciiDoctorTableMarker(writer);
            generateAsciiDoctorTableHeaders(ImmutableList.<String>of("Attribute", "Required", "Comments"), writer);

            generateAsciiDoctorJsonTable(f, allClasses, writer);

            generateAsciiDoctorTableMarker(writer);

        }

        writer.flush();
    }

    private void generateAsciiDoctorResourceTitle(final String resourceName, final Writer w) throws IOException {
        w.write("==== " + resourceName + "\n");
        w.write("\n");
    }

    private void generateAsciiDoctorAPIHeader(final Writer w) throws IOException {
        w.write("=====  APIs" + "\n");
        w.write("\n");
    }

    private void generateAsciiDoctorJsonHeader(final String resourceName, final Writer w) throws IOException {
        w.write("===== Json fields " + resourceName + "\n");
        w.write("\n");
    }

    private void generateAsciiDoctorTableMarker(final Writer w) throws IOException {
        w.write("|===" + "\n");
    }

    private void generateAsciiDoctorTableHeaders(final List<String> headers, final Writer w) throws IOException {
        for (int i = 0; i < headers.size(); i++) {
            w.write("|");
            w.write(headers.get(i));
            w.write(" ");
        }
        w.write("\n");
        w.write("\n");
    }

    private void generateAsciiDoctorTableEntry(final List<String> entries, final Writer w) throws IOException {
        for (int i = 0; i < entries.size(); i++) {
            w.write("|");
            w.write(entries.get(i));
            w.write("\n");
        }
        w.write("\n");
        w.write("\n");
    }


    private void generateAsciiDoctorAPIResource(ClassEnumOrInterface cur, final MethodOrDecl method, final String pathPrefix, final String verb, final Writer w) throws IOException, GeneratorException {

        final Annotation methodPathAnnotation = getPathAnnotation(method.getAnnotations());
        final String path = pathPrefix +
                (methodPathAnnotation != null ? methodPathAnnotation.getValue() : "");
        final String resolvedPath = resolvePath(cur, path, allClasses);
        final List<String> entries = ImmutableList.<String>builder()
                .add(verb)
                .add("+++" + resolvedPath + "+++")
                .add(createDescriptionFromMethodName(method.getName()))
                .build();

        generateAsciiDoctorTableEntry(entries, w);
    }


    private void generateAsciiDoctorJsonTable(final Field firstArgument, List<ClassEnumOrInterface> allClasses, final Writer w) throws IOException, GeneratorException {
        if (firstArgument == null) return;
        final ClassEnumOrInterface jsonClass = findClassEnumOrInterface(firstArgument.getType().getBaseType(), allClasses);
        final Constructor ctor = getJsonCreatorCTOR(jsonClass);

        final ImmutableList.Builder builder = ImmutableList.<String>builder();

        for (Field f : ctor.getOrderedArguments()) {
            final String attribute = getJsonPropertyAnnotationValue(jsonClass, f);
            builder.add(attribute);
            builder.add("true");
            builder.add("-");
        }

        generateAsciiDoctorTableEntry(builder.build(), w);

    }


    static String createDescriptionFromMethodName(final String methodName) {
        final String underscoreString = camelToUnderscore(methodName);
        final String[] parts = underscoreString.split("_");
        final StringBuffer tmp = new StringBuffer();
        boolean first = true;
        for (String cur : parts) {
            if (first) {
                tmp.append(Character.toUpperCase(cur.charAt(0)))
                        .append(cur.substring(1));
                first = false;
            } else {
                tmp.append(" ");
                tmp.append(cur);
            }
        }
        return tmp.toString();
    }

    private Field getJsonFieldIfExists(MethodOrDecl method, String verb) {
        final List<Field> arguments = method.getOrderedArguments();
        final Field firstArgument = arguments.size() >= 1 ? arguments.get(0) : null;
        if (verb.equals("GET") ||
                firstArgument == null ||
                firstArgument.getAnnotations().size() != 0) {
            return null;
        }
        return firstArgument;
    }

    private boolean containsPathAnnotation(final List<Annotation> annotations) {

        if (annotations == null ||
                annotations.size() == 0) {
            return false;
        }
        if (getPathAnnotation(annotations) != null) {
            return true;
        }
        return false;
    }

    private Annotation getPathAnnotation(List<Annotation> annotations) {
        for (final Annotation cur : annotations) {
            if (cur.getName().equals("Path")) {
                return cur;
            }
        }
        return null;
    }

    private Annotation getHttpMethodAnnotation(List<Annotation> annotations) {
        Collection<Annotation> filtered = Collections2.filter(annotations, new Predicate<Annotation>() {
            @Override
            public boolean apply(Annotation input) {
                final String name = input.getName();
                return (name.equals("GET") ||
                        name.equals("PUT") ||
                        name.equals("DELETE") ||
                        name.equals("POST"));
            }
        });
        return filtered.iterator().hasNext() ? filtered.iterator().next() : null;
    }


    //
    // TODO those methods should be moved up; already exists somewhere else
    //
    private ClassEnumOrInterface findClassEnumOrInterface(final String fullyQualifiedName, final List<ClassEnumOrInterface> allClasses) throws GeneratorException {
        for (final ClassEnumOrInterface cur : allClasses) {
            if (cur.getFullName().equals(fullyQualifiedName)) {
                return cur;
            }
        }
        throw new GeneratorException("Cannot find classEnumOrInterface " + fullyQualifiedName);
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

    private String getJsonPropertyAnnotationValue(final ClassEnumOrInterface obj, final Field f) throws GeneratorException {
        for (Annotation a : f.getAnnotations()) {
            if ("JsonProperty".equals(a.getName())) {
                return a.getValue();
            }
        }
        throw new GeneratorException("Could not find a JsonProperty annotation for object " + obj.getName() + " and field " + f.getName());
    }

    private String resolvePath(final ClassEnumOrInterface cur, final String pathAnnotation, final List<ClassEnumOrInterface> allClasses) throws GeneratorException {

        final StringBuilder tmp = new StringBuilder();
        final String[] parts = pathAnnotation.split("/");
        for (int i = 0; i < parts.length; i++) {

            Matcher m = PATTERN_ID.matcher(parts[i]);
            if (m.matches()) {
                tmp.append("/");
                tmp.append("{");
                tmp.append(m.group(1));
                tmp.append("}");
                continue;
            }
            m = PATTERN_PATH.matcher(parts[i]);
            if (m.matches()) {
                final StringBuilder tmp2 = new StringBuilder();
                resolveDeclaratorRecursively(cur, m.group(1), allClasses, tmp2);
                tmp.append(tmp2.toString());
                continue;
            }
            tmp.append(parts[i]);
        }
        return tmp.toString();
    }

    private void resolveDeclaratorRecursively(final ClassEnumOrInterface cur, final String toBeResolved, final List<ClassEnumOrInterface> allClasses, final StringBuilder tmp) throws GeneratorException {

        final String resolved = resolveDeclarator(cur, toBeResolved, allClasses);
        final String[] partsSlash = resolved.split("/");
        final String[] partsPlus = resolved.split("\\+");
        if (partsSlash.length == 1 && partsPlus.length == 1) {
            if (resolved.length() > 0 && !resolved.startsWith("/")) {
                tmp.append("/");
            }
            tmp.append(resolved);
            return;
        }
        boolean isSlash = (partsSlash.length > 1);
        final String[] target = isSlash ? partsSlash : partsPlus;
        for (String p : target) {
            Matcher m = PATTERN_PATH.matcher(p);
            if (!m.matches()) {
                continue;
            }

            resolveDeclaratorRecursively(cur, m.group(1), allClasses, tmp);
        }
    }

    private String resolveDeclarator(final ClassEnumOrInterface cur, final String toBeResolved, final List<ClassEnumOrInterface> allClasses) throws GeneratorException {

        String result = resolveFromDecls(cur.getMethodOrDecls(), toBeResolved);
        if (result != null) {
            return result;
        }

        if (cur.getSuperBaseClass() != null) {
            final ClassEnumOrInterface baseClass = findClassEnumOrInterface(cur.getSuperBaseClass(), allClasses);
            result = resolveDeclarator(baseClass, toBeResolved, allClasses);
            if (result != null) {
                return result;
            }
        }

        if (cur.getSuperInterfaces() != null) {
            for (String ifce : cur.getSuperInterfaces()) {
                final ClassEnumOrInterface curInterface = findClassEnumOrInterface(ifce, allClasses);
                result = resolveDeclarator(curInterface, toBeResolved, allClasses);
                if (result != null) {
                    return result;
                }
            }
        }
        return toBeResolved;
    }

    private String resolveFromDecls(final List<MethodOrDecl> decls, final String toBeResolved) {
        for (MethodOrDecl cur : decls) {
            if (cur.getName().equals(toBeResolved)) {
                return cur.getInitializerValue();
            }
        }
        return null;
    }
}
