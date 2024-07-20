package com.lzy.consumer;

import com.lzy.common.CalculatorService;
import com.lzy.rpc.bootstrap.ConsumerBootstrap;
import com.lzy.rpc.consumer.proxy.ServiceProxyFactory;

public class Main {

    public static void main(String[] args) {
        ConsumerBootstrap.init();
        CalculatorService calculatorService = ServiceProxyFactory.getProxy(CalculatorService.class);
        System.out.println(calculatorService.add(1,2));
    }
}
