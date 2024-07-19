package com.lzy.provider;

import com.lzy.common.CalculatorService;

public class CalculatorServiceImpl implements CalculatorService {

    @Override
    public int add(int a, int b) {
        return a+b;
    }
}
