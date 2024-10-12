#include <winsock2.h>
#include <ws2tcpip.h>
#include <iostream>
#include <string>
#include <vector>
#include "RPCRequest.h"
#include "RPCResponse.h"
#include "ParameterType.h"
#include "Serializer.h"

// 链接Winsock库
#pragma comment(lib, "Ws2_32.lib")

#define SERVER_PORT 9876
#define BUFFER_SIZE 2048
#define SERVER_IP "127.0.0.1" // 假设服务器在本地主机

// Marshalling和Unmarshalling函数声明
// std::vector<char> marshallRequest(const RPCRequest& request);
// RPCResponse unmarshallResponse(const char* data, int length);

int main() {
    WSADATA wsaData;
    SOCKET sockfd;
    struct sockaddr_in serverAddr;
    char buffer[BUFFER_SIZE];

    // 初始化Winsock
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        std::cerr << "WSAStartup failed." << std::endl;
        return 1;
    }

    // 创建UDP套接字
    if ((sockfd = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)) == INVALID_SOCKET) {
        std::cerr << "Socket creation failed with error: " << WSAGetLastError() << std::endl;
        WSACleanup();
        return 1;
    }

    // 服务器信息
    memset(&serverAddr, 0, sizeof(serverAddr));
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(SERVER_PORT);

    
    int ptonResult = InetPtonA(AF_INET, SERVER_IP, &serverAddr.sin_addr);
    if (ptonResult <= 0) {
        if (ptonResult == 0)
            std::cerr << "Invalid address format: " << SERVER_IP << std::endl;
        else
            std::cerr << "InetPtonA failed with error: " << WSAGetLastError() << std::endl;
        closesocket(sockfd);
        WSACleanup();
        return 1;
    }

    //业务代码

    // add方法,参数为int,用于测试INTERGER的序列化与反序列化
    //  RPCRequest request;
    // request.methodName = "add";
    // request.parameters.emplace_back(5);
    // request.parameters.emplace_back(10);


    // add方法，参数为float,用于测试MYFLOAT的序列化与反序列化
    RPCRequest request;
    request.methodName = "add";
    request.parameters.emplace_back(2.1f);
    request.parameters.emplace_back(5.6f);
    
    // 业务1,根据始发地和目的地查询航班号
    //RPCRequest request;
    //request.methodName = "queryFlightId";
    //request.parameters.emplace_back(std::string("Singapore"));
    //request.parameters.emplace_back(std::string("London"));
    

    try {
        // 序列化请求
        std::vector<char> sendData = marshallRequest(request);

        // 发送请求
        int sendResult = sendto(sockfd, sendData.data(), sendData.size(), 0,
            (struct sockaddr*)&serverAddr, sizeof(serverAddr));
        if (sendResult == SOCKET_ERROR) {
            std::cerr << "sendto failed with error: " << WSAGetLastError() << std::endl;
            closesocket(sockfd);
            WSACleanup();
            return 1;
        }
        std::cout << "Sent RPC request: method=" << request.methodName << std::endl;

        // 接收响应
        struct sockaddr_in fromAddr;
        int fromLen = sizeof(fromAddr);
        int recvBytes = recvfrom(sockfd, buffer, BUFFER_SIZE, 0,
            (struct sockaddr*)&fromAddr, &fromLen);
        if (recvBytes == SOCKET_ERROR) {
            std::cerr << "recvfrom failed with error: " << WSAGetLastError() << std::endl;
            closesocket(sockfd);
            WSACleanup();
            return 1;
        }

        // 反序列化响应
        RPCResponse response = unmarshallResponse(buffer, recvBytes);

        if (response.status == 0) {
            // 根据结果类型处理
            std::cout << "RPC Response: " << std::endl;
            for (size_t i = 0; i < response.results.size(); ++i) {
                const Parameter& param = response.results[i];
                std::cout << "  Result " << (i + 1) << ": ";
                switch (param.type) {
                case INTEGER:
                    std::cout << param.intValue;
                    break;
                case STRING:
                    std::cout << *(param.stringValue);
                    break;
                case MYFLOAT:
                    std::cout << param.floatValue;
                    break;
                default:
                    std::cout << "Unsupported result type.";
                }
                std::cout << std::endl;
            }
        }
        else {
            std::cout << "RPC Error: " << response.errorMessage << std::endl;
        }

    }
    catch (const std::exception& e) {
        std::cerr << "Error: " << e.what() << std::endl;
    }

    // 关闭套接字和清理Winsock
    closesocket(sockfd);
    WSACleanup();
    return 0;
}