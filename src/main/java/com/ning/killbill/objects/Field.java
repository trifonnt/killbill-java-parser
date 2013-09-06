package com.ning.killbill.objects;

import java.util.List;

public class Field {


    protected final String name;
    protected final Type type;

    protected final List<Annotation> annotations;

    public Field(final String name, final Type type, final List<Annotation> annotations) {
        this.name = name;
        this.type = type;
        this.annotations = annotations;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }


    public List<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Field{");
        sb.append("name='").append(name).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", annotations=").append(annotations);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Field field = (Field) o;

        if (name != null ? !name.equals(field.name) : field.name != null) return false;
        if (type != null ? !type.getBaseType().equals(field.type.getBaseType()) : field.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.getBaseType().hashCode() : 0);
        return result;
    }
}
