package com.lzy.rpc.consumer.retry;

import com.lzy.rpc.bean.RpcResponse;

import java.util.concurrent.Callable;

/**
 * 不重试
 */
public class NoRetry implements Retry{
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }
}
