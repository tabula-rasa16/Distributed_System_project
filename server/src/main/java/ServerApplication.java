import com.RPC.*;
import com.flightinfosys.RPCMethod.RPCMethodFactory;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.List;


public class ServerApplication {
    private static final int SERVER_PORT = 9876;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {




        try (DatagramSocket serverSocket = new DatagramSocket(SERVER_PORT)) {
            System.out.println("RPC Server is running on port " + SERVER_PORT);

            byte[] receiveBuffer = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                serverSocket.receive(receivePacket);

                try {
                    // 反序列化请求
                    RPCRequest request = Serializer.unmarshallRequest(receivePacket.getData(), receivePacket.getLength());
                    System.out.println("Received request: method=" + request.getMethodName() +
                            ", paramCount=" + request.getParameters().size());

                    // 获取对应的RPC方法
                    IRPCMethod method = RPCMethodFactory.getMethod(request.getMethodName());
                    RPCResponse response;
                    if (method == null) {
                        response = new RPCResponse((byte) 1, null, "Unknown method: " + request.getMethodName());
                    } else {
                        // 执行方法
                        List<Parameter> results;
                        try {
                            results = method.execute(request.getParameters());
                            response = new RPCResponse((byte) 0, results, null);
                        } catch (Exception e) {
                            response = new RPCResponse((byte) 1, null, e.getMessage());
                        }
                    }

                    // 序列化响应
                    byte[] sendData = Serializer.marshallResponse(response);

                    // 发送响应
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                            receivePacket.getAddress(), receivePacket.getPort());
                    serverSocket.send(sendPacket);

                    if (response.getStatus() == 0) {
                        System.out.println("Sent response: status=" + response.getStatus() +
                                ", resultCount=" + response.getResults().size());
                    } else {
                        System.out.println("Sent response: status=" + response.getStatus() +
                                ", errorMessage=" + response.getErrorMessage());
                    }
                } catch (Exception e) {
                    System.err.println("Failed to process request: " + e.getMessage());

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
