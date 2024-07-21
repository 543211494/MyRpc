package com.lzy.rpc.config;

import lombok.Data;
import lombok.ToString;

/**
 * 注册中心配置类
 */
@Data
@ToString
public class RegistryConfig {

    /**
     * 注册中心地址
     */
    private String host = "127.0.0.1";

    /**
     * 注册中心端口号
     */
    private Integer port = 2181;

    /**
     * 超时时间（单位毫秒）
     */
    private Integer timeout = 10000;

    /**
     * 连接最大重试次数
     */
    private Integer maxRetries=3;

    public String getAddress(){
        return this.host+":"+this.port;
    }
}
