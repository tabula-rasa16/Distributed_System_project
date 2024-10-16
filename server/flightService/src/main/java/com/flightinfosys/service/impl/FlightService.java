package com.flightinfosys.service.impl;

import com.flightinfosys.domain.Flight;
import com.flightinfosys.domain.Monitor;
import com.flightinfosys.mapper.FlightMapper;
import com.flightinfosys.mapper.MonitorMapper;
import com.flightinfosys.service.IFlightService;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class FlightService implements IFlightService {

    private FlightMapper flightMapper;
    private MonitorMapper monitorMapper;

    public FlightService() {
        String resource = "mybatis-config.xml";

        try {
            InputStream inputStream = Resources.getResourceAsStream(resource);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            SqlSession sqlSession = sqlSessionFactory.openSession();
            this.flightMapper = sqlSession.getMapper(FlightMapper.class);
            this.monitorMapper = sqlSession.getMapper(MonitorMapper.class); // 初始化 MonitorMapper
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 选择航班 ID
    public List<Integer> selectFlightId(Flight flight) {
        return flightMapper.selectFlightId(flight);
    }

    // 根据航班 ID 查询航班信息
    public Flight selectFlightInfoById(int flightId) {
        Flight flight = flightMapper.selectFlightInfoById(flightId);

        if (flight == null) {
            System.out.println("Flight with ID " + flightId + " not found.");
            return null;
        }
        return flight;
    }

    // 预订座位并通知监控的客户端
    public String reserveSeats(int flightId, int seatCount) {
        // 检查航班的可用座位数
        int availableSeats = flightMapper.checkFlightAvailability(flightId);

        // 如果航班不存在
        if (availableSeats == 0) {
            return "Error: Flight with ID " + flightId + " does not exist.";
        }

        // 如果可用座位数不足
        if (availableSeats < seatCount) {
            return "Error: Insufficient available seats.";
        }

        // 更新座位数
        int result = flightMapper.updateSeatAvailability(flightId, seatCount);

        // 如果更新成功
        if (result == 1) {
            // 预订成功，通知监控的客户端
            notifyClientsOfSeatChange(flightId, availableSeats - seatCount);
            return "Reservation successful!";
        } else {
            return "Error: Reservation failed.";
        }
    }

    // 注册监控
    public void registerMonitor(int flightId, String clientIp, int clientPort, int monitorIntervalSeconds) {
        // 计算监控结束时间
        Timestamp monitorEndTime = Timestamp.from(Instant.now().plusSeconds(monitorIntervalSeconds));

        // 注册监控信息
        monitorMapper.registerMonitor(flightId, clientIp, clientPort, monitorEndTime);
        System.out.println("Monitor registered for flight " + flightId + " until " + monitorEndTime);
    }

    // 删除过期的监控
    public void removeExpiredMonitors() {
        monitorMapper.removeExpiredMonitors();
        System.out.println("Expired monitors removed.");
    }


    // 通知所有监控该航班的客户端
    public void notifyClientsOfSeatChange(int flightId, int availableSeats) {
        // 首先清理掉过期的监控记录
        removeExpiredMonitors();

        // 获取监控该航班的所有有效客户端
        List<Monitor> monitors = monitorMapper.getMonitorsByFlightId(flightId);

        // 遍历每个客户端，发送更新通知
        for (Monitor monitor : monitors) {
             sendUpdateToClient(monitor.getClientIp(), monitor.getClientPort(), availableSeats);
        }
    }


    // 模拟的发送更新到客户端 (伪代码)
    public void sendUpdateToClient(String clientIp, int clientPort, int availableSeats) {
        // 模拟通过网络通信向客户端发送座位数更新的消息
        System.out.println("Sending seat update to client " + clientIp + ":" + clientPort 
            + " - Available seats: " + availableSeats);
        // 实际实现需要通过网络层发送（如 TCP/UDP）
    }





    // 查询票价低于给定参数的所有航班
    public List<Flight> getFlightsByMaxPrice(long maxPrice) {
        return flightMapper.selectFlightsByMaxPrice(maxPrice);
    }

    public String autoBookCheapestFlight(String source, String destination) {
        // 查询符合条件的最便宜航班
        Flight flight = flightMapper.selectCheapestFlightWithSeats(source, destination);

        // 检查是否有符合条件的航班
        if (flight == null) {
            return "No available flights found for the given route.";
        }

        // 预订座位
        int result = flightMapper.updateSeatAvailability(flight.getId(), 1); // 预订一张票

        if (result == 1) {
            return "Successfully booked flight from " + flight.getSource() + " to " + flight.getDestination() + 
                   " for $" + flight.getAirfare() + ".";
        } else {
            return "Failed to book the flight. Please try again.";
        }
    }
        
}
