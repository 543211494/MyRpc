package com.lzy.rpc.config;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RpcConfig {

    /**
     * 是否启用注册中心
     */
    private boolean useRegistry = false;

    /**
     * 服务端配置
     */
    private ServerConfig server = new ServerConfig();

    /**
     * 客户端配置
     */
    private ClientConfig client = new ClientConfig();

    /**
     * 注册中心配置
     */
    private RegistryConfig registry = new RegistryConfig();

}
