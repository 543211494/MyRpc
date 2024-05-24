package org.example.consumer;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.example.consumer.api.Translator;
import org.example.consumer.bean.RpcRequest;
import org.example.consumer.bean.RpcResponse;
import org.example.consumer.handler.NettyClientHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NettyRpcClient {

    /**
     * 创建线程池
     */
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * 业务处理器实例
     */
    private NettyClientHandler nettyClientHandler;

    /**
     * 缓存代理对象，实现复用
     */
    Map<Class<?>, Object> serviceProxy = new HashMap<>();

    /**
     * 服务端地址
     */
    private String hostName;

    /**
     * 服务端端口
     */
    private int port;

    public NettyRpcClient(String hostname, int port){
        this.hostName = hostname;
        this.port = port;
    }

    /**
     * 初始化客户端
     */
    public void initClient(){
        nettyClientHandler = new NettyClientHandler();
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast(new StringDecoder());
                                pipeline.addLast(new StringEncoder());
                                pipeline.addLast(nettyClientHandler);
                            }
                        }
                );
        try {
            /* 与服务端建立连接 */
            ChannelFuture channelFuture = bootstrap.connect(this.hostName, this.port).sync();
        } catch (Exception e) {
            e.printStackTrace();
            group.shutdownGracefully();
        }
    }

    /**
     * 发送消息
     * @param msg
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public Object send(String msg) throws ExecutionException, InterruptedException {
        /* 设置发送的消息 */
        nettyClientHandler.setMessage(msg);
        /* 提交给线程池调用，返回值即为nettyClientHandler中call方法的返回值 */
        return executor.submit(nettyClientHandler).get();
    }

    /**
     * 获取代理对象
     * @return
     */
    public Object getProxy(Class<?> serivceClass){
        Object proxy = serviceProxy.get(serivceClass);
        if(proxy==null){
            /**
             * 第一个参数Thread.currentThread().getContextClassLoader()是类加载器，用于加载动态代理类
             * 第二个参数new Class<?>[]{serivceClass}是接口数组，指定了代理类要实现的接口
             * 第三个参数是一个InvocationHandler接口的实现，它定义了代理类的调用处理程序，即当代理对象调用方法时，会被转发到这个处理程序中进行处理。
             */
            proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class<?>[]{serivceClass},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if(nettyClientHandler==null){
                                initClient();
                            }
                            /* 封装请求对象 */
                            RpcRequest request = new RpcRequest();
                            String className = method.getDeclaringClass().getName();
                            request.setClassName(className.substring(className.lastIndexOf('.')+1));
                            //System.out.println(request.getClassName());
                            request.setMethodName(method.getName());
                            request.setParameterTypes(method.getParameterTypes());
                            request.setParameters(args);
                            /* 发送消息并解析回复 */
                            RpcResponse response = null;
                            try{
                                /* 发送消息 */
                                response = JSON.parseObject((String)send(JSON.toJSONString(request)),RpcResponse.class);
                                if(response.getError()!=null){
                                    throw new RuntimeException(response.getError());
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            return JSON.parseObject(response.getResult().toString(),method.getReturnType());
                        }
                    });
            /* 缓存代理以便下次使用 */
            serviceProxy.put(serivceClass,proxy);
        }
        return proxy;
    }

    public static void main(String[] args) {
        NettyRpcClient rpcClient = new NettyRpcClient("127.0.0.1",8000);
        Translator translator = (Translator)rpcClient.getProxy(Translator.class);
        System.out.println(translator.translate("food"));
        System.out.println(translator.translate("English"));
        System.out.println(translator.translate("cmn"));
    }
}

