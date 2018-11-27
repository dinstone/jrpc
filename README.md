# What
**JRPC** is a lightweight Java RPC framework. It enables quick and easy development of RPC applications. It greatly simplifies RPC programming.

# Features
## Design
* Unified API for client and server
* Support a variety of serialization protocol at the same time - Jackson and Protobuff
* Layered architecture, including API layer, Proxy layer, Invoke layer, Protocol layer, Transport layer
* Pluggable service discovery - registry with Zookeeper
* The transport layer of the extensible implementation - mina, netty4, netty5

## Ease of use
* Out of the box client-side and server-side API
* Spring integration friendly

## Performance
* Efficient custom RPC protocol
* High-performance NIO socket frame support - mina and netty

# Quick Start
select transport implement, add 'mina' or 'netty' dependency:
```xml
<dependency>
	<groupId>com.dinstone.jrpc</groupId>
	<artifactId>jrpc-transport-mina</artifactId>
	<version>3.2.0</version>
</dependency>
```
or
```xml
<dependency>
	<groupId>com.dinstone.jrpc</groupId>
	<artifactId>jrpc-transport-netty4</artifactId>
	<version>3.2.0</version>
</dependency>
```
if you need service registry and discovery, please add dependencies :
```xml
<dependency>
	<groupId>com.dinstone.jrpc</groupId>
	<artifactId>jrpc-registry-zookeeper</artifactId>
	<version>3.2.0</version>
</dependency>
```
If you are integrated with Spring, please add dependencies :
```xml
<dependency>
	<groupId>com.dinstone.jrpc</groupId>
	<artifactId>jrpc-spring</artifactId>
	<version>3.2.0</version>
</dependency>
```
	
# Example
For more details, please refer to the example project : [jrpc-example](https://github.com/dinstone/jrpc/tree/master/jrpc-example)

## java programming by API
### export service:
```java
// setting endpoint config
EndpointConfig econfig = new EndpointConfig().setEndpointName("example-service-provider");

// setting registry config
RegistryConfig rconfig = new RegistryConfig().setSchema("zookeeper").addProperty("zookeeper.node.list",
    "localhost:2181");

// setting transport config
TransportConfig tconfig = new TransportConfig().setSchema("mina").addProperty("rpc.handler.count", "2");

Server server = null;
try {
    ServerBuilder builder = new ServerBuilder().bind("localhost", 4444);
    // build server and start it
    server = builder.endpointConfig(econfig).registryConfig(rconfig).transportConfig(tconfig).build();
   server.start();
   
    // export service
    server.exportService(HelloService.class, new HelloServiceImpl());

    System.in.read();
} finally {
    if (server != null) {
        server.stop();
    }
}
```

### import service:
```java
EndpointConfig endpointConfig = new EndpointConfig().setEndpointId("consumer-1")
    .setEndpointName("example-service-consumer");

RegistryConfig registryConfig = new RegistryConfig().setSchema("zookeeper").addProperty("zookeeper.node.list",
    "localhost:2181");

TransportConfig transportConfig = new TransportConfig().setSchema("netty").setConnectPoolSize(2);

Client client = new ClientBuilder().endpointConfig(endpointConfig).registryConfig(registryConfig)
    .transportConfig(transportConfig).build();

HelloService helloService = client.importService(HelloService.class);
helloService.sayHello("dinstone");

client.destroy();
```

## declarative programming by Spring
### export service:
```xml
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:jrpc="http://www.dinstone.com/schema/jrpc"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd 
	http://www.dinstone.com/schema/jrpc http://www.dinstone.com/schema/jrpc-3.0.xsd">

	<jrpc:server>
		<jrpc:registry schema="zookeeper">
			<jrpc:property key="zookeeper.node.list" value="localhost:2181" />
		</jrpc:registry>
		<jrpc:transport schema="mina" address="-:2001" />
	</jrpc:server>
	<jrpc:service interface="com.dinstone.jrpc.example.HelloService" implement="helloService" group="product-v1.0" timeout="2000" />

	<bean id="helloService" class="com.dinstone.jrpc.example.HelloServiceImpl" />
</beans>
```

### import service:
```xml
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:jrpc="http://www.dinstone.com/schema/jrpc"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd 
	http://www.dinstone.com/schema/jrpc http://www.dinstone.com/schema/jrpc-3.0.xsd">
	
	<jrpc:client name="netty-client">
		<jrpc:registry schema="zookeeper">
			<jrpc:property key="zookeeper.node.list" value="localhost:2181" />
		</jrpc:registry>
		<jrpc:transport schema="netty">
			<jrpc:property key="rpc.serialize.type" value="2" />
		</jrpc:transport>
	</jrpc:client>
	<jrpc:reference id="rhsv1" interface="com.dinstone.jrpc.example.HelloService" group="product-v1.0" />
</beans>
```

# Test Result
## JVM Parameter
```
-server -Xmx1g -Xms1g -Xmn712m -XX:PermSize=128m -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70
```

## Benchmark Test
For more details, please refer to the benchmark project : [jrpc-benchmark](https://github.com/dinstone/jrpc/tree/master/jrpc-benchmark)
