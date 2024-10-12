package RPC;

import java.util.List;

public interface IRPCMethod {
    Parameter execute(List<Parameter> parameters) throws Exception;
}
