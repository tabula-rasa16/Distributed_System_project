package com.RPC;


import java.util.List;

public class RPCResponse {
    private int requestId;
    private byte status; // 0: success, 1: error
    private List<Parameter> results; // 仅在成功时使用
    private String errorMessage; // 仅在出错时使用

    public RPCResponse(int requestId, byte status, List<Parameter> results, String errorMessage) {
        this.requestId = requestId;
        this.status = status;
        this.results = results;
        this.errorMessage = errorMessage;
    }

    public int getRequestId() {
        return requestId;
    }

    public byte getStatus() {
        return status;
    }

    public List<Parameter> getResults() {
        return results;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
