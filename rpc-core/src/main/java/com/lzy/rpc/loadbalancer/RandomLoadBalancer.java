package com.lzy.rpc.loadbalancer;

import com.lzy.rpc.bean.ServiceInfo;

import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡
 */
public class RandomLoadBalancer implements LoadBalancer{

    private final Random random = new Random();

    @Override
    public ServiceInfo select(List<ServiceInfo> serviceInfoList) {
        int index = random.nextInt(serviceInfoList.size());
        return serviceInfoList.get(index);
    }
}
