package com.ning.killbill.generators;

import java.io.IOException;

import com.ning.killbill.com.ning.killbill.args.KillbillParserArgs;

public interface Generator {

    public void generate(final KillbillParserArgs args) throws GeneratorException;
}
