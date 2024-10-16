#include <winsock2.h>
#include <ws2tcpip.h>
#include <iostream>
#include <string>
#include <vector>
#include <chrono>
#include <thread>
#include <atomic>
#include <unordered_map>
#include <random>
#include <iphlpapi.h>

#include "RPCRequest.h"
#include "RPCResponse.h"
#include "ParameterType.h"
#include "Serializer.h"

// 链接Winsock库
#pragma comment(lib, "Ws2_32.lib")
#pragma comment(lib, "iphlpapi.lib")

#define SERVER_PORT 9876
#define BUFFER_SIZE 2048
#define SERVER_IP "127.0.0.1" // 假设服务器在本地主机
#define TIMEOUT_MS 2000        // 超时时间2秒
#define MAX_RETRIES 5          // 最大重试次数

// 生成唯一请求ID
int generateRequestId() {
    static std::mt19937 rng(std::random_device{}());
    static std::uniform_int_distribution<int> dist(1, INT32_MAX);
    return dist(rng);
}

// 获取有效的局域网IP地址
std::string getValidLocalIP() {
    ULONG bufferSize = 0;
    if (GetAdaptersAddresses(AF_INET, 0, NULL, NULL, &bufferSize) == ERROR_BUFFER_OVERFLOW) {
        std::vector<BYTE> buffer(bufferSize);
        IP_ADAPTER_ADDRESSES* pAddresses = reinterpret_cast<IP_ADAPTER_ADDRESSES*>(buffer.data());
        if (GetAdaptersAddresses(AF_INET, 0, NULL, pAddresses, &bufferSize) == NO_ERROR) {
            for (IP_ADAPTER_ADDRESSES* pCurrAddresses = pAddresses; pCurrAddresses; pCurrAddresses = pCurrAddresses->Next) {
                if (pCurrAddresses->OperStatus == IfOperStatusUp && pCurrAddresses->IfType != IF_TYPE_SOFTWARE_LOOPBACK) {
                    for (IP_ADAPTER_UNICAST_ADDRESS* pUnicast = pCurrAddresses->FirstUnicastAddress; pUnicast; pUnicast = pUnicast->Next) {
                        if (pUnicast->Address.lpSockaddr->sa_family == AF_INET) {
                            char ip[INET_ADDRSTRLEN];
                            sockaddr_in* sa_in = reinterpret_cast<sockaddr_in*>(pUnicast->Address.lpSockaddr);
                            inet_ntop(AF_INET, &(sa_in->sin_addr), ip, INET_ADDRSTRLEN);
                            return std::string(ip);
                        }
                    }
                }
            }
        }
    }
    return "127.0.0.1";  // 如果找不到有效的局域网IP，返回回环地址
}

// 获取本地端口
int getLocalPort(SOCKET sockfd) {
    struct sockaddr_in addr;
    int addrLen = sizeof(addr);

    if (getsockname(sockfd, (struct sockaddr*)&addr, &addrLen) == 0) {
        return ntohs(addr.sin_port);
    }

    return 0; // 如果获取失败，返回0
}

// 美化命令行界面
void displayMenu() {
    std::cout << "========================================" << std::endl;
    std::cout << "        WELCOME TO RPC CLIENT           " << std::endl;
    std::cout << "========================================" << std::endl;
    std::cout << "Available commands:" << std::endl;
    std::cout << "----------------------------------------" << std::endl;
    std::cout << "|  add <num1> <num2>                            |" << std::endl;
    std::cout << "|  queryflightid <source> <destination>          |" << std::endl;
    std::cout << "|  queryflightinfobyid <flightId>                |" << std::endl;
    std::cout << "|  reserveseats <flightId> <seatCount>           |" << std::endl;
    std::cout << "|  registermonitor <flightId> <intervalSeconds>  |" << std::endl;
    std::cout << "|  queryflightbymaxprice <maxPrice>              |" << std::endl;
    std::cout << "|  autobookcheapestflight <source> <destination> |" << std::endl;
    std::cout << "----------------------------------------" << std::endl;
    std::cout << "Type 'exit' to quit." << std::endl;
}

// 处理RPC请求
bool handleRPCRequest(SOCKET sockfd, struct sockaddr_in serverAddr, RPCRequest& request) {
    char buffer[BUFFER_SIZE];
    int retries = 0;
    bool responseReceived = false;

    while (retries < MAX_RETRIES && !responseReceived) {
        try {
            // 序列化请求
            std::vector<char> sendData = marshallRequest(request);

            // 发送请求
            int sendResult = sendto(sockfd, sendData.data(), sendData.size(), 0,
                (struct sockaddr*)&serverAddr, sizeof(serverAddr));
            if (sendResult == SOCKET_ERROR) {
                std::cerr << "sendto failed with error: " << WSAGetLastError() << std::endl;
                throw std::runtime_error("Failed to send request.");
            }
            std::cout << "Sent RPC request: method=" << request.methodName
                << ", requestId=" << request.requestId << std::endl;

            // 设置接收超时
            DWORD timeout = TIMEOUT_MS;
            setsockopt(sockfd, SOL_SOCKET, SO_RCVTIMEO, (const char*)&timeout, sizeof(timeout));

            // 接收响应
            struct sockaddr_in fromAddr;
            int fromLen = sizeof(fromAddr);
            int recvBytes = recvfrom(sockfd, buffer, BUFFER_SIZE, 0,
                (struct sockaddr*)&fromAddr, &fromLen);
            if (recvBytes == SOCKET_ERROR) {
                std::cerr << "recvfrom failed or timed out. Retrying (" << (retries + 1) << "/" << MAX_RETRIES << ")..." << std::endl;
                retries++;
                continue;
            }

            // 反序列化响应
            RPCResponse response = unmarshallResponse(buffer, recvBytes);

            // 检查请求ID
            if (response.requestId != request.requestId) {
                std::cerr << "Mismatched response ID. Expected: " << request.requestId
                    << ", Received: " << response.requestId << std::endl;
                retries++;
                continue;
            }

            // 处理响应
            if (response.status == 0) {
                std::cout << "RPC Response:" << std::endl;
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

            responseReceived = true;

        }
        catch (const std::exception& e) {
            std::cerr << "Error: " << e.what() << std::endl;
            retries++;
        }
    }

    if (!responseReceived) {
        std::cerr << "Failed to receive response after " << MAX_RETRIES << " retries." << std::endl;
        return false;
    }

    return true;
}

int main(int argc, char* argv[]) {
    WSADATA wsaData;
    SOCKET sockfd;
    struct sockaddr_in serverAddr;

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

    // 绑定到任意可用端口
    struct sockaddr_in clientAddr;
    clientAddr.sin_family = AF_INET;
    clientAddr.sin_addr.s_addr = INADDR_ANY;
    clientAddr.sin_port = 0;  // 让系统分配一个可用端口
    if (bind(sockfd, (struct sockaddr*)&clientAddr, sizeof(clientAddr)) == SOCKET_ERROR) {
        std::cerr << "Bind failed with error: " << WSAGetLastError() << std::endl;
        closesocket(sockfd);
        WSACleanup();
        return 1;
    }

    // 服务器信息
    memset(&serverAddr, 0, sizeof(serverAddr));
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(SERVER_PORT);

    // 使用 InetPtonA
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

    // 显示命令行界面
    displayMenu();

    // 命令行输入循环
    std::string command;
    while (true) {
        std::cout << "> ";
        std::getline(std::cin, command);

        if (command == "exit") {
            break;
        }

        std::istringstream iss(command);
        std::string cmd;
        iss >> cmd;

        // 添加不同的命令处理逻辑
        if (cmd == "add") {
            int num1, num2;
            iss >> num1 >> num2;

            RPCRequest request;
            request.requestId = generateRequestId();
            request.methodName = "add";
            request.parameters.emplace_back(num1);
            request.parameters.emplace_back(num2);

            handleRPCRequest(sockfd, serverAddr, request);
        } 
        else if (cmd == "queryflightid") {
            std::string source, destination;
            iss >> source >> destination;

            RPCRequest request;
            request.requestId = generateRequestId();
            request.methodName = "queryflightid";
            request.parameters.emplace_back(source);
            request.parameters.emplace_back(destination);

            handleRPCRequest(sockfd, serverAddr, request);
        }
        else if (cmd == "queryflightinfobyid") {
            int flightId;
            iss >> flightId;

            RPCRequest request;
            request.requestId = generateRequestId();
            request.methodName = "queryflightinfobyid";
            request.parameters.emplace_back(flightId);

            handleRPCRequest(sockfd, serverAddr, request);
        }
        else if (cmd == "reserveseats") {
            int flightId, seatCount;
            iss >> flightId >> seatCount;

            RPCRequest request;
            request.requestId = generateRequestId();
            request.methodName = "reserveseats";
            request.parameters.emplace_back(flightId);
            request.parameters.emplace_back(seatCount);

            handleRPCRequest(sockfd, serverAddr, request);
        }
        else if (cmd == "registermonitor") {
            int flightId, interval;
            iss >> flightId >> interval;

            RPCRequest request;
            request.requestId = generateRequestId();
            request.methodName = "registermonitor";

            std::string ipStr = getValidLocalIP();
            int port = getLocalPort(sockfd);
            request.parameters.emplace_back(flightId);
            request.parameters.emplace_back(ipStr);
            request.parameters.emplace_back(port);
            request.parameters.emplace_back(interval);

            handleRPCRequest(sockfd, serverAddr, request);
        }
        else if (cmd == "queryflightbymaxprice") {
            long maxPrice;
            iss >> maxPrice;

            RPCRequest request;
            request.requestId = generateRequestId();
            request.methodName = "queryflightbymaxprice";
            request.parameters.emplace_back(maxPrice);

            handleRPCRequest(sockfd, serverAddr, request);
        }
        else if (cmd == "autobookcheapestflight") {
            std::string source, destination;
            iss >> source >> destination;

            RPCRequest request;
            request.requestId = generateRequestId();
            request.methodName = "autobookcheapestflight";
            request.parameters.emplace_back(source);
            request.parameters.emplace_back(destination);

            handleRPCRequest(sockfd, serverAddr, request);
        }
        else {
            std::cerr << "Unknown command: " << cmd << std::endl;
            displayMenu(); // 重新显示菜单
        }
    }

    // 关闭套接字和清理Winsock
    closesocket(sockfd);
    WSACleanup();
    return 0;
}
