package com.lzy.rpc.provider.registry;


import com.lzy.rpc.bean.ServiceInfo;
import com.lzy.rpc.config.RegistryConfig;

import java.util.List;

/**
 * 注册中心
 */
public interface Registry {

    /**
     * 初始化
     */
    void init();

    /**
     * 注册服务（服务端）
     */
    void register(ServiceInfo serviceInfo);

    /**
     * 注销服务（服务端）
     */
    void unRegister(ServiceInfo serviceInfo);

    /**
     * 服务发现（获取某服务的所有节点，消费端）
     */
    List<ServiceInfo> serviceDiscovery(String serviceKey);

    /**
     * 服务销毁
     */
    void destroy();
}
