package com.ning.killbill.generators.doc;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
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
                    generateClass(cur, allClasses, w);
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
        final File doc = new File(outputDir, "jersey.doc");
        doc.createNewFile();
        return doc;
    }

    private void generateClass(ClassEnumOrInterface cur, List<ClassEnumOrInterface> allClasses, Writer writer) throws IOException, GeneratorException {
        writer.write("****************************** " + cur.getName() + " ******************************\n");
        final List<Annotation> classAnnotations = cur.getAnnotations();
        final Annotation classPathAnnotation = getPathAnnotation(classAnnotations);
        final String pathPrefix = classPathAnnotation.getValue();

        for (MethodOrDecl m : cur.getMethodOrDecls()) {
            Annotation httpVerbAnnotation = getHttpMethodAnnotation(m.getAnnotations());
            if (httpVerbAnnotation == null) {
                continue;
            }
            generateAPISignature(cur, m, pathPrefix, httpVerbAnnotation.getName(), writer);
            generateJsonIfRequired(m, allClasses, httpVerbAnnotation.getName(), writer);
        }
    }


    private void generateAPISignature(ClassEnumOrInterface cur, final MethodOrDecl method, final String pathPrefix, final String verb, final Writer w) throws IOException, GeneratorException {

        final Annotation methodPathAnnotation = getPathAnnotation(method.getAnnotations());
        final String path = pathPrefix +
                (methodPathAnnotation != null ? methodPathAnnotation.getValue() : "");
        final String resolvedPath = resolvePath(cur, path, allClasses);
        w.write(verb);
        w.write(" ");
        w.write(resolvedPath);
        w.write("\n");
        w.write("\n");
        w.flush();
    }

    private void generateJsonIfRequired(final MethodOrDecl method, List<ClassEnumOrInterface> allClasses, final String verb, final Writer w) throws IOException, GeneratorException {
        final List<Field> arguments = method.getOrderedArguments();
        final Field firstArgument = arguments.size() >= 1 ? arguments.get(0) : null;
        if (verb.equals("GET") ||
                firstArgument == null ||
                firstArgument.getAnnotations().size() != 0) {
            return;
        }
        final ClassEnumOrInterface jsonClass = findClassEnumOrInterface(firstArgument.getType().getBaseType(), allClasses);
        final Constructor ctor = getJsonCreatorCTOR(jsonClass);

        w.write("--------  Json Body:  --------\n");
        for (Field f : ctor.getOrderedArguments()) {
            final String attribute = getJsonPropertyAnnotationValue(jsonClass, f);
            w.write("\t" + attribute + "\n");
        }
        w.flush();
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
            if (resolved.length() > 0 && ! resolved.startsWith("/")) {
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
