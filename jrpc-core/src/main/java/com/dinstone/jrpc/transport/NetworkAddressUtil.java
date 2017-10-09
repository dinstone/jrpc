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
package com.dinstone.jrpc.transport;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public class NetworkAddressUtil {

    public static final List<InetAddress> getPrivateInetInetAddress() throws SocketException {
        List<InetAddress> inetAddresses = new LinkedList<>();
        for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) {
            NetworkInterface netInterface = e.nextElement();
            if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                continue;
            }

            for (InterfaceAddress address : netInterface.getInterfaceAddresses()) {
                InetAddress inetAddress = address.getAddress();
                if (inetAddress.isSiteLocalAddress() && !inetAddress.isLoopbackAddress()
                        && (inetAddress instanceof Inet4Address)) {
                    inetAddresses.add(inetAddress);
                }
            }
        }

        return inetAddresses;
    }

    public static final List<InetAddress> getPublicInetInetAddress() throws SocketException {
        List<InetAddress> inetAddresses = new LinkedList<>();
        for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) {
            NetworkInterface netInterface = e.nextElement();
            if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                continue;
            }

            for (InterfaceAddress address : netInterface.getInterfaceAddresses()) {
                InetAddress inetAddress = address.getAddress();
                if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()
                        && !inetAddress.isSiteLocalAddress() && (inetAddress instanceof Inet4Address)) {
                    inetAddresses.add(inetAddress);
                }
            }
        }

        return inetAddresses;
    }

    public static String addressLabel(SocketAddress localAddress, SocketAddress remoteAddress) {
        InetSocketAddress local = (InetSocketAddress) localAddress;
        InetSocketAddress remote = (InetSocketAddress) remoteAddress;

        String key = "";
        if (local == null || local.getAddress() == null) {
            key += "local-";
        } else {
            key += local.getAddress().getHostAddress() + ":" + local.getPort() + "-";
        }

        if (remote == null || remote.getAddress() == null) {
            key += "remote";
        } else {
            key += remote.getAddress().getHostAddress() + ":" + remote.getPort();
        }

        return key;
    }

}
