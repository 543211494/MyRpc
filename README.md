# MyRpc

## 1.简介

这是一个基于`netty`实现的简易`RPC`框架

项目结构：

```
.
├── common  公共接口，用于演示用法
│   ├── pom.xml
│   └── src
│       ├── main
│       │   └── java
│       │       └── com
│       │           └── lzy
│       │               └── common
│       │                   └── CalculatorService.java
│       └── test
│           └── java
├── consumer  客户端，用于演示用法
│   ├── pom.xml
│   └── src
│       ├── main
│       │   ├── java
│       │   │   └── com
│       │   │       └── lzy
│       │   │           └── consumer
│       │   │               └── Main.java
│       │   └── resources
│       │       └── application.properties
│       └── test
│           └── java
├── pom.xml
├── provider  服务端，用于演示用法
│   ├── pom.xml
│   └── src
│       ├── main
│       │   ├── java
│       │   │   └── com
│       │   │       └── lzy
│       │   │           └── provider
│       │   │               ├── CalculatorServiceImpl.java
│       │   │               └── Main.java
│       │   └── resources
│       │       └── application.properties
│       └── test
│           └── java
├── README.md
└── rpc-core   rpc核心实现，被客户端和服务端调用
    ├── pom.xml
    └── src
        ├── main
        │   └── java
        │       └── com
        │           └── lzy
        │               └── rpc
        │                   ├── anno   注解
        │                   │   └── RpcService.java  用于实例化服务提供类的注解
        │                   ├── bean   请求回复实体类
        │                   │   ├── RpcRequest.java
        │                   │   └── RpcResponse.java
        │                   ├── bootstrap  客户端/服务端启动类
        │                   │   ├── ConsumerBootstrap.java
        │                   │   └── ProviderBootstrap.java
        │                   ├── config  配置类
        │                   │   ├── Constant.java  常数
        │                   │   └── RpcConfig.java  配置类
        │                   ├── consumer  客户端调用部分
        │                   │   └── proxy  客户端代理类及代理工厂
        │                   │       ├── ServiceProxyFactory.java
        │                   │       └── ServiceProxy.java
        │                   ├── provider   服务端调用部分
        │                   │   ├── registry  本地map注册中心(待升级)
        │                   │   │   └── LocalRegistry.java
        │                   │   └── server   netty服务器
        │                   │       ├── NettyRpcServer.java
        │                   │       └── NettyServerHandler.java
        │                   ├── RpcApplication.java
        │                   └── util   工具类
        │                       ├── ConfigUtil.java  配置加载类
        │                       ├── JdkSerializer.java  jdk序列化类
        │                       └── Serializer.java  序列化接口
        └── test
            └── java
```



## 2.启动方法

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
