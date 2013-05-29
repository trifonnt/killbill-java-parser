package com.ning.killbill.com.ning.killbill.args;

import java.net.URI;
import java.net.URISyntaxException;

import com.beust.jcommander.converters.BaseConverter;

public class URIConverter extends BaseConverter<URI> {

    public URIConverter(String optionName) {
        super(optionName);
    }

    @Override
    public URI convert(String value)  {
        try {
            return new URI(value);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
