package com.lzy.rpc.loadbalancer;

import com.lzy.rpc.bean.ServiceInfo;

import java.util.List;
import java.util.Random;

/**
 * 带权重的随机负载均衡
 */
public class WeightedRandomLoadBalancer implements LoadBalancer {

    private final Random random = new Random();

    @Override
    public ServiceInfo select(List<ServiceInfo> serviceInfoList) {
        int totalWeight = 0;
        for(int i = 0;i<serviceInfoList.size();i++){
            totalWeight += serviceInfoList.get(i).getWeight();
        }
        int number = random.nextInt(totalWeight);
        for(int i = 0;i<serviceInfoList.size();i++){
            number -= serviceInfoList.get(i).getWeight();
            if(number<0){
                return serviceInfoList.get(i);
            }
        }
        return serviceInfoList.get(0);
    }
}
