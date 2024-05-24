package org.example.consumer.bean;

import lombok.Data;

@Data
public class RpcResponse {

    /**
     * 错误信息
     */
    private String error;

    /**
     * 返回的结果
     */
    private Object result;
}
