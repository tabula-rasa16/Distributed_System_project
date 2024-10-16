package com.flightinfosys.mapper;

import com.flightinfosys.domain.Monitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface MonitorMapper {

    // 1. 注册新的监控
    void registerMonitor(@Param("flightId") int flightId, 
                         @Param("clientIp") String clientIp, 
                         @Param("clientPort") int clientPort, 
                         @Param("monitorEndTime") Timestamp monitorEndTime);

    // 2. 查询监控该航班的所有客户端
    List<Monitor> getMonitorsByFlightId(@Param("flightId") int flightId);

    // 3. 删除已过期的监控信息
    void removeExpiredMonitors();

}
