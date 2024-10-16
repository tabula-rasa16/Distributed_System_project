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

public class RPCQueryFlightInfoById implements IRPCMethod {

    //// 使用 FlightService 查询航班信息
    private IFlightService flightService = new FlightService();

    @Override
    public List<Parameter> execute(List<Parameter> parameters) throws Exception {
        // 验证参数数量和类型
        if (parameters.size() != 1) {
            throw new Exception("QueryFlightInfoById method requires exactly 1 parameter.");
        }
        if (parameters.get(0).getType() != ParameterType.INTEGER) {
            throw new Exception("QueryFlightInfoById method parameter must be an integer.");
        }

        // 获取航班 ID
        Integer flightId = (Integer) parameters.get(0).getValue();

        // 查询航班信息
        Flight flight = flightService.selectFlightInfoById(flightId);

        // 如果航班不存在，返回错误信息
        if (flight == null) {
            throw new Exception("Flight with ID " + flightId + " does not exist.");
        }

        // 构造返回结果
        List<Parameter> resultList = new ArrayList<>();

        // 将航班的起飞时间、票价、可用座位数分别转换为参数并返回
        resultList.add(new Parameter(ParameterType.STRING, flight.getDepartureTime().toString())); // 起飞时间
        resultList.add(new Parameter(ParameterType.FLOAT, flight.getAirfare())); // 票价
        resultList.add(new Parameter(ParameterType.INTEGER, flight.getAvailabeSeatNum())); // 可用座位数

        return resultList;
    }
}