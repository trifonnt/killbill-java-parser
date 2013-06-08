package com.ning.killbill.objects;

public class Type {

    private final String baseType;
    private final String genericType;

    public Type(final String baseType, final String genericType) {
        this.baseType = baseType;
        this.genericType = genericType;
    }

    public Type(final String baseType) {
        this(baseType, null);
    }

    public String getBaseType() {
        return baseType;
    }

    public String getGenericType() {
        return genericType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Type{");
        sb.append("baseType='").append(baseType).append('\'');
        sb.append(", genericType='").append(genericType).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
