package com.ning.killbill.objects;

import java.util.List;

public final class Argument {


    private final String name;
    private final String type;

    private final List<Annotation> annotations;

    public Argument(final String name, final String type, final List<Annotation> annotations) {
        this.name = name;
        this.type = type;
        this.annotations = annotations;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Argument{");
        sb.append("name='").append(name).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", annotations=").append(annotations);
        sb.append('}');
        return sb.toString();
    }
}
