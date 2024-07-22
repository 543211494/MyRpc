package com.lzy.rpc.consumer.retry;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.lzy.rpc.bean.RpcResponse;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 定时重试
 */
public class ScheduledRetry implements Retry{
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfExceptionOfType(Exception.class) //发生异常时重试
                .retryIfRuntimeException()  //发生运行时异常时重试
                .withWaitStrategy(WaitStrategies.fixedWait(2L, TimeUnit.SECONDS)) //每次重试之间等待2秒钟
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))  //尝试3次后停止
                .build();
        return retryer.call(callable);
    }
}
