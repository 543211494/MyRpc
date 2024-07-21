# MyRpc

## 1.简介

这是一个基于`netty`实现的`RPC`框架，参考了`github`上众多开源`RPC`实现，并根据自己开发经验进行了改进与整合

项目结构：

```
.
├── common    公共接口，用于演示用法
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
├── consumer   客户端，用于演示用法
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
├── provider   服务端，用于演示用法
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
        │   │               ├── bean  实体类
        │   │               │   ├── RpcRequest.java    RPC请求实体类
        │   │               │   ├── RpcResponse.java   RPC回复实体类
        │   │               │   └── ServiceInfo.java   服务端注册信息实体类
        │   │               ├── bootstrap  客户端/服务端启动类
        │   │               │   ├── ConsumerBootstrap.java
        │   │               │   └── ProviderBootstrap.java
        │   │               ├── config
        │   │               │   ├── ClientConfig.java    客户端配置类
        │   │               │   ├── Constant.java        常数类
        │   │               │   ├── RegistryConfig.java  注册中心配置类
        │   │               │   ├── RpcConfig.java       总配置类，存放所有配置类
        │   │               │   └── ServerConfig.java    服务端配置类
        │   │               ├── consumer   客户端调用部分
        │   │               │   └── proxy  客户端代理类及代理工厂
        │   │               │       ├── ServiceProxyFactory.java
        │   │               │       └── ServiceProxy.java
        │   │               ├── provider
        │   │               │   ├── registry
        │   │               │   │   ├── LocalRegistry.java   本地对象注册中心
        │   │               │   │   ├── Registry.java        注册中心接口
        │   │               │   │   └── ZooKeeperRegistry.java   zookeeper注册中心操作类
        │   │               │   └── server  netty服务器
        │   │               │       ├── NettyRpcServer.java
        │   │               │       └── NettyServerHandler.java
        │   │               ├── RpcApplication.java  存储配置类实例和注册中心类实例
        │   │               └── util   工具类
        │   │                   ├── ConfigUtil.java     配置加载类
        │   │                   ├── JdkSerializer.java  jdk序列化类
        │   │                   └── Serializer.java     序列化接口
        │   └── resources
        │       └── log4j.properties  日志配置文件(用于关闭curator日志打印)
        └── test
            └── java
```

## 2.配置信息

配置信息写于`resource`文件夹下的`application.properties`文件中

### 2.1服务端配置信息

```properties
#服务端地址，用于向注册中心注册
rpc.server.host=127.0.0.1
#服务端服务名称，用于向注册中心注册
rpc.server.serviceName=test
#服务端端口
rpc.server.port=8081
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

### 2.2客户端配置信息

```properties
#要连接的服务端服务名称，用于向注册中心发现服务
rpc.client.serviceName=test
#要连接的服务端地址，用于不启用注册中心时连接服务端
rpc.client.serverHost=127.0.0.1
#要连接的服务端端口号，用于不启用注册中心时连接服务端
rpc.client.serverPort=8081
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

## 3.注册中心

本次更新了对服务注册发现功能的支持，目前支持使用`zookeeper`作为服务注册中心。用户可自行在`application.properties`文件中配置，并可选择开启/关闭服务注册中心。若关闭注册中心，则需在配置文件中配置服务端`ip`和端口进行连接。

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
