package com.lzy.rpc.consumer.retry;

import com.lzy.rpc.bean.RpcResponse;

import java.util.concurrent.Callable;

/**
 * 重试接口
 */
public interface Retry {

    RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception;
}
