#ifndef SERIALIZER_H
#define SERIALIZER_H

#include <vector>
#include <string>
#include "RPCRequest.h"
#include "RPCResponse.h"


uint32_t convfton(float f);
float convntof(uint32_t p);

std::vector<char> marshallRequest(const RPCRequest& request);
RPCResponse unmarshallResponse(const char* data, int length);

#endif // SERIALIZER_H

