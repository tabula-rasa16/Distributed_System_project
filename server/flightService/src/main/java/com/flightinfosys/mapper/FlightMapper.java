package com.flightinfosys.mapper;

import com.flightinfosys.domain.Flight;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FlightMapper {

    List<Integer> selectFlightId(Flight flight);
}
