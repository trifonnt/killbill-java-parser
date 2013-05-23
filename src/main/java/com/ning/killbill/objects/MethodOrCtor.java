package com.ning.killbill.objects;

import java.util.ArrayList;
import java.util.List;

public abstract class MethodOrCtor {

    protected final String name;

    protected final List<Argument> orderedArguments;

    public MethodOrCtor(final String name) {
        this.name = name;
        this.orderedArguments = new ArrayList<Argument>();
    }

    public void addArgument(Argument arg) {
        orderedArguments.add(arg);
    }

    public String getName() {
        return name;
    }

    public List<Argument> getOrderedArguments() {
        return orderedArguments;
    }
}
