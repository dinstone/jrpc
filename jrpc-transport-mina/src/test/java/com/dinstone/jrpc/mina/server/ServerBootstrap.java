/*
 * Copyright (C) 2014~2017 dinstone<dinstone@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dinstone.jrpc.mina.server;

import com.dinstone.jrpc.cases.HelloService;
import com.dinstone.jrpc.cases.HelloServiceImpl;
import com.dinstone.jrpc.mina.MinaServer;

/**
 * @author guojf
 * @version 1.0.0.2013-5-2
 */
public class ServerBootstrap {

    public static void main(String[] args) {
        MinaServer server = new MinaServer("localhost", 1234);
        server.regist(HelloService.class, new HelloServiceImpl());
        server.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        server.stop();
    }
}
