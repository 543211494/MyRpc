package com.lzy.rpc.util;

import cn.hutool.setting.dialect.Props;

/**
 * 配置加载工具类
 */
public class ConfigUtil {

    /**
     *
     * @param tClass 配置类的类型
     * @param prefix 前缀
     * @param <T>
     * @return
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix) {
        Props props = new Props("application.properties");
        return props.toBean(tClass, prefix);
    }
}
