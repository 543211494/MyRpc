package com.lzy.provider;

import com.lzy.common.CalculatorService;
import com.lzy.rpc.anno.RpcService;

@RpcService
public class CalculatorServiceImpl implements CalculatorService {

    @Override
    public int add(int a, int b) {
        return a+b;
    }
}
