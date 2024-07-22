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
        int current = currentIndex.getAndIncrement();
        int index = current%size;
        /**
         * 使用自旋锁更新currentIndex值
         */
        if(current>=size){
            int next;
            do {
                current = currentIndex.get();
                next = (current) % size;
            } while (!currentIndex.compareAndSet(current, next));
        }
        return serviceMetaInfoList.get(index);
    }
}
