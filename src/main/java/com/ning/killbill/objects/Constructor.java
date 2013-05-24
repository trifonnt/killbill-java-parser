package com.ning.killbill.objects;

import java.util.List;

public class Constructor extends MethodOrCtor {

    public Constructor(final String name, final List<Annotation> annotations) {
        super(name, annotations);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CTOR{");
        sb.append("name='").append(name).append('\'');
        sb.append(", orderedArguments=").append(orderedArguments);
        sb.append(", annotations=").append(annotations);
        sb.append('}');
        return sb.toString();
    }
}
