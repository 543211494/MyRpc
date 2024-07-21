package com.lzy.rpc.config;

import lombok.Data;
import lombok.ToString;

/**
 * 服务端配置
 */
@Data
@ToString
public class ServerConfig {

    /**
     * 服务名称
     */
    private String serviceName = "service";

    /**
     * 服务器主机名
     */
    private String host = "127.0.0.1";

    /**
     * 服务器端口号
     */
    private Integer port = 8080;
}
