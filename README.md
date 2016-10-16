# What
**JRPC** is a lightweight Java RPC framework. it supports service registry and discovery by the zookeeper framework.

# Quick Start
## step 1:
```
git clone https://github.com/dinstone/jrpc.git
```
## step 2:
```
maven install.
```
## step 3:
select transport implement,add 'mina' or 'netty' dependency:
```xml
<dependency>
	<groupId>com.dinstone.jrpc</groupId>
	<artifactId>jrpc-transport-mina</artifactId>
	<version>${jrpc.version}</version>
</dependency>
```
or
```xml
<dependency>
	<groupId>com.dinstone.jrpc</groupId>
	<artifactId>jrpc-transport-netty4</artifactId>
	<version>${jrpc.version}</version>
</dependency>
```
if you need service registry and discovery, please add dependencies :
```xml
<dependency>
	<groupId>com.dinstone.jrpc</groupId>
	<artifactId>jrpc-registry-zookeeper</artifactId>
	<version>${jrpc.version}</version>
</dependency>
```
If you are integrated with Spring, please add dependencies :
```xml
<dependency>
	<groupId>com.dinstone.jrpc</groupId>
	<artifactId>jrpc-spring</artifactId>
	<version>${jrpc.version}</version>
</dependency>
```
	
# Example
For more details, please refer to the example project : [jrpc-example](https://github.com/dinstone/jrpc/tree/master/jrpc-example)

## java programming by API
### export service:
```java
Server server = new Server("localhost", 4444);
ServiceExporter serviceExporter = server.getServiceExporter();
serviceExporter.exportService(HelloService.class, new HelloServiceImpl());

System.in.read();

server.destroy();
```

### import service:
```java
Client client = new Client("localhost", 4444);
ServiceImporter serviceImporter = client.getServiceImporter();
HelloService helloService = serviceImporter.importService(HelloService.class);
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

## Case: Server[netty5] Client[netty5]
```	
1 Threads,	500 Bytes,	4824 ms,	AVG: 2072 tps
10 Threads,	500 Bytes,	17325 ms,	AVG: 5772 tps
20 Threads,	500 Bytes,	13456 ms,	AVG: 14863 tps
32 Threads,	500 Bytes,	11134 ms,	AVG: 28740 tps
1 Threads,	1024 Bytes,	1101 ms,	AVG: 9082 tps
10 Threads,	1024 Bytes,	5809 ms,	AVG: 17214 tps
20 Threads,	1024 Bytes,	9108 ms,	AVG: 21958 tps
32 Threads,	1024 Bytes,	14622 ms,	AVG: 21884 tps
40 Threads,	1024 Bytes,	17692 ms,	AVG: 22609 tps
```

## Case: Server[netty5] Client[mina]
```
1 Threads,	500 Bytes,	994 ms,	AVG: 10060 tps
10 Threads,	500 Bytes,	4516 ms,	AVG: 22143 tps
20 Threads,	500 Bytes,	8555 ms,	AVG: 23378 tps
32 Threads,	500 Bytes,	13801 ms,	AVG: 23186 tps
1 Threads,	1024 Bytes,	1027 ms,	AVG: 9737 tps
10 Threads,	1024 Bytes,	6488 ms,	AVG: 15413 tps
20 Threads,	1024 Bytes,	12776 ms,	AVG: 15654 tps
32 Threads,	1024 Bytes,	20318 ms,	AVG: 15749 tps
40 Threads,	1024 Bytes,	26149 ms,	AVG: 15296 tps
```

## Case: Server[mina] Client[mina]
```
1 Threads,	500 Bytes,	919 ms,	AVG: 10881 tps
10 Threads,	500 Bytes,	2437 ms,	AVG: 41034 tps
20 Threads,	500 Bytes,	4149 ms,	AVG: 48204 tps
32 Threads,	500 Bytes,	6694 ms,	AVG: 47804 tps
1  Threads,	1024 Bytes,	957 ms,	    AVG: 10449 tps
10 Threads,	1024 Bytes,	3168 ms,	AVG: 31565 tps
20 Threads,	1024 Bytes,	6071 ms,	AVG: 32943 tps
32 Threads,	1024 Bytes,	9485 ms,	AVG: 33737 tps
40 Threads,	1024 Bytes,	11735 ms,	AVG: 34086 tps
```

## Case: Server[mina] Client[netty5]
```
1 Threads,	500 Bytes,	1046 ms,	AVG: 9560 tps
10 Threads,	500 Bytes,	5234 ms,	AVG: 19105 tps
20 Threads,	500 Bytes,	6962 ms,	AVG: 28727 tps
32 Threads,	500 Bytes,	9193 ms,	AVG: 34809 tps
1 Threads,	1024 Bytes,	1096 ms,	AVG: 9124 tps
10 Threads,	1024 Bytes,	5069 ms,	AVG: 19727 tps
20 Threads,	1024 Bytes,	6591 ms,	AVG: 30344 tps
32 Threads,	1024 Bytes,	10526 ms,	AVG: 30400 tps
40 Threads,	1024 Bytes,	12216 ms,	AVG: 32743 tps
```
