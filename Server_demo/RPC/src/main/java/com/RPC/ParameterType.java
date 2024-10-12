package com.RPC;

public enum ParameterType {
    INTEGER(1),
    STRING(2);
    // 可以根据需要添加更多类型

    private final int code;

    ParameterType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ParameterType fromCode(int code) {
        for (ParameterType type : ParameterType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ParameterType code: " + code);
    }
}
