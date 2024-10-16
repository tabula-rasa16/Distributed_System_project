package com.flightinfosys.domain;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
@Data
public class Monitor {

    private Integer id;              // 监控记录 ID
    private Integer flightId;         // 监控的航班 ID
    private String clientIp;          // 客户端 IP 地址
    private Integer clientPort;       // 客户端端口号
    private Timestamp monitorEndTime; // 监控结束时间

    // Getter and Setter methods
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFlightId() {
        return flightId;
    }

    public void setFlightId(Integer flightId) {
        this.flightId = flightId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public Integer getClientPort() {
        return clientPort;
    }

    public void setClientPort(Integer clientPort) {
        this.clientPort = clientPort;
    }

    public Timestamp getMonitorEndTime() {
        return monitorEndTime;
    }

    public void setMonitorEndTime(Timestamp monitorEndTime) {
        this.monitorEndTime = monitorEndTime;
    }
}
