package com.ning.killbill.generators;

import com.ning.killbill.com.ning.killbill.args.KillbillParserArgs;
import com.ning.killbill.generators.ruby.JRubyPluginGenerator;
import com.ning.killbill.generators.ruby.RubyClientApiGenerator;

public class DistpatchGenerator {

    public DistpatchGenerator() {
    }

    public void generate(final KillbillParserArgs args) throws GeneratorException {

        final Generator gen;
        switch (args.getTargetGenerator()) {
            case RUBY_CLIENT_API:
                gen = new RubyClientApiGenerator();
                break;
            case JRUBY_PLUGIN:
                gen = new JRubyPluginGenerator();
                break;
            case PHP_CLIENT_API:
                gen = null;
                break;
            default:
                throw new RuntimeException("Unsupported language " + args.getTargetGenerator());
        }
        gen.generate(args);
    }
}
