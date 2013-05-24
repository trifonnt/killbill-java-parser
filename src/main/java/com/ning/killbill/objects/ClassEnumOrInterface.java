package com.ning.killbill.objects;

import java.util.ArrayList;
import java.util.List;

public final class ClassEnumOrInterface {


    public enum ClassEnumOrInterfaceType {
        CLASS,
        INTERFACE,
        ENUM
    };

    private final String name;
    private final ClassEnumOrInterfaceType type;
    private final List<String> superInterfaces;
    private String superBaseClass;


    private final List<String> enumValues;
    private final List<Method> methods;

    private final List<Field> fields;
    private final List<Constructor> ctors;


    public ClassEnumOrInterface(final String name, final ClassEnumOrInterfaceType type) {
        this.name = name;
        this.type = type;
        this.methods = new ArrayList<Method>();
        this.ctors = new ArrayList<Constructor>();
        this.superInterfaces = new ArrayList<String>();
        this.superBaseClass = null;
        this.enumValues = new ArrayList<String>();
        fields = new ArrayList<Field>();
    }

    public void addConstructor(Constructor ctor) {
        ctors.add(ctor);
    }

    public void addMethod(Method method) {
        methods.add(method);
    }

    public void addField(Field field) {
        fields.add(field);
    }

    public void addSuperInterface(final String ifce) {
        superInterfaces.add(ifce);
    }

    public void addSuperClass(final String claz) {
        superBaseClass = claz;
    }

    public void addEnumValue(String value) {
        enumValues.add(value);
    }

    public String getName() {
        return name;
    }

    public boolean isInterface() {
        return type == ClassEnumOrInterfaceType.INTERFACE;
    }

    public boolean isClass() {
        return type == ClassEnumOrInterfaceType.CLASS;
    }

    public boolean isEnum() {
        return type == ClassEnumOrInterfaceType.ENUM;
    }

    public List<String> getEnumValues() {
        return enumValues;
    }

    public List<Constructor> getCtors() {
        return ctors;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public List<Field> getFields() {
        return fields;
    }

    public List<String> getSuperInterfaces() {
        return superInterfaces;
    }

    public String getSuperBaseClass() {
        return superBaseClass;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClassEnumOrInterface{");
        sb.append("name='").append(name).append('\'');
        sb.append(", type=").append(type);
        sb.append(", superInterfaces=").append(superInterfaces);
        sb.append(", superBaseClass='").append(superBaseClass).append('\'');
        sb.append(", enumValues=").append(enumValues);
        sb.append(", methods=").append(methods);
        sb.append(", fields=").append(fields);
        sb.append(", ctors=").append(ctors);
        sb.append('}');
        return sb.toString();
    }
}
