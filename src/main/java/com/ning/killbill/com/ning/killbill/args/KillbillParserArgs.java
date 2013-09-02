package com.ning.killbill.com.ning.killbill.args;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

public class KillbillParserArgs {

    public final static Logger logger = LoggerFactory.getLogger(KillbillParserArgs.class);

    @Parameter(names= {"-d", "--debug"}, description = "Turn on debug traces")
    private boolean debug = false;

    @Parameter(names = {"-i", "--input"}, description = "The input file/jar/directory for the java sources to prase", variableArity = true, required = true)
    private List<String> input = new ArrayList<String>();

    @Parameter(names = {"-o", "--output"}, description ="The output directory for the objects created", converter = FileConverter.class, required = true)
    private File outputDir;

    @Parameter(names = {"-p", "--packageParserIncludeFilter"}, variableArity = true, description ="A optional filter list of java packages for the parser")
    private List<String> packagesParserFilter = new ArrayList<String>();

    @Parameter(names = {"-q", "--packageGeneratorIncludeFilter"}, variableArity = true, description ="A optional filter list of java packages for the parser")
    private List<String> packagesGeneratorFilter = new ArrayList<String>();

    /* That option should probably go; needed at this point because there are a few classes that the generator can't produce because our API needs some work */
    @Parameter(names = {"-x", "--classGeneratorExcludeFilter"}, variableArity = true, description ="A optional filter list of java packages for the parser")
    private List<String> classGeneratorExcludes = new ArrayList<String>();

    @Parameter(names = "--help", help = true)
    private boolean help;

    @Parameter(names = {"-t", "--target"}, description ="The target generator", required = true)
    private TARGET_GENERATOR targetGenerator;

    @Parameter(names = {"-m", "--mode"}, description ="The generator mode")
    private GENERATOR_MODE mode = GENERATOR_MODE.NON_APPLICABLE;


    public enum TARGET_GENERATOR {
        JRUBY_PLUGIN,
        RUBY_CLIENT_API,
        PHP_CLIENT_API,
        JERSEY_DOC
    }

    public enum GENERATOR_MODE {
        NON_APPLICABLE,
        JRUBY_API,
        JRUBY_PLUGIN_API
    }

    public List<URI> getInput() {
        return ImmutableList.<URI>copyOf(Collections2.transform(input, new Function<String, URI>() {
            final URIConverter uriConverter = new URIConverter("-i");

            @Nullable
            @Override
            public URI apply(@Nullable final String input) {
                return uriConverter.convert(input);
            }
        }));
    }

    public File getOutputDir() {
        return outputDir;
    }

    public List<String> getPackagesParserFilter() {
        return packagesParserFilter;
    }

    public List<String> getPackagesGeneratorFilter() {
        return packagesGeneratorFilter;
    }

    public TARGET_GENERATOR getTargetGenerator() {
        return targetGenerator;
    }

    public GENERATOR_MODE getMode() {
        return mode;
    }

    public List<String> getClassGeneratorExcludes() {
        return classGeneratorExcludes;
    }

    public boolean isInputJar(final URI input) {
        return "jar".equals(input.getScheme());
    }

    public boolean isHelp() {
        return help;
    }

    public static boolean isInputFile(final URI input) {
        if (! "file".equals(input.getScheme())) {
            return false;
        }
        File f = getInputFile(input);
        return f.isFile();
    }

    public static boolean isInputDirectory(final URI input) {
        if (! "file".equals(input.getScheme())) {
            return false;
        }
        File f = getInputFile(input);
        return f.isDirectory();
    }

    public static File getInputFile(final URI input) {
        File f = new File(input.getPath());
        if (! f.exists()) {
            throw new IllegalArgumentException(input + " is not  a valid file/directory");
        }
        return f;
    }

}
