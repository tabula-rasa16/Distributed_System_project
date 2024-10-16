package com.flightinfosys.RPCMethod;

import com.RPC.IRPCMethod;
import com.RPC.Parameter;
import com.RPC.ParameterType;
import com.flightinfosys.domain.Flight;

import java.util.ArrayList;
import java.util.List;

public class PRCAdd implements IRPCMethod {
    @Override
    public List<Parameter> execute(List<Parameter> parameters) throws Exception {
        if (parameters.size() != 2) {
            throw new Exception("QueryFlight method requires exactly 2 parameters.");
        }
        if ((parameters.get(0).getType() != ParameterType.INTEGER && parameters.get(0).getType() != ParameterType.FLOAT) ||
                (parameters.get(0).getType() != ParameterType.INTEGER && parameters.get(0).getType() != ParameterType.FLOAT)) {
            throw new Exception("QueryFlight method parameters must be integer or float.");
        }
        List<Parameter> results = new ArrayList<>();


        if(parameters.get(0).getType() == ParameterType.INTEGER && parameters.get(1).getType() == ParameterType.INTEGER) {
            int a = (Integer) parameters.get(0).getValue();
            int b = (Integer) parameters.get(1).getValue();
            results.add(new Parameter(ParameterType.INTEGER, a + b));
            return results;
        }
        else{
            float a = (Float) parameters.get(0).getValue();
            float b = (Float) parameters.get(1).getValue();
            results.add(new Parameter(ParameterType.FLOAT, a + b));
            return results;
        }



    }
}
