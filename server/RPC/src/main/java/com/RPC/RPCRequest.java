package com.RPC;

// RPCRequest.java
import java.util.List;

public class RPCRequest {
    private String methodName;
    private List<Parameter> parameters;

    public RPCRequest(String methodName, List<Parameter> parameters) {
        this.methodName = methodName;
        this.parameters = parameters;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }
}




