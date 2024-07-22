package com.lzy.rpc.consumer.tolerant;

import com.lzy.rpc.bean.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DefaultTolerant implements Tolerant{

    @Override
    public RpcResponse tolerant(List<String> urls, Exception e) {
        if(urls!=null&&!urls.isEmpty()){
            System.err.println("无法提供服务的节点:");
            for(int i = 0;i<urls.size();i++){
                System.err.println(urls.get(i));
            }
        }
        e.printStackTrace();
        return null;
    }
}
