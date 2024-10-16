package com.flightinfosys.RPCMethod;

import com.RPC.IRPCMethod;
import com.RPC.Parameter;
import com.RPC.ParameterType;
import com.flightinfosys.service.IFlightService;
import com.flightinfosys.service.impl.FlightService;

import java.util.ArrayList;
import java.util.List;

public class RPCRegisterMonitor implements IRPCMethod {

    // 使用 FlightService 处理业务逻辑
    private IFlightService flightService = new FlightService();

    @Override
    public List<Parameter> execute(List<Parameter> parameters) throws Exception {
        // 检查参数是否正确（期望接收4个参数）
        if (parameters.size() != 4) {
            throw new Exception("RegisterMonitor method requires exactly 4 parameters.");
        }

        // 检查参数类型：flightId, clientIp, clientPort, monitorIntervalSeconds
        if (parameters.get(0).getType() != ParameterType.INTEGER ||
                parameters.get(1).getType() != ParameterType.STRING ||
                parameters.get(2).getType() != ParameterType.INTEGER ||
                parameters.get(3).getType() != ParameterType.INTEGER) {
            throw new Exception("RegisterMonitor method parameters are not of expected types.");
        }

        // 获取参数值
        int flightId = (Integer) parameters.get(0).getValue();
        String clientIp = (String) parameters.get(1).getValue();
        int clientPort = (Integer) parameters.get(2).getValue();
        int monitorIntervalSeconds = (Integer) parameters.get(3).getValue();

        // 调用服务层的 registerMonitor 注册监控信息
        flightService.registerMonitor(flightId, clientIp, clientPort, monitorIntervalSeconds);


        // 返回结果
        List<Parameter> resultList = new ArrayList<>();
        resultList.add(new Parameter(ParameterType.STRING, "Monitor registered successfully!"));
        return resultList;
    }
}
