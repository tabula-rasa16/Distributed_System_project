#ifndef RPCREQUEST_H
#define RPCREQUEST_H

#include <string>
#include <vector>
#include "ParameterType.h"
#include "Parameter.h"



struct RPCRequest {
    std::string methodName;
    std::vector<Parameter> parameters;
};



#endif // RPCREQUEST_H
