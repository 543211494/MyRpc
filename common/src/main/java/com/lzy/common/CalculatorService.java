package com.lzy.common;

/**
 * 用户服务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @learn <a href="https://codefather.cn">编程宝典</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
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
