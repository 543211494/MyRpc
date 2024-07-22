package com.lzy.rpc;

import com.lzy.rpc.config.Constant;
import com.lzy.rpc.config.RpcConfig;
import com.lzy.rpc.consumer.retry.Retry;
import com.lzy.rpc.consumer.tolerant.Tolerant;
import com.lzy.rpc.loadbalancer.LoadBalancer;
import com.lzy.rpc.provider.registry.Registry;
import com.lzy.rpc.provider.registry.ZooKeeperRegistry;
import com.lzy.rpc.util.ConfigUtil;
import com.lzy.rpc.util.SpiLoader;

/**
 * 框架类，负责执行初始化操作
 */
public class RpcApplication {

    public static volatile RpcConfig rpcConfig;

    public static volatile Registry registry;

    public static volatile LoadBalancer loadBalancer;

    public static volatile Retry retry;

    public static volatile Tolerant tolerant;

    public static void init(){
        if(RpcApplication.rpcConfig==null){
            try {
                RpcApplication.rpcConfig = ConfigUtil.loadConfig(RpcConfig.class, Constant.DEFAULT_CONFIG_PREFIX);
                if(RpcApplication.rpcConfig.isUseRegistry()){
                    RpcApplication.registry = new ZooKeeperRegistry();
                    RpcApplication.registry.init();
                }else{
                    RpcApplication.registry = null;
                }
                SpiLoader.init();
                RpcApplication.loadBalancer = (LoadBalancer) SpiLoader.getClazz(LoadBalancer.class.getName(),
                        RpcApplication.rpcConfig.getClient().getLoadBalancerPolicy()).newInstance();
                RpcApplication.retry = (Retry) SpiLoader.getClazz(Retry.class.getName(),
                        RpcApplication.rpcConfig.getClient().getRetry()).newInstance();
                RpcApplication.tolerant = (Tolerant) SpiLoader.getClazz(Tolerant.class.getName(),
                        RpcApplication.rpcConfig.getClient().getTolerant()).newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                // 配置加载失败，使用默认值
                RpcApplication.rpcConfig = new RpcConfig();
                RpcApplication.registry = null;
            }
            //System.out.println("init!");
        }
    }
}
