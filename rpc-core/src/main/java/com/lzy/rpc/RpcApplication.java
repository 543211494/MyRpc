package com.lzy.rpc;

import com.lzy.rpc.config.Constant;
import com.lzy.rpc.config.RpcConfig;
import com.lzy.rpc.util.ConfigUtil;

/**
 * 框架类，负责执行初始化操作
 */
public class RpcApplication {

    public static volatile RpcConfig rpcConfig;

    public static void init(){
        if(RpcApplication.rpcConfig==null){
            try {
                RpcApplication.rpcConfig = ConfigUtil.loadConfig(RpcConfig.class, Constant.DEFAULT_CONFIG_PREFIX);
            } catch (Exception e) {
                // 配置加载失败，使用默认值
                RpcApplication.rpcConfig = new RpcConfig();
            }
        }
    }
}
