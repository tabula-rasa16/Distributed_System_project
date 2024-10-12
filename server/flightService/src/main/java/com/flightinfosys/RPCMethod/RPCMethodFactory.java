package com.flightinfosys.RPCMethod;

import com.RPC.IRPCMethod;

import java.util.HashMap;
import java.util.Map;


public class RPCMethodFactory {
    private static final Map<String, IRPCMethod> methods = new HashMap<>();

    static {
        methods.put("add",new PRCAdd());
        methods.put("queryflightid", new RPCQueryFlightId());
        // 在此处注册方法

    }

    public static IRPCMethod getMethod(String methodName) {
        return methods.get(methodName.toLowerCase());
    }
}
