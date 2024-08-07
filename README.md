# MyRpc

## 1.简介

这是一个基于`netty+zookeeper`实现的`RPC`框架，参考了`github`上众多开源`RPC`实现，并根据自己开发经验进行了改进与整合，下面是项目结构示意图。

![](./images/1.png)



项目简介：

- 服务端基于`Netty`的`Http`服务器实现，实现服务端与客户端的高性能通信
- 客户端基于jdk动态代理为服务接口生成可发送`Http`请求的代理对象，实现远程方法的透明调用
- 利用反射实现自动化注解，服务端启动时可自动实例化服务对象存储于`ConcurrentHashMap`中
- 使用`Hutool`实现全局配置加载，所有配置均有默认值，用户可通过`.properties`修改配置
- 使用`zookeeper`作为注册中心，即使连接意外断开，临时节点也会被自动删除
- 客户端实现了多种负载均衡、重试和容错策略，可通过配置文件选择使用，提高框架可用性
- 利用反射实现`SPI`机制，用户可选择或自定义负载均衡、重试和容错策略，提高框架拓展性

项目文档：

- [项目概述](./doc/rpc-00.md)
- [RPC基本功能的实现](./doc/rpc-01.md)
- [自动注解与全局配置文件的实现](./doc/rpc-02.md)
- [服务注册/发现中心的实现](./doc/rpc-03.md)
- [负载均衡和SPI机制的实现](./doc/rpc-04.md)
- [重试和容错机制的实现](./doc/rpc-05.md)


## 2.框架使用方法

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

## 3.项目结构

```
.
├── common    公共接口，用于演示用法
│   ├── pom.xml
│   └── src
│       └── main
│           └── java
│               └── com
│                   └── lzy
│                       └── common
│                           └── CalculatorService.java
├── consumer   客户端，用于演示用法
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── lzy
│           │           └── consumer
│           │               └── Main.java
│           └── resources
│               └── application.properties
├── pom.xml
├── provider   服务端，用于演示用法
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── lzy
│           │           └── provider
│           │               ├── CalculatorServiceImpl.java
│           │               └── Main.java
│           └── resources
│               └── application.properties
├── README.md
└── rpc-core
    ├── pom.xml
    └── src
        ├── main
        │   ├── java
        │   │   └── com
        │   │       └── lzy
        │   │           └── rpc
        │   │               ├── anno  注解
        │   │               │   └── RpcService.java    用于实例化服务提供类的注解
        │   │               ├── bean
        │   │               │   ├── RpcRequest.java    RPC请求实体类
        │   │               │   ├── RpcResponse.java   RPC回复实体类
        │   │               │   └── ServiceInfo.java   服务端注册信息实体类
        │   │               ├── bootstrap
        │   │               │   ├── ConsumerBootstrap.java   客户端启动类
        │   │               │   └── ProviderBootstrap.java   服务端启动类
        │   │               ├── config
        │   │               │   ├── ClientConfig.java    客户端配置类
        │   │               │   ├── Constant.java        常数类
        │   │               │   ├── RegistryConfig.java  注册中心配置类
        │   │               │   ├── RpcConfig.java       总配置类，存放所有配置类
        │   │               │   └── ServerConfig.java    服务端配置类
        │   │               ├── consumer   客户端调用部分
        │   │               │   └── proxy  客户端代理类及代理工厂
        │   │               │   │   ├── ServiceProxyFactory.java   代理工厂类
        │   │               │   │   └── ServiceProxy.java          客户端代理类
        │   │               │   ├── retry
        │   │               │   │   ├── NoRetry.java           不重试策略实现类
        │   │               │   │   ├── Retry.java             重试策略接口
        │   │               │   │   ├── RetryPolicy.java       标识重试策略的常量
        │   │               │   │   └── ScheduledRetry.java    定时重试策略实现类
        │   │               │   └── tolerant
        │   │               │       ├── DefaultTolerant.java   默认容错策略实现类
        │   │               │       ├── Tolerant.java          容错策略接口
        │   │               │       └── TolerantPolicy.java    标识容错策略的常量
        │   │               ├── loadbalancer
        │   │               │   ├── LoadBalancer.java               负载均衡接口
        │   │               │   ├── LoadBalancerPolicy.java         标识负载均衡策略的常量
        │   │               │   ├── RandomLoadBalancer.java         随机负载均衡实现
        │   │               │   ├── RoundRobinLoadBalancer.java     带权重的随机负载均衡实现
        │   │               │   └── WeightedRandomLoadBalancer.java 轮询负载均衡实现
        │   │               ├── provider
        │   │               │   ├── registry
        │   │               │   │   ├── LocalRegistry.java   本地对象注册中心
        │   │               │   │   ├── Registry.java        注册中心接口
        │   │               │   │   └── ZooKeeperRegistry.java   zookeeper注册中心操作类
        │   │               │   └── server  netty服务器
        │   │               │       ├── NettyRpcServer.java
        │   │               │       └── NettyServerHandler.java
        │   │               ├── RpcApplication.java   存储配置类实例和注册中心类实例
        │   │               └── util
        │   │                   ├── ConfigUtil.java     配置加载类
        │   │                   ├── JdkSerializer.java  jdk序列化类
        │   │                   ├── Serializer.java     序列化接口
        │   │                   └── SpiLoader.java      spi加载器
        │   └── resources
        │       ├── log4j.properties  日志配置文件(用于关闭curator日志打印)
        │       └── META-INF
        │           └── rpc
        │               └── spi.properties   spi配置文件
        └── test
            └── java
```

## 3.配置信息说明

配置信息分为服务端、客户端两部分，位于`resource`文件夹下的`application.properties`文件中

### 3.1服务端配置信息

```properties
#服务端地址，用于向注册中心注册
rpc.server.host=127.0.0.1
#服务端服务名称，用于向注册中心注册
rpc.server.serviceName=test
#服务端端口
rpc.server.port=8081
#服务权重
rpc.server.weight=1
#是否启用注册中心
rpc.useRegistry=true
#注册中心地址
rpc.registry.host=127.0.0.1
#注册中心端口号
rpc.registry.port=2181
#连接注册中心超时时间，单位毫秒
rpc.registry.timeout=5000
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
#重试策略，目前支持no、scheduledRetry两种重试策略
rpc.client.retry=scheduledRetry
#容错策略，目前只实现了一种容错策略
rpc.client.tolerant=default
#是否启用注册中心
rpc.useRegistry=true
#注册中心地址
rpc.registry.host=127.0.0.1
#注册中心端口号
rpc.registry.port=2181
#连接注册中心超时时间，单位毫秒
rpc.registry.timeout=5000
#连接注册中心最大重试次数
rpc.registry.maxRetries=3
```

## 4.注册中心

框架实现了对服务注册发现功能的支持，目前支持使用`zookeeper`作为服务注册中心。用户可自行在`application.properties`文件中配置，并可选择开启/关闭服务注册中心。若关闭注册中心，则需在配置文件中配置服务端`ip`和端口进行连接。

以下是`zookeeper`安装方法：

官网下载地址:<a href="https://zookeeper.apache.org/releases.html#download">`https://zookeeper.apache.org/releases.html#download`</a>

将conf文件夹下的zoo_sample.cfg改名为zoo.cfg

```shell
cp zoo_sample.cfg zoo.cfg
#修改配置文件z的dataDir，例如
dataDir=/home/lzy/Downloads/zookeeper/dataDir
```

进入`bin`目录

服务端常用命令：


```shell
#启动zookeeper
./zkServer.sh start
#停止zookeeper
./zkServer.sh stop
#重启zookeeper
./zkServer.sh restart
#查看状态
./zkServer.sh status
```

客户端常用命令：

```shell
#连接服务端
./zkCli.sh -server localhost:2181
#断开连接
quit
#查看
ls
ls -s
#创建节点 create /节点 数据，例如
create /app1 data
#查看数据 get /节点
get /app1
#修改数据 set /节点 数据
set /app1 data1
#删除 delete /节点 
delete /app1
#删除全部子节点 deleteall /节点
deleteall /app1
```

## 5.SPI机制

`SPI`机制的主要作用是将接口的多个实现类路径与用户的配置关联起来，用于提高系统拓展性，以负载均衡接口为例进行说明

```java
public interface LoadBalancer {

    /**
     * 选择要调用的服务
     */
    public ServiceInfo select(List<ServiceInfo> serviceInfoList);
}
```

框架中定义了`LoadBalancer`接口，并实现了一系列负载均衡实现类。为方便用户使用，希望在`application.properties`配置使用的负载均衡策略框架可以自动实例化对应的接口实现类，这就依赖于SPI机制。

首先在`resource/META-INF/rpc/spi.properties`中写负载均衡实现类的`key`和实现类路径的对对应关系，如下所示：
```properties
com.lzy.rpc.loadbalancer.LoadBalancer.random=com.lzy.rpc.loadbalancer.RandomLoadBalancer
com.lzy.rpc.loadbalancer.LoadBalancer.roundRobin=com.lzy.rpc.loadbalancer.RoundRobinLoadBalancer
com.lzy.rpc.loadbalancer.LoadBalancer.weightedRandom=com.lzy.rpc.loadbalancer.WeightedRandomLoadBalancer
```

在服务端启动时读取该文件，将对应关系存入`map`中，当需要用到负载均衡实例时，先获取用户配置，再利用反射实例化对应的实现类

`spi`机制极大提高了系统拓展性，用户也可实现`LoadBalancer`接口自定义负载均衡策略


## 6.负载均衡机制

框架实现了随机、带权重的随机、轮询三种负载均衡，下面是轮询负载均衡实现

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
         * 当超过服务实例数量时，使用自旋锁更新currentIndex值
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

## 7.重试和容错机制

重试和容错机制同样也使用了`SPI`机制来提高拓展性

重试机制，即请求失败时根据重试策略重新请求，目前实现了不重试和定时重试两种重试策略

以下是重试机制的一个实现案例

```java
public class ScheduledRetry implements Retry{
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfExceptionOfType(Exception.class) //发生异常时重试
                .retryIfRuntimeException()  //发生运行时异常时重试
                .withWaitStrategy(WaitStrategies.fixedWait(2L, TimeUnit.SECONDS)) //每次重试之间等待2秒钟
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))  //尝试3次后停止
                .build();
        return retryer.call(callable);
    }
}
```

虽然已经有了重试机制，但重试超过了一定次数仍然失败，此时需要执行容错机制

常见容错机制有：故障转移、静默处理、快速失败等

目前实现的默认容错机制会打印无法提供服务的地址和错误信息，然后做静默处理

```java
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
```

