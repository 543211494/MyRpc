package com.lzy.rpc.bean;

import lombok.Data;
import lombok.ToString;

/**
 * 服务信息，用于服务注册发现
 */
@Data
@ToString
public class ServiceInfo {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务域名
     */
    private String serviceHost;

    /**
     * 服务端口号
     */
    private Integer servicePort;

    /**
     * 服务权重
     */
    private Integer weight;

    public String getAddress(){
        return "http://"+this.serviceHost+":"+this.servicePort;
    }
}
