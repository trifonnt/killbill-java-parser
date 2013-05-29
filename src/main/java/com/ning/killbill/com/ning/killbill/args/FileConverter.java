package com.ning.killbill.com.ning.killbill.args;

import java.io.File;

import com.beust.jcommander.converters.BaseConverter;

public class FileConverter extends BaseConverter<File> {

    public FileConverter(String optionName) {
        super(optionName);
    }

    @Override
    public File convert(String value) {
        return new File(value);
    }
}
