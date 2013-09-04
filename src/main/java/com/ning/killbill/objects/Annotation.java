package com.ning.killbill.objects;

// Simple annotation assuming one or 0 value string
public class Annotation {

    private final String name;
    private String value;

/*
    public Annotation(final String name, final String value) {
        this.name = name;
        this.value = value;
    }
  */

    public Annotation(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Annotation{");
        sb.append("name='").append(name).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
