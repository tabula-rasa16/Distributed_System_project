#ifndef RPCRESPONSE_H
#define RPCRESPONSE_H

#include <string>
#include <vector>
#include "ParameterType.h"
#include "Parameter.h"

struct RPCResponse {
    uint8_t status; // 0: success, 1: error
    std::vector<Parameter> results; // 仅在成功时使用
    std::string errorMessage; // 仅在出错时使用

    RPCResponse() : status(1) {}
};

#endif // RPCRESPONSE_H