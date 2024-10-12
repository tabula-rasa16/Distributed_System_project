package service;
import java.util.List;
import RPC.*;
import service.impl.QueryFlightService;

public class QueryFlightMethod implements IRPCMethod {
    @Override
    public Parameter execute(List<Parameter> parameters) throws Exception {
        if (parameters.size() != 2) {
            throw new Exception("QueryFlight method requires exactly 2 parameters.");
        }
        if (parameters.get(0).getType() != ParameterType.STRING ||
                parameters.get(1).getType() != ParameterType.STRING) {
            throw new Exception("QueryFlight method parameters must be string.");
        }
        int a = (Integer) parameters.get(0).getValue();
        int b = (Integer) parameters.get(1).getValue();
        String source = (String) parameters.get(0).getValue();
        String destination = (String) parameters.get(1).getValue();
        return new Parameter(ParameterType.INTEGER, a + b);
    }
}
