package com.ning.killbill.objects;

import java.util.ArrayList;
import java.util.List;

public abstract class MethodOrCtor {

    protected final String name;
    protected final List<Field> orderedArguments;


    protected final List<Annotation> annotations;

    public MethodOrCtor(final String name, final List<Annotation> annotations) {
        this.name = name;
        this.annotations = annotations;
        this.orderedArguments = new ArrayList<Field>();
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

    public List<Annotation> getAnnotations() {
        return annotations;
    }
}
