package com.flightinfosys.server.service;

import com.flightinfosys.server.domain.Flight;

import java.util.List;

public interface IFlightService {

    List<Integer> selectFlightId(Flight flight);
}
