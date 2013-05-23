package com.ning.killbill.objects;

import java.util.ArrayList;
import java.util.List;

public final class Method {

    private final String name;

    private final List<Argument> orderedArguments;

    public Method(final String name) {
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

    public boolean isGetter() {
        return (name.startsWith("get") || name.startsWith("is"));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Method{");
        sb.append("name='").append(name).append('\'');
        sb.append(", orderedArguments=").append(orderedArguments);
        sb.append('}');
        return sb.toString();
    }
}
