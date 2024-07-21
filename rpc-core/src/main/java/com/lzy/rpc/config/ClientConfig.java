package com.lzy.rpc.config;

import lombok.Data;
import lombok.ToString;

/**
 * 客户端配置
 */
@Data
@ToString
public class ClientConfig {

    /**
     * 服务名称
     */
    private String serviceName = "service";

    /**
     * 服务器主机名
     */
    private String serverHost = "127.0.0.1";

    /**
     * 服务器端口号
     */
    private Integer serverPort = 8080;

    public String getAddress(){
        return "http://"+this.serverHost+":"+this.serverPort;
    }
}
