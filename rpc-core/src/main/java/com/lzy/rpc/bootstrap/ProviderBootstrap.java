package com.lzy.rpc.bootstrap;

import com.lzy.rpc.RpcApplication;
import com.lzy.rpc.anno.RpcService;
import com.lzy.rpc.bean.ServiceInfo;
import com.lzy.rpc.provider.registry.LocalRegistry;
import com.lzy.rpc.provider.server.NettyRpcServer;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.util.Set;

/**
 * 服务端启动类
 */
public class ProviderBootstrap {
    public static void run(){
        RpcApplication.init();
        //System.out.println(RpcApplication.rpcConfig);
        /**
         * 服务端启动时向服务注册中心注册
         */
        if(RpcApplication.registry!=null){
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.setServiceName(RpcApplication.rpcConfig.getServer().getServiceName());
            serviceInfo.setServiceHost(RpcApplication.rpcConfig.getServer().getHost());
            serviceInfo.setServicePort(RpcApplication.rpcConfig.getServer().getPort());
            serviceInfo.setWeight(RpcApplication.rpcConfig.getServer().getWeight());
            RpcApplication.registry.register(serviceInfo);
        }

        /**
         * 实例化注解标记的类，并注册至注册中心
         */
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages(""));
        /* 获取指定包下的所有类,并实例化对象并缓存 */
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(RpcService.class);
        try {
            for (Class<?> clazz : classes) {
                /* 实例化对象 */
                Object object = clazz.getConstructor().newInstance();
                /* 获取所实现接口的名称 */
                String serviceName = clazz.getInterfaces()[0].getName();
                serviceName = serviceName.substring(serviceName.lastIndexOf('.')+1);
                //System.out.println(serviceName);
                LocalRegistry.register(serviceName,object);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        /* 启动服务 */
        NettyRpcServer server = new NettyRpcServer();
//        System.out.println(RpcApplication.rpcConfig);
//        System.out.println(RpcApplication.rpcConfig.getServer().getPort());
        server.start(RpcApplication.rpcConfig.getServer().getPort());
    }
}
