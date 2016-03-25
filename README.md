# What
	JRPC is a lightweight Java RPC framework. it supports service registry and discovery by the zookeeper framework.
# How
## step 1:
	git clone https://github.com/dinstone/jrpc.git

## step 2:
	maven install.

## step 3:
	add dependency to classpath.
	jrpc-core-2.0.0.jar
	jrpc-mina-2.0.0.jar
	
	service registry discovery libs :
	jrpc-srd-2.0.0.jar
	jrpc-cluster-2.0.0.jar
	
# Example

## export service:
```java
	MinaServer server = new MinaServer("localhost", 1234);
    server.regist(HelloService.class, new HelloServiceImpl());
    server.start();
```

## import service:
```java
	Client client = new MinaClient("localhost", 1234).setDefaultTimeout(5000);
	HelloService service = client.getService(HelloService.class);
    service.sayHello("dinstone");
```
