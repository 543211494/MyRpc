# 负载均衡和SPI机制的实现

该部分的源码地址：<a href="https://github.com/543211494/MyRpc/tree/MyRpc-loadbalance-spi">https://github.com/543211494/MyRpc/tree/MyRpc-loadbalance-spi</a>

下一章：<a href="./rpc-05.md">重试和容错机制的实现</a>

## 1.负载均衡

在上个版本实现的服务注册/发现中心中存在一个问题：当多个相同服务都注册至`Zookeeper`中时该如何选择服务。

因此，我们需要实现负载均衡策略在服务列表中进行选择，目前实现了随机负载均衡，带权重的随机负载均衡和轮询负载均衡策略。

首先定义一个负载均衡接口便于后续拓展：

```java
public interface LoadBalancer {

    /**
     * 选择要调用的服务
     */
    public ServiceInfo select(List<ServiceInfo> serviceInfoList);
}
```

以下是目前实现的负载均衡策略，用户可根据需求选择使用：

随机负载均衡

```java
public class RandomLoadBalancer implements LoadBalancer{

    private final Random random = new Random();

    @Override
    public ServiceInfo select(List<ServiceInfo> serviceInfoList) {
        int index = random.nextInt(serviceInfoList.size());
        return serviceInfoList.get(index);
    }
}
```

带权重的随机负载均衡

```java
public class WeightedRandomLoadBalancer implements LoadBalancer {

    private final Random random = new Random();

    @Override
    public ServiceInfo select(List<ServiceInfo> serviceInfoList) {
        int totalWeight = 0;
        for(int i = 0;i<serviceInfoList.size();i++){
            totalWeight += serviceInfoList.get(i).getWeight();
        }
        int number = random.nextInt(totalWeight);
        for(int i = 0;i<serviceInfoList.size();i++){
            number -= serviceInfoList.get(i).getWeight();
            if(number<0){
                return serviceInfoList.get(i);
            }
        }
        return serviceInfoList.get(0);
    }
}
```

轮询负载均衡

```java
public class RoundRobinLoadBalancer implements LoadBalancer{
    /**
     * 当前轮询的下标
     */
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    @Override
    public ServiceInfo select(List<ServiceInfo> serviceMetaInfoList) {
        int size = serviceMetaInfoList.size();
        int current = currentIndex.getAndIncrement();
        int index = current%size;
        /**
         * 使用自旋锁更新currentIndex值
         */
        if(current>=size){
            int next;
            do {
                current = currentIndex.get();
                next = (current) % size;
            } while (!currentIndex.compareAndSet(current, next));
        }
        return serviceMetaInfoList.get(index);
    }
}
```

当客户端查询到服务端列表时，使用负载均衡策略进行选择

```java
public class ServiceProxy implements InvocationHandler {

    private static final Serializer serializer = new JdkSerializer();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = new RpcRequest();
        String serviceName = method.getDeclaringClass().getName();
        /* 不能用完整路径,得用接口名 */
        serviceName = serviceName.substring(serviceName.lastIndexOf('.')+1);
        rpcRequest.setServiceName(serviceName);
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setParameterTypes(method.getParameterTypes());
        rpcRequest.setArgs(args);

        try {
            /* 序列化 */
            byte[] data = serializer.serialize(rpcRequest);
            /* 发送请求 */
            String url = RpcApplication.rpcConfig.getClient().getAddress();
            if(RpcApplication.registry!=null){
                List<ServiceInfo> services = RpcApplication.registry.serviceDiscovery(RpcApplication.rpcConfig.getClient().getServiceName());
                /**
                 * 使用选定的负载均衡策略选择服务
                 */
                if(services!=null&&!services.isEmpty()){
                    url = RpcApplication.loadBalancer.select(services).getAddress();
                }
            }
            try (HttpResponse httpResponse = HttpRequest.post(url)
                    .body(data)
                    .execute()) {
                byte[] result = httpResponse.bodyBytes();
                // 反序列化
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return rpcResponse.getData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
```

## 2.SPI机制

`SPI`机制的主要作用是将接口的多个实现类路径与用户的配置`key`关联起来，用于提高系统拓展性，以负载均衡接口为例进行说明。

框架中定义了`LoadBalancer`接口，并实现了一系列负载均衡实现类。为方便用户使用，希望在`application.properties`配置使用的负载均衡策略框架可以自动实例化对应的接口实现类，这就依赖于SPI机制。

首先在`resource/META-INF/rpc/spi.properties`中写负载均衡实现类的`key`和实现类路径的对对应关系，如下所示：

```properties
com.lzy.rpc.loadbalancer.LoadBalancer.random=com.lzy.rpc.loadbalancer.RandomLoadBalancer
com.lzy.rpc.loadbalancer.LoadBalancer.roundRobin=com.lzy.rpc.loadbalancer.RoundRobinLoadBalancer
com.lzy.rpc.loadbalancer.LoadBalancer.weightedRandom=com.lzy.rpc.loadbalancer.WeightedRandomLoadBalancer
```

在服务端启动时读取该文件，将对应关系存入`map`中，当需要用到负载均衡实例时，先获取用户配置，再利用反射实例化对应的实现类。

例如，用户在配置文件中写：

```properties
rpc.client.loadBalancerPolicy=random
```

那么就可以通过`SPI`可以的知要实例化的类是`com.lzy.rpc.loadbalancer.RandomLoadBalancer`，当需要使用负载均衡策略时即可实例化该类。`SPI`机制极大提高了系统拓展性，用户也可实现`LoadBalancer`接口自定义负载均衡策略

以下是`SPI`加载器的实现：

```java
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
}
```

## 3.配置信息

配置信息写于`resource`文件夹下的`application.properties`文件中，目前版本更新了一些配置参数，以下是详细介绍：

### 3.1服务端配置信息

```properties
#服务端地址，用于向注册中心注册
rpc.server.host=127.0.0.1
#服务端服务名称，用于向注册中心注册
rpc.server.serviceName=test
#服务端端口
rpc.server.port=8081
#服务权重
rpc.server.weight=2
#是否启用注册中心
rpc.useRegistry=true
#注册中心地址
rpc.registry.host=127.0.0.1
#注册中心端口号
rpc.registry.port=2181
#连接注册中心超时时间，单位毫秒
rpc.registry.timeout=10000
#连接注册中心最大重试次数
rpc.registry.maxRetries=3
```

### 3.2客户端配置信息

```properties
#要连接的服务端服务名称，用于向注册中心发现服务
rpc.client.serviceName=test
#要连接的服务端地址，用于不启用注册中心时连接服务端
rpc.client.serverHost=127.0.0.1
#要连接的服务端端口号，用于不启用注册中心时连接服务端
rpc.client.serverPort=8081
#负载均衡策略，目前支持random、weightedRandom、roundRobin三种负载均衡策略
rpc.client.loadBalancerPolicy=random
#是否启用注册中心
rpc.useRegistry=true
#注册中心地址
rpc.registry.host=127.0.0.1
#注册中心端口号
rpc.registry.port=2181
#连接注册中心超时时间，单位毫秒
rpc.registry.timeout=10000
#连接注册中心最大重试次数
rpc.registry.maxRetries=3
```

## 4.启动方法

项目为标准的`Maven`项目

`provider`下为服务端代码，执行其中的`main`函数即可启动服务端，服务端启动方式如下

```java
public static void main(String[] args) {
    ProviderBootstrap.run();
}
```

目前服务端以实现了自动注解和读取配置文件`application.properties`

只需要在实现接口的类上加上注解`@RpcService`即可自动注册，无需手动注册，例如

```java
@RpcService
public class CalculatorServiceImpl implements CalculatorService {

    @Override
    public int add(int a, int b) {
        return a+b;
    }
}
```

`consumer`文件夹下为客户端代码，通过代理工厂获取代理类，从而调用服务端的远程方法

```java
public static void main(String[] args) {
    ConsumerBootstrap.init();
    CalculatorService calculatorService = ServiceProxyFactory.getProxy(CalculatorService.class);
    System.out.println(calculatorService.add(1,2));
}
```

## 附录：项目文件结构

```
.
├── common    公共接口，用于演示用法
│   ├── pom.xml
│   └── src
│       └── main
│           └── java
│               └── com
│                   └── lzy
│                       └── common
│                           └── CalculatorService.java
├── consumer   客户端，用于演示用法
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── lzy
│           │           └── consumer
│           │               └── Main.java
│           └── resources
│               └── application.properties
├── pom.xml
├── provider   服务端，用于演示用法
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── lzy
│           │           └── provider
│           │               ├── CalculatorServiceImpl.java
│           │               └── Main.java
│           └── resources
│               └── application.properties
├── README.md
└── rpc-core
    ├── pom.xml
    └── src
        ├── main
        │   ├── java
        │   │   └── com
        │   │       └── lzy
        │   │           └── rpc
        │   │               ├── anno  注解
        │   │               │   └── RpcService.java    用于实例化服务提供类的注解
        │   │               ├── bean
        │   │               │   ├── RpcRequest.java    RPC请求实体类
        │   │               │   ├── RpcResponse.java   RPC回复实体类
        │   │               │   └── ServiceInfo.java   服务端注册信息实体类
        │   │               ├── bootstrap  客户端/服务端启动类
        │   │               │   ├── ConsumerBootstrap.java
        │   │               │   └── ProviderBootstrap.java
        │   │               ├── config
        │   │               │   ├── ClientConfig.java    客户端配置类
        │   │               │   ├── Constant.java        常数类
        │   │               │   ├── RegistryConfig.java  注册中心配置类
        │   │               │   ├── RpcConfig.java       总配置类，存放所有配置类
        │   │               │   └── ServerConfig.java    服务端配置类
        │   │               ├── consumer   客户端调用部分
        │   │               │   └── proxy  客户端代理类及代理工厂
        │   │               │       ├── ServiceProxyFactory.java
        │   │               │       └── ServiceProxy.java
        │   │               ├── loadbalancer
        │   │               │   ├── LoadBalancer.java               负载均衡接口
        │   │               │   ├── LoadBalancerPolicy.java         标识负载均衡策略的常量
        │   │               │   ├── RandomLoadBalancer.java         随机负载均衡实现
        │   │               │   ├── RoundRobinLoadBalancer.java     带权重的随机负载均衡实现
        │   │               │   └── WeightedRandomLoadBalancer.java 轮询负载均衡实现
        │   │               ├── provider
        │   │               │   ├── registry
        │   │               │   │   ├── LocalRegistry.java   本地对象注册中心
        │   │               │   │   ├── Registry.java        注册中心接口
        │   │               │   │   └── ZooKeeperRegistry.java   zookeeper注册中心操作类
        │   │               │   └── server  netty服务器
        │   │               │       ├── NettyRpcServer.java
        │   │               │       └── NettyServerHandler.java
        │   │               ├── RpcApplication.java   存储配置类实例和注册中心类实例
        │   │               └── util
        │   │                   ├── ConfigUtil.java     配置加载类
        │   │                   ├── JdkSerializer.java  jdk序列化类
        │   │                   ├── Serializer.java     序列化接口
        │   │                   └── SpiLoader.java      spi加载器
        │   └── resources
        │       ├── log4j.properties  日志配置文件(用于关闭curator日志打印)
        │       └── META-INF
        │           └── rpc
        │               └── spi.properties   spi配置文件
        └── test
            └── java
```