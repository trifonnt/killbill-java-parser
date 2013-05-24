package com.ning.killbill.objects;

public final class Method extends MethodOrCtor {


    public Method(final String name) {
        super(name);
    }

    public boolean isGetter() {
        return (name.startsWith("get") || name.startsWith("is"));
    }
}
