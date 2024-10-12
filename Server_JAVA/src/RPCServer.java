// RPCServer.java
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class RPCServer {
    private static final int SERVER_PORT = 9876;
    private static final int BUFFER_SIZE = 1024;

    // RPC请求结构
    static class RPCRequest {
        String methodName;
        int param1;
        int param2;
    }

    // RPC响应结构
    static class RPCResponse {
        byte status; // 0: success, 1: error
        int result; // 仅在成功时使用
        String errorMessage; // 仅在出错时使用
    }

    // Unmarshalling: 从字节数组反序列化到RPCRequest
    public static RPCRequest unmarshallRequest(byte[] data, int length) {
        RPCRequest request = new RPCRequest();
        int index = 0;

        // 方法名长度
        int methodNameLen = data[index] & 0xFF;
        index += 1;

        // 方法名
        request.methodName = new String(data, index, methodNameLen);
        index += methodNameLen;

        // 参数1（网络字节序）
        ByteBuffer buffer = ByteBuffer.wrap(data, index, 4);
        request.param1 = buffer.getInt();
        index += 4;

        // 参数2（网络字节序）
        buffer = ByteBuffer.wrap(data, index, 4);
        request.param2 = buffer.getInt();
        index += 4;

        return request;
    }

    // Marshalling: 从RPCResponse序列化到字节数组
    public static byte[] marshallResponse(RPCResponse response) {
        ByteBuffer buffer;
        if (response.status == 0) { // success
            buffer = ByteBuffer.allocate(1 + 4);
            buffer.put(response.status);
            buffer.putInt(response.result);
        } else { // error
            byte[] errorMsgBytes = response.errorMessage.getBytes();
            buffer = ByteBuffer.allocate(1 + 1 + errorMsgBytes.length);
            buffer.put(response.status);
            buffer.put((byte) errorMsgBytes.length);
            buffer.put(errorMsgBytes);
        }
        return buffer.array();
    }

    // 处理RPC请求
    public static RPCResponse handleRequest(RPCRequest request) {
        RPCResponse response = new RPCResponse();

        if ("add".equalsIgnoreCase(request.methodName)) {
            response.status = 0;
            response.result = request.param1 + request.param2;
        } else {
            response.status = 1;
            response.errorMessage = "Unknown method: " + request.methodName;
        }

        return response;
    }

    public static void main(String[] args) {
        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(SERVER_PORT);
            System.out.println("RPC Server is running on port " + SERVER_PORT);

            byte[] receiveBuffer = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                serverSocket.receive(receivePacket);

                // 反序列化请求
                RPCRequest request = unmarshallRequest(receivePacket.getData(), receivePacket.getLength());
                System.out.println("Received request: method=" + request.methodName + ", param1=" + request.param1 + ", param2=" + request.param2);

                // 处理请求
                RPCResponse response = handleRequest(request);

                // 序列化响应
                byte[] sendData = marshallResponse(response);

                // 发送响应
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                serverSocket.send(sendPacket);

                System.out.println("Sent response: status=" + response.status + (response.status == 0 ? ", result=" + response.result : ", errorMessage=" + response.errorMessage));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }
}
