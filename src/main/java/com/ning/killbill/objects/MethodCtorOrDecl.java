package com.ning.killbill.objects;

import java.util.ArrayList;
import java.util.List;

public abstract class MethodCtorOrDecl {

    protected final String name;
    protected final List<Field> orderedArguments;

    protected final boolean isAbstract;

    protected final List<Annotation> annotations;

    protected final List<String> exceptions;

    protected String initializerValue;

    public MethodCtorOrDecl(final String name, final boolean isAbstract, final List<Annotation> annotations) {
        this.name = name;
        this.annotations = annotations;
        this.orderedArguments = new ArrayList<Field>();
        this.exceptions = new ArrayList<String>();
        this.isAbstract = isAbstract;
    }

    public void addException(String exception) {
        exceptions.add(exception);
    }


    public void addArgument(Field arg) {
        orderedArguments.add(arg);
    }

    public String getName() {
        return name;
    }

    public List<Field> getOrderedArguments() {
        return orderedArguments;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public List<String> getExceptions() {
        return exceptions;
    }

    public String getInitializerValue() {
        return initializerValue;
    }

    public void setInitializerValue(String initializerValue) {
        this.initializerValue = initializerValue;
    }
}
