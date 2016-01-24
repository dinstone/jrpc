# JRPC
--
JRPC is a lightweight Java RPC framework
--
Example
---------------
export service:

        MinaServer server = new MinaServer("localhost", 1234);
        server.regist(HelloService.class, new HelloServiceImpl());
        server.bind();
---------------
import service:

        Client client = new MinaClient("localhost", 1234).setParallelCount(2).setCallTimeout(5000).build();
        HelloService service = client.getProxy(HelloService.class);
        service.sayHello("dinstone");
