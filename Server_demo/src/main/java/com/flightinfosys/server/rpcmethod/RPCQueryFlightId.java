package com.flightinfosys.server.rpcmethod;

import com.RPC.IRPCMethod;
import com.RPC.Parameter;
import com.RPC.ParameterType;
import com.flightinfosys.server.domain.Flight;
import com.flightinfosys.server.service.IFlightService;
import com.flightinfosys.server.service.Impl.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class RPCQueryFlightId implements IRPCMethod {

    private  IFlightService flightService = new FlightService();


    @Override
    public List<Parameter> execute(List<Parameter> parameters) throws Exception {
        if (parameters.size() != 2) {
            throw new Exception("QueryFlight method requires exactly 2 parameters.");
        }
        if (parameters.get(0).getType() != ParameterType.STRING ||
                parameters.get(1).getType() != ParameterType.STRING) {
            throw new Exception("QueryFlight method parameters must be string.");
        }

        String source = (String) parameters.get(0).getValue();
        String destination = (String) parameters.get(1).getValue();
        Flight flight = new Flight();
        flight.setSource(source);
        flight.setDestination(destination);

        List<Integer> flightIds = flightService.selectFlightId(flight);


        List<Parameter> resultList = new ArrayList<>();
        if(flightIds != null && flightIds.size() != 0){
            for (Integer flightId : flightIds) {
                Parameter result = new Parameter(ParameterType.INTEGER, flightId);
                resultList.add(result);
            }
        }

        return resultList;


    }
}

