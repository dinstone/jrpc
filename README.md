# What
JRPC is a lightweight Java RPC framework. it supports service registry and discovery by the zookeeper framework.

# Quick Start
## step 1:
git clone https://github.com/dinstone/jrpc.git

## step 2:
maven install.

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
## java API
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

## Spring integration
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

	<bean id="helloService" class="com.dinstone.jrpc.demo.HelloServiceImpl" />
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
