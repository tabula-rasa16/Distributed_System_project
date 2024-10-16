#include <mutex>
#include <winsock2.h>
#include <ws2tcpip.h>
#include <iostream>
#include <string>
#include <vector>
#include <chrono>
#include <thread>
#include <atomic>
#include <mutex>
#include <unordered_map>
#include <random>
#include "RPCRequest.h"
#include "RPCResponse.h"
#include "ParameterType.h"
#include "Serializer.h"

// 链接Winsock库
#pragma comment(lib, "Ws2_32.lib")

#define SERVER_PORT 9876
#define BUFFER_SIZE 2048
#define SERVER_IP "127.0.0.1" // 假设服务器在本地主机
#define TIMEOUT_MS 2000        // 超时时间2秒
#define MAX_RETRIES 5          // 最大重试次数

// 声明序列化和反序列化函数
// std::vector<char> marshallRequest(const RPCRequest& request);
// RPCResponse unmarshallResponse(const char* data, int length);

// 生成唯一请求ID
int generateRequestId() {
    static std::mt19937 rng(std::random_device{}());
    static std::uniform_int_distribution<int> dist(1, INT32_MAX);
    return dist(rng);
}

int main(int argc, char* argv[]) {
    // 读取调用语义参数
    if (argc < 2) {
        std::cerr << "Usage: RPCClient <semantics>" << std::endl;
        std::cerr << "semantics: at-most-once | at-least-once" << std::endl;
        return 1;
    }
    std::string semantics = argv[1];
    if (semantics != "at-most-once" && semantics != "at-least-once") {
        std::cerr << "Invalid semantics. Use 'at-most-once' or 'at-least-once'." << std::endl;
        return 1;
    }

    WSADATA wsaData;
    SOCKET sockfd;
    struct sockaddr_in serverAddr;
    char buffer[BUFFER_SIZE];
    std::unordered_map<int, RPCResponse> responseMap; // 存储响应
    std::mutex mapMutex;
    std::atomic<bool> running(true);

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

    // 使用 InetPtonA 替代 InetPton
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

    // 线程：接收响应
    std::thread recvThread([&]() {
        while (running) {
            struct sockaddr_in fromAddr;
            int fromLen = sizeof(fromAddr);
            int recvBytes = recvfrom(sockfd, buffer, BUFFER_SIZE, 0,
                (struct sockaddr*)&fromAddr, &fromLen);
            if (recvBytes == SOCKET_ERROR) {
                // 检查是否是由于关闭套接字引起的错误
                if (running) {
                    std::cerr << "recvfrom failed with error: " << WSAGetLastError() << std::endl;
                }
                continue;
            }

            try {
                // 反序列化响应
                RPCResponse response = unmarshallResponse(buffer, recvBytes);

                // 存储响应
                std::lock_guard<std::mutex> lock(mapMutex);
                responseMap[response.requestId] = response;

                std::cout << "Received response: requestId=" << response.requestId << std::endl;
            }
            catch (const std::exception& e) {
                std::cerr << "Failed to deserialize response: " << e.what() << std::endl;
            }
        }
        });

    // 示例调用：调用add方法
    {
        RPCRequest request;
        request.requestId = generateRequestId();
        request.methodName = "add";
        request.parameters.emplace_back(5);
        request.parameters.emplace_back(10);

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

                // 等待响应
                std::this_thread::sleep_for(std::chrono::milliseconds(TIMEOUT_MS));

                // 检查是否收到响应
                {
                    std::lock_guard<std::mutex> lock(mapMutex);
                    if (responseMap.find(request.requestId) != responseMap.end()) {
                        RPCResponse response = responseMap[request.requestId];
                        responseMap.erase(request.requestId);
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
                }

                if (!responseReceived) {
                    retries++;
                    std::cout << "No response received. Retrying (" << retries << "/" << MAX_RETRIES << ")..." << std::endl;
                }

            }
            catch (const std::exception& e) {
                std::cerr << "Error: " << e.what() << std::endl;
                retries++;
            }
        }

        if (!responseReceived) {
            std::cerr << "Failed to receive response after " << MAX_RETRIES << " retries." << std::endl;
        }
    }

    

    // 结束接收线程
    running = false;
    closesocket(sockfd);
    recvThread.join();
    WSACleanup();
    return 0;
}