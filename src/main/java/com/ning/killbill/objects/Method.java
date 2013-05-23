package com.ning.killbill.objects;

public final class Method extends MethodOrCtor {


    public Method(final String name) {
        super(name);
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
