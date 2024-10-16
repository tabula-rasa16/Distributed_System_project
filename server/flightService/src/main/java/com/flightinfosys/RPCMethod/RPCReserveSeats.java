package com.flightinfosys.RPCMethod;

import com.RPC.IRPCMethod;
import com.RPC.Parameter;
import com.RPC.ParameterType;
import com.flightinfosys.domain.Flight;
import com.flightinfosys.service.IFlightService;
import com.flightinfosys.service.impl.FlightService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

public class RPCReserveSeats implements IRPCMethod {

    // 使用 FlightService 处理业务逻辑
    private IFlightService flightService = new FlightService();

    @Override
    public List<Parameter> execute(List<Parameter> parameters) throws Exception {
        // 检查参数是否正确
        if (parameters.size() != 2) {
            throw new Exception("ReserveSeats method requires exactly 2 parameters.");
        }
        if (parameters.get(0).getType() != ParameterType.INTEGER ||
                parameters.get(1).getType() != ParameterType.INTEGER) {
            throw new Exception("ReserveSeats method parameters must be integers.");
        }

        // 获取参数：flightId 和 seatCount
        int flightId = (Integer) parameters.get(0).getValue();
        int seatCount = (Integer) parameters.get(1).getValue();

        // 调用服务层逻辑进行座位预订
        String reservationResult = flightService.reserveSeats(flightId, seatCount);

        // 创建返回结果的参数列表
        List<Parameter> resultList = new ArrayList<>();

        // 返回预订结果
        Parameter result = new Parameter(ParameterType.STRING, reservationResult);
        resultList.add(result);

        return resultList;
    }
}
