# What
	JRPC is a lightweight Java RPC framework.
# How
## step 1:
	git clone https://github.com/dinstone/jrpc.git

## step 2:
	maven install.

## step 3:
	add dependency to classpath.
	jrpc-core-xxx.jar
	jrpc-min-xxx.jar
	
# Example

## export service:
```java
MinaServer server = new MinaServer("localhost", 1234);
server.regist(HelloService.class, new HelloServiceImpl());
server.bind();
```

## import service:
```java
Client client = new MinaClient("localhost", 1234).setParallelCount(2).setCallTimeout(5000).build();
HelloService service = client.getProxy(HelloService.class);
service.sayHello("dinstone");
```
