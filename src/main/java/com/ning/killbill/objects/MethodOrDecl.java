package com.ning.killbill.objects;

import java.util.List;

public final class MethodOrDecl extends MethodCtorOrDecl {

    private final Type returnValueType;

    public MethodOrDecl(final String name, final Type returnValueType, final boolean isAbstract, final List<Annotation> annotations) {
        super(name, isAbstract, annotations);
        this.returnValueType = returnValueType;
    }

    public boolean isGetter() {
        return (name.startsWith("get") || name.startsWith("is"));
    }

    public Type getReturnValueType() {
        return returnValueType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("METHOD{");
        sb.append("name='").append(name).append('\'');
        sb.append(", returnValueType='").append(returnValueType).append('\'');
        sb.append(", orderedArguments=").append(orderedArguments);
        sb.append(", annotations=").append(annotations);
        sb.append('}');
        return sb.toString();
    }
}
