package com.lzy.rpc.loadbalancer;

import com.lzy.rpc.bean.ServiceInfo;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡
 */
public class RoundRobinLoadBalancer implements LoadBalancer{
    /**
     * 当前轮询的下标
     */
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    @Override
    public ServiceInfo select(List<ServiceInfo> serviceMetaInfoList) {
        int size = serviceMetaInfoList.size();
        int index = Math.abs(currentIndex.getAndIncrement() % size);
        return serviceMetaInfoList.get(index);
    }
}
