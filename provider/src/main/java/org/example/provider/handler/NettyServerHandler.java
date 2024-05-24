package org.example.provider.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.provider.anno.RpcService;
import org.example.provider.bean.RpcRequest;
import org.example.provider.bean.RpcResponse;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 自定义业务处理器
 */
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<String> {

    public Map<String,Object> serviceInstance;

    /**
     * 读取客户端消息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
        RpcRequest request = JSON.parseObject(msg,RpcRequest.class);
        RpcResponse response = new RpcResponse();
        /* 业务处理 */
        try{
            Object service = serviceInstance.get(request.getClassName());
            //System.out.println(request.getClassName());
            /* 没有该方法 */
            if(service==null){
                throw new RuntimeException("The requested method does not exist");
            }else {
                /* 获取要调用的方法 */
                Method method = service.getClass().getMethod(request.getMethodName(),request.getParameterTypes());
                /* 调用方法并将调用结果保存到response对象中 */
                response.setResult(JSON.toJSONString(method.invoke(service,request.getParameters())));
            }
        }catch (Exception e){
            response.setError(e.getMessage());
        }
        channelHandlerContext.writeAndFlush(JSON.toJSONString(response));
    }

    /**
     * 构造方法
     * 将标有@RpcService的注解的bean进行缓存
     */
    public NettyServerHandler() {
        serviceInstance = new HashMap<>();
        Reflections reflections = new Reflections();
        /* 获取指定包下的所有类,并实例化对象并缓存 */
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(RpcService.class);
        try {
            for (Class<?> clazz : classes) {
                /* 实例化对象 */
                Object object = clazz.getConstructor().newInstance();
                /* 获取所实现接口的名称 */
                String className = clazz.getInterfaces()[0].getName();
                serviceInstance.put(className.substring(className.lastIndexOf('.')+1),object);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
