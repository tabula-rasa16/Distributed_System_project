package com.flightinfosys.service;

import com.flightinfosys.domain.Flight;

import java.util.List;

public interface IFlightService {

    // 1. 根据出发地和目的地选择航班 ID
    List<Integer> selectFlightId(Flight flight);

    // 2. 根据航班 ID 获取航班信息
    Flight selectFlightInfoById(int flightId);

    // 3. 预订座位
    String reserveSeats(int flightId, int seatCount);

    // 4. 注册监控
    void registerMonitor(int flightId, String clientIp, int clientPort, int monitorIntervalSeconds);

    // 5. 删除已过期的监控记录
    void removeExpiredMonitors();

    // 6. 查询票价低于给定参数的所有航班
    List<Flight> getFlightsByMaxPrice(long maxPrice);  
 
    // 7.自动预订最便宜的航班
    String autoBookCheapestFlight(String source, String destination); 
}
