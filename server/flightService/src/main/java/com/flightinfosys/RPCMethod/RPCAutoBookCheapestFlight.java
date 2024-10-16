package com.flightinfosys.RPCMethod;

import com.RPC.IRPCMethod;
import com.RPC.Parameter;
import com.RPC.ParameterType;
import com.flightinfosys.service.IFlightService;
import com.flightinfosys.service.impl.FlightService;

import java.util.ArrayList;
import java.util.List;

public class RPCAutoBookCheapestFlight implements IRPCMethod {

    // 使用 FlightService 处理业务逻辑
    private IFlightService flightService = new FlightService();

    @Override
    public List<Parameter> execute(List<Parameter> parameters) throws Exception {
        // 检查参数是否正确（期望接收2个参数：source 和 destination）
        if (parameters.size() != 2) {
            throw new Exception("AutoBookCheapestFlight method requires exactly 2 parameters.");
        }

        // 检查参数类型
        if (parameters.get(0).getType() != ParameterType.STRING || parameters.get(1).getType() != ParameterType.STRING) {
            throw new Exception("AutoBookCheapestFlight parameters must be strings.");
        }

        // 获取参数值
        String source = (String) parameters.get(0).getValue();
        String destination = (String) parameters.get(1).getValue();

        // 调用服务层逻辑自动预订最便宜的航班
        String bookingResult = flightService.autoBookCheapestFlight(source, destination);

        // 返回结果
        List<Parameter> resultList = new ArrayList<>();
        resultList.add(new Parameter(ParameterType.STRING, bookingResult));
        return resultList;
    }
}
