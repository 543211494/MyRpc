package com.lzy.rpc.consumer.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.lzy.rpc.RpcApplication;
import com.lzy.rpc.bean.RpcRequest;
import com.lzy.rpc.bean.RpcResponse;
import com.lzy.rpc.bean.ServiceInfo;
import com.lzy.rpc.util.JdkSerializer;
import com.lzy.rpc.util.Serializer;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

public class ServiceProxy implements InvocationHandler {

    private static final Serializer serializer = new JdkSerializer();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = new RpcRequest();
        String serviceName = method.getDeclaringClass().getName();
        /* 不用完整路径,得用接口名 */
        serviceName = serviceName.substring(serviceName.lastIndexOf('.')+1);
        rpcRequest.setServiceName(serviceName);
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setParameterTypes(method.getParameterTypes());
        rpcRequest.setArgs(args);
        Set<String> urls = new HashSet<>();
        try {
            RpcResponse rpcResponse = RpcApplication.retry.doRetry(new Callable<RpcResponse>() {
                @Override
                public RpcResponse call() throws Exception {
                    System.out.println("try!!!");
                    /* 获取服务url */
                    String url = RpcApplication.rpcConfig.getClient().getAddress();
                    if(RpcApplication.registry!=null){
                        List<ServiceInfo> services = RpcApplication.registry.serviceDiscovery(RpcApplication.rpcConfig.getClient().getServiceName());
                        /**
                         * 使用选定的负载均衡策略选择服务
                         */
                        if(services!=null&&!services.isEmpty()){
                            url = RpcApplication.loadBalancer.select(services).getAddress();
                        }
                    }
                    //System.out.println(url);
                    /* 记录请求过的url */
                    urls.add(url);
                    /* 序列化 */
                    byte[] data = serializer.serialize(rpcRequest);
                    /* 发送请求 */
                    HttpResponse httpResponse = HttpRequest.post(url)
                            .body(data)
                            .execute();
                    byte[] result = httpResponse.bodyBytes();
                    /* 反序列化 */
                    return serializer.deserialize(result, RpcResponse.class);
                }
            });
            return rpcResponse.getData();
        } catch (Exception e) {
            return RpcApplication.tolerant.tolerant(new ArrayList<>(urls),e);
        }
    }
}
