# What
JRPC is a lightweight Java RPC framework. it supports service registry and discovery by the zookeeper framework.

# How
## step 1:
git clone https://github.com/dinstone/jrpc.git

## step 2:
maven install.

## step 3:
add dependency to classpath.
jrpc-core-2.1.0.jar
jrpc-mina-2.1.0.jar

service registry discovery libs :
jrpc-zookeeper-2.1.0.jar
jrpc-spring-2.1.0.jar
	
# Example
## java API
### export service:
```java

MinaServer server = new MinaServer("localhost", 1234);
server.regist(HelloService.class, new HelloServiceImpl());
server.start();
    
```

### import service:
```java

Client client = new MinaClient("localhost", 1234).setDefaultTimeout(5000);
HelloService service = client.getService(HelloService.class);
service.sayHello("dinstone");
    
```

## Spring schema
### export service:
```xml

<jrpc:server host="-" port="1234" transport="mina">
	<jrpc:registry schema="zookeeper" addresses="localhost:2181" />
</jrpc:server>
<jrpc:service interface="com.dinstone.jrpc.demo.HelloService" implement="helloService" group="product-v2.0" timeout="2000" />
<jrpc:service interface="com.dinstone.jrpc.demo.HelloService" implement="helloService" group="product-v1.0" timeout="2000" />

<bean id="helloService" class="com.dinstone.jrpc.demo.HelloServiceImpl" />

```
### import service:
```xml

<jrpc:client>
	<jrpc:registry schema="zookeeper" addresses="localhost:2181" />
</jrpc:client>
<jrpc:reference id="rhs" interface="com.dinstone.jrpc.demo.HelloService" group="product-v1.0" timeout="1000" />
	
```
