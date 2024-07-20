package com.lzy.rpc.config;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RpcConfig {
    /**
     * 服务器主机名
     */
    private String serverHost = "127.0.0.1";

    /**
     * 服务器端口号
     */
    private Integer serverPort = 8080;
}
