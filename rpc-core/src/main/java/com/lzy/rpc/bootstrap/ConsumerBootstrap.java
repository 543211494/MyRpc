package com.lzy.rpc.bootstrap;

import com.lzy.rpc.RpcApplication;

/**
 * 客户端启动类
 */
public class ConsumerBootstrap {

    public static void init(){
        RpcApplication.init();
        //System.out.println(RpcApplication.rpcConfig);
    }
}
