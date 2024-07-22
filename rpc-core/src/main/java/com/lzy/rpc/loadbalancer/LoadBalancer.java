package com.lzy.rpc.loadbalancer;

import com.lzy.rpc.bean.ServiceInfo;

import java.util.List;

/**
 * 负载均衡接口
 */
public interface LoadBalancer {

    /**
     * 选择要调用的服务
     */
    public ServiceInfo select(List<ServiceInfo> serviceInfoList);
}
