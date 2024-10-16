package com.RPC;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Serializer {

    // 反序列化请求
    public static RPCRequest unmarshallRequest(byte[] data, int length) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(data, 0, length);

        // 请求标识符
        if (buffer.remaining() < 4) throw new Exception("Invalid request: Missing request ID.");
        int requestId = buffer.getInt();

        // 方法名长度
        if (buffer.remaining() < 1) throw new Exception("Invalid request: Missing method name length.");
        int methodNameLen = Byte.toUnsignedInt(buffer.get());

        // 方法名
        if (buffer.remaining() < methodNameLen) throw new Exception("Invalid request: Incomplete method name.");
        byte[] methodNameBytes = new byte[methodNameLen];
        buffer.get(methodNameBytes);
        String methodName = new String(methodNameBytes, StandardCharsets.UTF_8);

        // 参数数量
        if (buffer.remaining() < 1) throw new Exception("Invalid request: Missing parameter count.");
        int paramCount = Byte.toUnsignedInt(buffer.get());

        List<Parameter> parameters = new ArrayList<>();
        for (int i = 0; i < paramCount; i++) {
            // 参数类型
            if (buffer.remaining() < 1) throw new Exception("Invalid request: Missing parameter type.");
            int typeCode = Byte.toUnsignedInt(buffer.get());
            ParameterType type = ParameterType.fromCode(typeCode);

            // 参数值
            switch (type) {
                case INTEGER:
                    if (buffer.remaining() < 4) throw new Exception("Invalid request: Incomplete integer parameter.");
                    int intValue = buffer.getInt();
                    parameters.add(new Parameter(type, intValue));
                    break;
                case STRING:
                    if (buffer.remaining() < 1) throw new Exception("Invalid request: Missing string length.");
                    int strLen = Byte.toUnsignedInt(buffer.get());
                    if (buffer.remaining() < strLen) throw new Exception("Invalid request: Incomplete string parameter.");
                    byte[] strBytes = new byte[strLen];
                    buffer.get(strBytes);
                    String strValue = new String(strBytes, StandardCharsets.UTF_8);
                    parameters.add(new Parameter(type, strValue));
                    break;
                case FLOAT:
                    if (buffer.remaining() < 4) throw new Exception("Invalid request: Incomplete float parameter.");
                    float floatValue = buffer.getFloat();
                    parameters.add(new Parameter(type, floatValue));
                    break;
                default:
                    throw new Exception("Unsupported parameter type: " + type);
            }
        }

//        return new RPCRequest(methodName, parameters);
        return new RPCRequest(requestId, methodName, parameters);
    }

    // 序列化响应
//    public static byte[] marshallResponse(RPCResponse response) throws Exception {
//        List<Byte> bytes = new ArrayList<>();
//        bytes.add(response.getStatus());
//
//        if (response.getStatus() == 0) { // success
//            List<Parameter> results = response.getResults();
//            if (results.size() > 255) {
//                throw new Exception("Too many result parameters to serialize.");
//            }
//            bytes.add((byte) results.size());
//
//            for (Parameter result : results) {
//                bytes.add((byte) result.getType().getCode());
//                switch (result.getType()) {
//                    case INTEGER:
//                        int intValue = (Integer) result.getValue();
//                        ByteBuffer intBuffer = ByteBuffer.allocate(4);
//                        intBuffer.putInt(intValue);
//                        for (byte b : intBuffer.array()) {
//                            bytes.add(b);
//                        }
//                        break;
//                    case STRING:
//                        String strValue = (String) result.getValue();
//                        byte[] strBytes = strValue.getBytes(StandardCharsets.UTF_8);
//                        if (strBytes.length > 255) {
//                            throw new Exception("String result too long to serialize.");
//                        }
//                        bytes.add((byte) strBytes.length);
//                        for (byte b : strBytes) {
//                            bytes.add(b);
//                        }
//                        break;
//                    case FLOAT:
//                        float floatValue = (Float) result.getValue();
//                        ByteBuffer floatBuffer = ByteBuffer.allocate(4);
//                        floatBuffer.putFloat(floatValue);
//                        for (byte b : floatBuffer.array()) {
//                            bytes.add(b);
//                        }
//                        break;
//                    default:
//                        throw new Exception("Unsupported result type: " + result.getType());
//                }
//            }
//        } else { // error
//            String errorMsg = response.getErrorMessage();
//            byte[] errorBytes = errorMsg.getBytes(StandardCharsets.UTF_8);
//            if (errorBytes.length > 255) {
//                errorBytes = "Error message too long.".getBytes(StandardCharsets.UTF_8);
//            }
//            bytes.add((byte) errorBytes.length);
//            for (byte b : errorBytes) {
//                bytes.add(b);
//            }
//        }
//
//        // 转换为byte数组
//        byte[] byteArray = new byte[bytes.size()];
//        for (int i = 0; i < bytes.size(); i++) {
//            byteArray[i] = bytes.get(i);
//        }
//        return byteArray;
//    }

    // 序列化响应
    public static byte[] marshallResponse(RPCResponse response) throws Exception {
        List<Byte> bytes = new ArrayList<>();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        // 请求标识符
        buffer.putInt(response.getRequestId());

        // 状态码
        buffer.put(response.getStatus());

        if (response.getStatus() == 0) { // success
            List<Parameter> results = response.getResults();
            if (results.size() > 255) {
                throw new Exception("Too many result parameters to serialize.");
            }
            buffer.put((byte) results.size());

            for (Parameter result : results) {
                buffer.put((byte) result.getType().getCode());
                switch (result.getType()) {
                    case INTEGER:
                        buffer.putInt((Integer) result.getValue());
                        break;
                    case STRING:
                        String strValue = (String) result.getValue();
                        byte[] strBytes = strValue.getBytes(StandardCharsets.UTF_8);
                        if (strBytes.length > 255) {
                            throw new Exception("String result too long to serialize.");
                        }
                        buffer.put((byte) strBytes.length);
                        buffer.put(strBytes);
                        break;
                    case FLOAT:
                        buffer.putFloat((Float) result.getValue());
                        break;
                    default:
                        throw new Exception("Unsupported result type: " + result.getType());
                }
            }
        } else { // error
            String errorMsg = response.getErrorMessage();
            byte[] errorBytes = errorMsg.getBytes(StandardCharsets.UTF_8);
            if (errorBytes.length > 255) {
                errorBytes = "Error message too long.".getBytes(StandardCharsets.UTF_8);
            }
            buffer.put((byte) errorBytes.length);
            buffer.put(errorBytes);
        }

        // 获取实际的数据长度
        int actualLength = buffer.position();
        byte[] byteArray = new byte[actualLength];
        buffer.rewind();
        buffer.get(byteArray, 0, actualLength);
        return byteArray;
    }



}

