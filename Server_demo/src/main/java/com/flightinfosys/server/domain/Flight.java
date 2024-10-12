package com.flightinfosys.server.domain;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Data
public class Flight {


    private Integer id;
    private String source;
    private String destination;
    private Date departureTime;
    private Long airfare;
    private Integer availabeSeatNum;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public String getDestination() {
        return destination;
    }
    public void setDestination(String destination) {
        this.destination = destination;
    }
    public Date getDepartureTime() {
        return departureTime;
    }
    public void setDepartureTime(Date departureTime) {
        this.departureTime = departureTime;
    }

    public Long getAirfare() {
        return airfare;
    }

    public void setAirfare(Long airfare) {
        this.airfare = airfare;
    }

    public Integer getAvailabeSeatNum() {
        return availabeSeatNum;
    }

    public void setAvailabeSeatNum(Integer availabeSeatNum) {
        this.availabeSeatNum = availabeSeatNum;
    }


}
