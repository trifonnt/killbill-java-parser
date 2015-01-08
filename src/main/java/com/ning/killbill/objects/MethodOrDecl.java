package com.ning.killbill.objects;

import java.util.List;

import javax.annotation.Nullable;

public class MethodOrDecl extends MethodCtorOrDecl {

    private final Type returnValueType;
    private final Boolean hasParameters;

    public MethodOrDecl(final String name, final Type returnValueType, final boolean isAbstract, final List<Annotation> annotations) {
        this(name, returnValueType, isAbstract, annotations, null);
    }

    public MethodOrDecl(final String name, final Type returnValueType, final boolean isAbstract, final List<Annotation> annotations, @Nullable final Boolean hasParameters) {
        super(name, isAbstract, annotations);
        this.returnValueType = returnValueType;
        this.hasParameters = hasParameters;
    }

    public boolean isGetter() {
        return (name.startsWith("get") || name.startsWith("is")) && (hasParameters == null || !hasParameters);
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
