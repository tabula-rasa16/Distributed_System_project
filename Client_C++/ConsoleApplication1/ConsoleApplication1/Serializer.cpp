#include "Serializer.h"
#include <cstring>
#include <stdexcept>
#include <winsock2.h>
#include <ws2tcpip.h>

// 确保 Winsock 被正确初始化
#pragma comment(lib, "Ws2_32.lib")




// convert float to network byte order (big endian)
uint32_t convfton(float f) {
    uint32_t p;
    std::memcpy(&p, &f, sizeof(float));
    // Convert to big endian
    return htonl(p);
}

// convert float from network byte order (big endian)
float convntof(uint32_t p) {
    p = ntohl(p);
    float f;
    std::memcpy(&f, &p, sizeof(float));
    return f;
}






// Marshalling: 序列化RPCRequest到字节数组
std::vector<char> marshallRequest(const RPCRequest& request) {
    std::vector<char> buffer;

    // 请求标识符
    int netRequestId = htonl(request.requestId);
    char* p = reinterpret_cast<char*>(&netRequestId);
    buffer.insert(buffer.end(), p, p + sizeof(int));


    // 方法名长度
    if (request.methodName.length() > 255) {
        throw std::runtime_error("Method name too long!");
    }
    buffer.push_back(static_cast<char>(request.methodName.length()));

    // 方法名
    buffer.insert(buffer.end(), request.methodName.begin(), request.methodName.end());

    // 参数数量
    if (request.parameters.size() > 255) {
        throw std::runtime_error("Too many parameters!");
    }
    buffer.push_back(static_cast<char>(request.parameters.size()));

    // 每个参数
    for (const auto& param : request.parameters) {
        // 参数类型
        buffer.push_back(static_cast<char>(param.type));

        // 参数值
        switch (param.type) {
        case INTEGER: {
            int netInt = htonl(param.intValue);
            char* p = reinterpret_cast<char*>(&netInt);
            buffer.insert(buffer.end(), p, p + sizeof(int));
            break;
        }
        case STRING: {
            std::string str = *(param.stringValue);
            if (str.length() > 255) {
                throw std::runtime_error("String parameter too long!");
            }
            buffer.push_back(static_cast<char>(str.length()));
            buffer.insert(buffer.end(), str.begin(), str.end());
            break;
        }
        case MYFLOAT: { 
            uint32_t netFloat = convfton(param.floatValue);
            char* p = reinterpret_cast<char*>(&netFloat);
            buffer.insert(buffer.end(), p, p + sizeof(uint32_t));
            break;
        }
        default:
            throw std::runtime_error("Unsupported parameter type!");
        }
    }

    return buffer;
}

// Unmarshalling: 反序列化字节数组到RPCResponse
RPCResponse unmarshallResponse(const char* data, int length) {
    RPCResponse response;
    int index = 0;

    if (length < 4) {
        throw std::runtime_error("Invalid response length: Missing request ID.");
    }

    // 请求标识符
    int netRequestId;
    std::memcpy(&netRequestId, data + index, sizeof(int));
    response.requestId = ntohl(netRequestId);
    index += sizeof(int);

    if (length < index + 1) {
        throw std::runtime_error("Invalid response length: Missing status.");
    }


   // if (length < 1) {
     //   throw std::runtime_error("Invalid response length.");
   // }

    // 状态码
    response.status = static_cast<uint8_t>(data[index]);
    index += 1;

    if (response.status == 0) { // success
        if (length < index + 1) {
            throw std::runtime_error("Invalid response length for success.");
        }

        // 结果参数数量
        int resultCount = static_cast<uint8_t>(data[index]);
        index += 1;

        for (int i = 0; i < resultCount; i++) {
            if (length < index + 1) {
                throw std::runtime_error("Invalid response length: Missing result parameter type.");
            }
            ParameterType resultType = static_cast<ParameterType>(static_cast<uint8_t>(data[index]));
            index += 1;

            switch (resultType) {
            case INTEGER: {
                if (length < index + 4) {
                    throw std::runtime_error("Invalid response length for integer result.");
                }
                int netInt;
                std::memcpy(&netInt, data + index, sizeof(int));
                int intValue = ntohl(netInt);
                response.results.emplace_back(intValue);
                index += 4;
                break;
            }
            case STRING: {
                if (length < index + 1) {
                    throw std::runtime_error("Invalid response length for string result.");
                }
                int strLen = static_cast<uint8_t>(data[index]);
                index += 1;
                if (length < index + strLen) {
                    throw std::runtime_error("Invalid response length for string result.");
                }
                std::string strValue(data + index, strLen);
                response.results.emplace_back(strValue);
                index += strLen;
                break;
            }
            case MYFLOAT: { 
                if (length < index + 4) {
                    throw std::runtime_error("Invalid response length for float result.");
                }
                uint32_t netFloat;
                std::memcpy(&netFloat, data + index, sizeof(uint32_t));
                float floatValue = convntof(netFloat);
                response.results.emplace_back(floatValue);
                index += 4;
                break;
            }
            default:
                throw std::runtime_error("Unsupported result type.");
            }
        }
    }
    else { // error
        if (length < index + 1) {
            throw std::runtime_error("Invalid response length for error.");
        }

        // 错误消息长度
        int errorMsgLen = static_cast<uint8_t>(data[index]);
        index += 1;

        if (length < index + errorMsgLen) {
            throw std::runtime_error("Invalid response length for error message.");
        }

        // 错误消息
        response.errorMessage = std::string(data + index, errorMsgLen);
        index += errorMsgLen;
    }

    return response;
}
