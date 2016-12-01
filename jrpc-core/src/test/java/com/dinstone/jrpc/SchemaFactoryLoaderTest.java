
package com.dinstone.jrpc;

import org.junit.Test;

import com.dinstone.jrpc.transport.ConnectionFactory;

public class SchemaFactoryLoaderTest {

    @Test
    public void test() {
        SchemaFactoryLoader<ConnectionFactory> cfLoader = SchemaFactoryLoader.getInstance(ConnectionFactory.class);
        ConnectionFactory connectionFactory = cfLoader.getSchemaFactory("mina");
        System.out.println("ConnectionFactory : " + connectionFactory);
    }

}
