package com.ning.killbill.generators;

import java.io.IOException;

import com.ning.killbill.com.ning.killbill.args.KillbillParserArgs;
import com.ning.killbill.generators.ruby.RubyGenerator;

public class LanguageGenerator {

    public LanguageGenerator() {
    }

    public void generate(final KillbillParserArgs args) throws GeneratorException {

        final Generator gen;
        switch (args.getLanguage()) {
            case RUBY:
                gen = new RubyGenerator();
                break;
            case JRUBY:
                gen = null;
                break;
            case PHP:
                gen = null;
                break;
            default:
                throw new RuntimeException("Unsupported language " + args.getLanguage());
        }
        gen.generate(args);
    }
}
