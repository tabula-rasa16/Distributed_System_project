package com.flightinfosys.RPCMethod;

import com.RPC.IRPCMethod;
import com.RPC.Parameter;
import com.RPC.ParameterType;
import com.flightinfosys.domain.Flight;
import com.flightinfosys.service.IFlightService;
import com.flightinfosys.service.impl.FlightService;

import java.util.ArrayList;
import java.util.List;

public class RPCQueryFlightByMaxPrice implements IRPCMethod {

    // 使用 FlightService 处理业务逻辑
    private IFlightService flightService = new FlightService();

    @Override
    public List<Parameter> execute(List<Parameter> parameters) throws Exception {
        // 检查参数是否正确（期望接收1个参数）
        if (parameters.size() != 1) {
            throw new Exception("getFlightsByMaxPrice method requires exactly 1 parameter.");
        }

        // 检查参数类型
        if (parameters.get(0).getType() != ParameterType.FLOAT) {
            throw new Exception("getFlightsByMaxPrice parameter must be a long.");
        }

        // 获取参数值
        long maxPrice = (Long) parameters.get(0).getValue();

        // 调用服务层的 getFlightsByMaxPrice 方法
        List<Flight> flights = flightService.getFlightsByMaxPrice(maxPrice);

        // 返回结果
        List<Parameter> resultList = new ArrayList<>();
        for (Flight flight : flights) {
            // 返回每个航班的出发地、目的地和票价
            resultList.add(new Parameter(ParameterType.STRING, flight.getSource() + " -> " + flight.getDestination() + " - $" + flight.getAirfare()));
        }

        return resultList;
    }
}
