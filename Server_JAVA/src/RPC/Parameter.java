package RPC;

public class Parameter {
    private ParameterType type;
    private Object value;

    public Parameter(ParameterType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public ParameterType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }
}
