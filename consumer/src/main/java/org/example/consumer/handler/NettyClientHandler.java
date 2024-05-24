package org.example.consumer.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Data;

import java.util.concurrent.Callable;

/**
 * 自定义业务处理器
 */
@Data
public class NettyClientHandler extends SimpleChannelInboundHandler<String> implements Callable {

    /**
     * 用于发送消息的上下文对象
     */
    private ChannelHandlerContext ctx;

    /**
     * 发送消息
     */
    private String message;

    /**
     * 保存服务端返回结果
     */
    private String response;

    /**
     * 接收客户端回复
     */
    @Override
    protected synchronized void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
        this.response = msg;
        /**
         * 唤醒等待的线程
         */
        notify();
    }

    /**
     * 保存上下文对象，发送消息时使用
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }

    /**
     * 发送消息
     */
    @Override
    public synchronized Object call() throws Exception {
        ctx.writeAndFlush(message);
        /**
         * 等待channelRead方法获取到服务器的结果后唤醒
         */
        wait();
        return this.response;
    }
}
