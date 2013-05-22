package main.java.com.ning.killbill.objects;

public final class Argument {

    private final String name;
    private final String type;

    Argument(final String name, final String type) {
        this.name = name;
        this.type = type;
    }

    private String getName() {
        return name;
    }

    private String getType() {
        return type;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Argument{");
        sb.append("name='").append(name).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
