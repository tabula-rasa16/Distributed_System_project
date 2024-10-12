package com.RPC;

import java.util.List;

public interface IRPCMethod {
    List<Parameter> execute(List<Parameter> parameters) throws Exception;
}
