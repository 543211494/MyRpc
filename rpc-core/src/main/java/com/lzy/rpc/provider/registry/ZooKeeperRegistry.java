package com.lzy.rpc.provider.registry;

import cn.hutool.core.io.LineHandler;
import cn.hutool.core.lang.func.Func1;
import com.lzy.rpc.RpcApplication;
import com.lzy.rpc.bean.ServiceInfo;
import com.lzy.rpc.config.RegistryConfig;
import com.sun.xml.internal.ws.wsdl.writer.document.Service;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ZooKeeperRegistry implements Registry{

    /**
     * 客户端连接实例
     */
    private CuratorFramework client;

    /**
     * 用于进行服务注册的对象
     */
    private ServiceDiscovery<ServiceInfo> serviceDiscovery;

    /**
     * 根节点
     */
    private static final String ZK_ROOT_PATH = "/rpc/zk";

    @Override
    public void init() {
        RegistryConfig registryConfig = RpcApplication.rpcConfig.getRegistry();
        /**
         * 重试策略
         */
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(registryConfig.getTimeout(),registryConfig.getMaxRetries());
        this.client = CuratorFrameworkFactory
                .builder()
                .connectString(registryConfig.getAddress())
                .retryPolicy(retryPolicy)
                .build();

        // 构建 serviceDiscovery 实例
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceInfo.class)
                .client(this.client)
                .basePath(ZooKeeperRegistry.ZK_ROOT_PATH)
                .serializer(new JsonInstanceSerializer<>(ServiceInfo.class))
                .build();
        try {
            client.start();
            serviceDiscovery.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(ServiceInfo serviceInfo) {
        try {
            /**
             * name为服务的名称,是服务发现的依据
             * payload为节点中存储的数据，即服务的详细信息
             */
            ServiceInstance<ServiceInfo> serviceInstance = ServiceInstance.<ServiceInfo>builder()
                    .name(serviceInfo.getServiceName())
                    .payload(serviceInfo)
                    .build();

            /**
             * 执行序列化后，会在zookeeper的中创建临时节点
             * 临时节点为当前根目录/serviceInstance.name的子节点
             * 临时节点中存储序列化后的serviceInstance
             */
            this.serviceDiscovery.registerService(serviceInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unRegister(ServiceInfo serviceInfo) {
        try {
            ServiceInstance<ServiceInfo> serviceInstance = ServiceInstance.<ServiceInfo>builder()
                    .name(serviceInfo.getServiceName())
                    .payload(serviceInfo)
                    .build();

            this.serviceDiscovery.unregisterService(serviceInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ServiceInfo> serviceDiscovery(String serviceKey) {
        try {
//            Collection<ServiceInstance<ServiceInfo>> serviceInstances = serviceDiscovery.queryForInstances(serviceKey);
//            ServiceInstance<ServiceInfo>[] array = serviceInstances.stream().toArray(ServiceInstance[]::new);
//            List<ServiceInfo> list = new ArrayList<>();
//            for(int i = 0;i<array.length;i++){
//                list.add(array[i].getPayload());
//            }
            // 查询服务信息
            Collection<ServiceInstance<ServiceInfo>> serviceInstanceList = serviceDiscovery.queryForInstances(serviceKey);

            // 解析服务信息
            List<ServiceInfo> list = serviceInstanceList.stream()
                    .map(ServiceInstance::getPayload)
                    .collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void destroy() {
        if(this.client!=null){
            this.client.close();
        }
    }
}
