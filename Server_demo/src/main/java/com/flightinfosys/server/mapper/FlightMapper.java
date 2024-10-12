package com.flightinfosys.server.mapper;

import com.flightinfosys.server.domain.Flight;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FlightMapper {

    List<Integer> selectFlightId(Flight flight);
}
