package com.RPC;

// RPCRequest.java
import java.util.List;

public class RPCRequest {
    private int requestId;
    private String methodName;
    private List<Parameter> parameters;

    public RPCRequest(int requestId, String methodName, List<Parameter> parameters) {
        this.requestId = requestId;
        this.methodName = methodName;
        this.parameters = parameters;
    }

    public int getRequestId() { return requestId; }

    public String getMethodName() {
        return methodName;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }
}




