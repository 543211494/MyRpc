package com.lzy.rpc.bootstrap;

import com.lzy.rpc.RpcApplication;
import com.lzy.rpc.anno.RpcService;
import com.lzy.rpc.provider.registry.LocalRegistry;
import com.lzy.rpc.provider.server.NettyRpcServer;
import org.reflections.Reflections;

import java.util.Set;

/**
 * 服务端启动类
 */
public class ProviderBootstrap {
    public static void run(){
        RpcApplication.init();
        //System.out.println(RpcApplication.rpcConfig);

        /**
         * 实例化注解标记的类，并注册至注册中心
         */
        Reflections reflections = new Reflections();
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
        server.start(RpcApplication.rpcConfig.getServerPort());
    }
}
