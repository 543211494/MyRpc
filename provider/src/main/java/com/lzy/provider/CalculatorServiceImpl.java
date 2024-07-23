package com.lzy.provider;

import com.lzy.common.CalculatorService;
import com.lzy.rpc.anno.RpcService;

@RpcService
public class CalculatorServiceImpl implements CalculatorService {

    @Override
    public Integer add(Integer a, Integer b) {
        return a+b;
    }
}
