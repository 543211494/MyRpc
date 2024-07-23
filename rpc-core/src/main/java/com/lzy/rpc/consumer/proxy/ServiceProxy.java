package com.lzy.rpc.consumer.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.lzy.rpc.bean.RpcRequest;
import com.lzy.rpc.bean.RpcResponse;
import com.lzy.rpc.util.JdkSerializer;
import com.lzy.rpc.util.Serializer;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public class ServiceProxy implements InvocationHandler {

    /**
     * 序列化类
     */
    private static final Serializer serializer = new JdkSerializer();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        /* 构造rpc倾请求 */
        RpcRequest rpcRequest = new RpcRequest();
        /* 获取被调用方法所属接口的名称及路径 */
        rpcRequest.setServiceName(method.getDeclaringClass().getName());
        /* 获取被调用方法的方法名 */
        rpcRequest.setMethodName(method.getName());
        /* 获取被调用方法 */
        rpcRequest.setParameterTypes(method.getParameterTypes());
        rpcRequest.setArgs(args);

        try {
            /* 序列化 */
            byte[] data = serializer.serialize(rpcRequest);
            /* 发送请求，此处暂时将路径写死，待后续实现注册中心后修改 */
            try (HttpResponse httpResponse = HttpRequest.post("http://127.0.0.1:8080")
                    .body(data)
                    .execute()) {
                byte[] result = httpResponse.bodyBytes();
                // 反序列化
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return rpcResponse.getData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
