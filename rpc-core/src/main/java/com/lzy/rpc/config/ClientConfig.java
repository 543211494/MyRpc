package com.lzy.rpc.config;

import com.lzy.rpc.consumer.retry.RetryPolicy;
import com.lzy.rpc.consumer.tolerant.TolerantPolicy;
import com.lzy.rpc.loadbalancer.LoadBalancerPolicy;
import lombok.Data;
import lombok.ToString;

/**
 * 客户端配置
 */
@Data
@ToString
public class ClientConfig {

    /**
     * 服务名称
     */
    private String serviceName = "service";

    /**
     * 服务器主机名
     */
    private String serverHost = "127.0.0.1";

    /**
     * 服务器端口号
     */
    private Integer serverPort = 8080;

    /**
     * 负载均衡策略
     */
    private String loadBalancerPolicy = LoadBalancerPolicy.RANDOM;

    /**
     * 重试策略
     */
    private String retry = RetryPolicy.NO_RETRY;

    /**
     * 容错策略
     */
    private String tolerant = TolerantPolicy.DEFAULT_TOLERANT;

    public String getAddress(){
        return "http://"+this.serverHost+":"+this.serverPort;
    }
}
