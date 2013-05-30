package com.ning.killbill.com.ning.killbill.args;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;

public class KillbillParserArgs {

    public final static Logger logger = LoggerFactory.getLogger(KillbillParserArgs.class);

    @Parameter(names= {"-d", "--debug"}, description = "Turn on debug traces")
    private boolean debug = false;

    @Parameter(names = {"-i", "--input"}, description = "The input file/jar/directory for the java sources to prase", converter = URIConverter.class, required = true)
    private URI input;

    @Parameter(names = {"-o", "--output"}, description ="The output directory for the objects created", converter = FileConverter.class, required = true)
    private File outputDir;

    @Parameter(names = {"-p", "--packageFilter"}, variableArity = true, description ="A optional filter list of java packages")
    private List<String> packagesFilter = new ArrayList<String>();

    @Parameter(names = {"-l", "--language"}, description ="The target language for the classes created", required = true)
    private LANGUAGE language;


    public enum LANGUAGE {
        RUBY,
        JRUBY,
        PHP
    }

    public URI getInput() {
        return input;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public List<String> getPackagesFilter() {
        return packagesFilter;
    }

    public LANGUAGE getLanguage() {
        return language;
    }


    public boolean isInputJar() {
        return "jar".equals(input.getScheme());
    }

    public boolean isInputFile() {
        if (! "file".equals(input.getScheme())) {
            return false;
        }
        File f = getInputFile();
        return f.isFile();
    }

    public boolean isInputDirectory() {
        if (! "file".equals(input.getScheme())) {
            return false;
        }
        File f = getInputFile();
        return f.isDirectory();
    }

    public File getInputFile() {
        File f = new File(input.getPath());
        if (! f.exists()) {
            throw new IllegalArgumentException(input + " is not  a valid file/directory");
        }
        return f;
    }

}
