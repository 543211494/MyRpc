package com.lzy.rpc.consumer.tolerant;

import com.lzy.rpc.bean.RpcResponse;

import java.util.List;

/**
 * 容错接口
 */
public interface Tolerant {

    /**
     *
     * @param urls   无法提供服务的节点地址
     * @param e      异常
     * @return
     */
    public RpcResponse tolerant(List<String> urls,Exception e);
}
