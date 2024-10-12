package RPC;


public class RPCResponse {
    private byte status; // 0: success, 1: error
    private Parameter result; // 仅在成功时使用
    private String errorMessage; // 仅在出错时使用

    public RPCResponse(byte status, Parameter result, String errorMessage) {
        this.status = status;
        this.result = result;
        this.errorMessage = errorMessage;
    }

    public byte getStatus() {
        return status;
    }

    public Parameter getResult() {
        return result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
