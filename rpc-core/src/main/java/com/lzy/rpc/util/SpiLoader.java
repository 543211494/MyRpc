package com.lzy.rpc.util;

import cn.hutool.core.io.resource.ResourceUtil;
import com.lzy.rpc.loadbalancer.LoadBalancer;
import com.lzy.rpc.loadbalancer.LoadBalancerPolicy;
import lombok.Data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SPI机制实现
 */
@Data
public class SpiLoader {

    /**
     * 存储已加载的类，其结构为：
     * 接口完整路径.实现类对应的标识:实现类
     */
    private static final Map<String, Class<?>> loaderMap = new HashMap<>();

    /**
     * SPI扫描目录
     */
    private static final String RPC_SPI_CONFIG = "META-INF/rpc/spi.properties";

    public static void init(){
        //System.out.println(LoadBalancer.class.getName());
        /**
         * 加载文件
         */
        List<URL> resources = ResourceUtil.getResources(SpiLoader.RPC_SPI_CONFIG);
        //System.out.println(resources.get(0).getPath());
        for (int i = 0;i<resources.size();i++) {
            InputStreamReader inputStreamReader = null;
            BufferedReader bufferedReader = null;
            try {
                inputStreamReader = new InputStreamReader(resources.get(i).openStream());
                bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    //System.out.println(line);
                    String[] strArray = line.split("=");
                    if (strArray.length > 1) {
                        String key = strArray[0];
                        String className = strArray[1];
                        SpiLoader.loaderMap.put(key, Class.forName(className));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                /**
                 * 释放
                 */
                try{
                    if(bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if(inputStreamReader != null){
                        inputStreamReader.close();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取指定接口的指定实现类类型
     */
    public static Class<?> getClazz(String interfaceName,String key){
        return SpiLoader.loaderMap.get(interfaceName+"."+key);
    }

//    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
//        SpiLoader.init();
//        System.out.println(SpiLoader.getClazz(LoadBalancer.class.getName(), LoadBalancerPolicy.RANDOM));
//        System.out.println(SpiLoader.getClazz(LoadBalancer.class.getName(), LoadBalancerPolicy.ROUND_ROBIN));
//        System.out.println(SpiLoader.getClazz(LoadBalancer.class.getName(), LoadBalancerPolicy.WEIGHTED_RANDOM).newInstance());
//    }
}
