package com.ning.killbill.objects;

import java.util.List;

public final class Method extends MethodOrCtor {

    private final String returnValueType;

    public Method(final String name, final String returnValueType, final boolean isAbstract, final List<Annotation> annotations) {
        super(name, isAbstract, annotations);
        this.returnValueType = returnValueType;
    }

    public boolean isGetter() {
        return (name.startsWith("get") || name.startsWith("is"));
    }

    public String getReturnValueType() {
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
