package com.flightinfosys.RPCMethod;

import com.RPC.IRPCMethod;

import java.util.HashMap;
import java.util.Map;


public class RPCMethodFactory {
    private static final Map<String, IRPCMethod> methods = new HashMap<>();

    static {
        methods.put("add",new PRCAdd());
        methods.put("queryflightid", new RPCQueryFlightId());
        methods.put("queryflightinfobyid", new RPCQueryFlightInfoById());
        methods.put("reserveseats", new RPCReserveSeats());
        methods.put("registermonitor", new RPCRegisterMonitor());
        methods.put("queryflightbymaxprice", new RPCQueryFlightByMaxPrice());
        methods.put("autobookcheapestflight", new RPCAutoBookCheapestFlight());    
        // 在此处注册方法

    }

    public static IRPCMethod getMethod(String methodName) {
        return methods.get(methodName.toLowerCase());
    }
}
