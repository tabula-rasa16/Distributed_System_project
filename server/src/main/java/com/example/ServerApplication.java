import com.RPC.*;
import com.flightinfosys.RPCMethod.RPCMethodFactory;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class ServerApplication {


    private static final int SERVER_PORT = 9876;
    private static final int BUFFER_SIZE = 1024;
    private static final long HISTORY_TIMEOUT_MS = 300000; // 历史记录保留5分钟

    // 存储请求ID到响应的映射
    private static final Map<Integer, RPCResponse> requestHistory = new LinkedHashMap<Integer, RPCResponse>() {
        protected boolean removeEldestEntry(Map.Entry<Integer, RPCResponse> eldest) {
            return size() > 100; // 最多保留100条记录
        }
    };

    public static void main(String[] args) {
        // 读取调用语义参数
        if (args.length < 1) {
            System.err.println("Usage: java RPCServer <semantics>");
            System.err.println("semantics: at-most-once | at-least-once");
            return;
        }
        String semantics = args[0].toLowerCase();
        if (!semantics.equals("at-most-once") && !semantics.equals("at-least-once")) {
            System.err.println("Invalid semantics. Use 'at-most-once' or 'at-least-once'.");
            return;
        }

        try (DatagramSocket serverSocket = new DatagramSocket(SERVER_PORT)) {
            System.out.println("RPC Server is running on port " + SERVER_PORT + " with semantics: " + semantics);

            byte[] receiveBuffer = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                serverSocket.receive(receivePacket);

                // 处理请求
                handleRequest(receivePacket, serverSocket, semantics);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleRequest(DatagramPacket receivePacket, DatagramSocket serverSocket, String semantics) {
        try {
            // 反序列化请求
            RPCRequest request = Serializer.unmarshallRequest(receivePacket.getData(), receivePacket.getLength());
            System.out.println("Received request: method=" + request.getMethodName() +
                    ", requestId=" + request.getRequestId() +
                    ", paramCount=" + request.getParameters().size());

            RPCResponse response;

            if (semantics.equals("at-most-once")) {
                // 检查请求是否已处理
                if (requestHistory.containsKey(request.getRequestId())) {
                    // 发送之前的响应
                    response = requestHistory.get(request.getRequestId());
                    System.out.println("Duplicate request detected. Resending previous response.");
                } else {
                    // 处理请求
                    IRPCMethod method = RPCMethodFactory.getMethod(request.getMethodName());
                    if (method == null) {
                        response = new RPCResponse(request.getRequestId(), (byte) 1, null, "Unknown method: " + request.getMethodName());
                    } else {
                        try {
                            List<Parameter> results = method.execute(request.getParameters());
                            response = new RPCResponse(request.getRequestId(), (byte) 0, results, null);
                        } catch (Exception e) {
                            response = new RPCResponse(request.getRequestId(), (byte) 1, null, e.getMessage());
                        }
                    }
                    // 记录请求和响应
                    requestHistory.put(request.getRequestId(), response);
                }
            } else { // at-least-once
                // 在at-least-once语义下，每个请求至少被执行一次
                IRPCMethod method = RPCMethodFactory.getMethod(request.getMethodName());
                if (method == null) {
                    response = new RPCResponse(request.getRequestId(), (byte) 1, null, "Unknown method: " + request.getMethodName());
                } else {
                    try {
                        List<Parameter> results = method.execute(request.getParameters());
                        response = new RPCResponse(request.getRequestId(), (byte) 0, results, null);
                    } catch (Exception e) {
                        response = new RPCResponse(request.getRequestId(), (byte) 1, null, e.getMessage());
                    }
                }
                // 记录请求和响应
                requestHistory.put(request.getRequestId(), response);
            }

            // 序列化响应
            byte[] sendData = Serializer.marshallResponse(response);

            // 发送响应
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                    receivePacket.getAddress(), receivePacket.getPort());
            serverSocket.send(sendPacket);

            if (response.getStatus() == 0) {
                System.out.println("Sent response: requestId=" + response.getRequestId() +
                        ", status=" + response.getStatus() +
                        ", resultCount=" + response.getResults().size());
            } else {
                System.out.println("Sent response: requestId=" + response.getRequestId() +
                        ", status=" + response.getStatus() +
                        ", errorMessage=" + response.getErrorMessage());
            }

        } catch (Exception e) {
            System.err.println("Failed to process request: " + e.getMessage());
        }
    }





//    private static final int SERVER_PORT = 9876;
//    private static final int BUFFER_SIZE = 1024;
//
//    public static void main(String[] args) {
//
//
//
//
//        try (DatagramSocket serverSocket = new DatagramSocket(SERVER_PORT)) {
//            System.out.println("RPC Server is running on port " + SERVER_PORT);
//
//            byte[] receiveBuffer = new byte[BUFFER_SIZE];
//
//            while (true) {
//                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
//                serverSocket.receive(receivePacket);
//
//                try {
//                    // 反序列化请求
//                    RPCRequest request = Serializer.unmarshallRequest(receivePacket.getData(), receivePacket.getLength());
//                    System.out.println("Received request: method=" + request.getMethodName() +
//                            ", paramCount=" + request.getParameters().size());
//
//                    // 获取对应的RPC方法
//                    IRPCMethod method = RPCMethodFactory.getMethod(request.getMethodName());
//                    RPCResponse response;
//                    if (method == null) {
//                        response = new RPCResponse((byte) 1, null, "Unknown method: " + request.getMethodName());
//                    } else {
//                        // 执行方法
//                        List<Parameter> results;
//                        try {
//                            results = method.execute(request.getParameters());
//                            response = new RPCResponse((byte) 0, results, null);
//                        } catch (Exception e) {
//                            response = new RPCResponse((byte) 1, null, e.getMessage());
//                        }
//                    }
//
//                    // 序列化响应
//                    byte[] sendData = Serializer.marshallResponse(response);
//
//                    // 发送响应
//                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
//                            receivePacket.getAddress(), receivePacket.getPort());
//                    serverSocket.send(sendPacket);
//
//                    if (response.getStatus() == 0) {
//                        System.out.println("Sent response: status=" + response.getStatus() +
//                                ", resultCount=" + response.getResults().size());
//                    } else {
//                        System.out.println("Sent response: status=" + response.getStatus() +
//                                ", errorMessage=" + response.getErrorMessage());
//                    }
//                } catch (Exception e) {
//                    System.err.println("Failed to process request: " + e.getMessage());
//
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
