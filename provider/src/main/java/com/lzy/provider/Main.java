package com.lzy.provider;

import com.lzy.common.CalculatorService;
import com.lzy.rpc.provider.registry.LocalRegistry;
import com.lzy.rpc.provider.server.NettyRpcServer;

public class Main {

    public static void main(String[] args) {
        /**
         * 注册提供服务的实体类
         */
        LocalRegistry.register(CalculatorService.class.getName(),CalculatorServiceImpl.class);
        NettyRpcServer server = new NettyRpcServer();
        server.start(8080);
    }
}
