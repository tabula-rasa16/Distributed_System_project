#ifndef RPCREQUEST_H
#define RPCREQUEST_H

#include <string>
#include <vector>
#include "ParameterType.h"
#include "Parameter.h"



struct RPCRequest {
    int requestId;
    std::string methodName;
    std::vector<Parameter> parameters;
};



#endif // RPCREQUEST_H
