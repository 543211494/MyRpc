package com.lzy.common;

/**
 * 用户服务
 */
public interface CalculatorService {

    /**
     * 获取用户
     */
    public int add(int a,int b);

    /**
     * 用于测试 mock 接口返回值
     */
    default short getNumber() {
        return 1;
    }
}
