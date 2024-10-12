package com.flightinfosys.service;

import com.flightinfosys.domain.Flight;

import java.util.List;

public interface IFlightService {

     List<Integer> selectFlightId(Flight flight);
}
