package com.flightinfosys.server.rpcmethod;

import com.RPC.IRPCMethod;
import com.flightinfosys.server.service.IFlightService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


public class RPCMethodFactory {
    private static final Map<String, IRPCMethod> methods = new HashMap<>();

    static {
        methods.put("queryflightid", new RPCQueryFlightId());

        // 可以根据需要注册更多方法
        // methods.put("divide", new DivideMethod());
    }

    public static IRPCMethod getMethod(String methodName) {
        return methods.get(methodName.toLowerCase());
    }
}
