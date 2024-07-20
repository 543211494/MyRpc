package com.lzy.rpc.provider.server;

import com.lzy.rpc.bean.RpcRequest;
import com.lzy.rpc.bean.RpcResponse;
import com.lzy.rpc.provider.registry.LocalRegistry;
import com.lzy.rpc.util.JdkSerializer;
import com.lzy.rpc.util.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.lang.reflect.Method;

public class NettyServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Serializer serializer = new JdkSerializer();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        ByteBuf buffer = fullHttpRequest.content();
        byte[] data = new byte[buffer.readableBytes()];
        buffer.readBytes(data);
        RpcRequest rpcRequest = serializer.deserialize(data,RpcRequest.class);
        // 构造响应结果对象
        RpcResponse rpcResponse = new RpcResponse();
        // 如果请求为 null，直接返回
        if (rpcRequest == null) {
            rpcResponse.setMessage("rpcRequest is null");
        }else{
            /* 通过反射调用相应的方法 */
            Object service = LocalRegistry.get(rpcRequest.getServiceName());
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
            Object result = method.invoke(service, rpcRequest.getArgs());
            // 封装返回结果
            rpcResponse.setData(result);
            rpcResponse.setDataType(method.getReturnType());
            rpcResponse.setMessage("ok");
        }
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.content().writeBytes(serializer.serialize(rpcResponse));
        // 设置响应头
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        channelHandlerContext.writeAndFlush(response);
    }
}
