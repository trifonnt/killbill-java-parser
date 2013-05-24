package com.ning.killbill.objects;

import java.util.List;

public final class Method extends MethodOrCtor {


    public Method(final String name, final List<Annotation> annotations) {
        super(name, annotations);
    }

    public boolean isGetter() {
        return (name.startsWith("get") || name.startsWith("is"));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("METHOD{");
        sb.append("name='").append(name).append('\'');
        sb.append(", orderedArguments=").append(orderedArguments);
        sb.append(", annotations=").append(annotations);
        sb.append('}');
        return sb.toString();
    }
}
