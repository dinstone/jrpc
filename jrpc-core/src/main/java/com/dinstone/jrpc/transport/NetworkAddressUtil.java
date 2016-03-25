
package com.dinstone.jrpc.transport;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public class NetworkAddressUtil {

    public static final List<InetAddress> getPrivateInetInetAddress() throws SocketException {
        List<InetAddress> inetAddresses = new LinkedList<InetAddress>();
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
        List<InetAddress> inetAddresses = new LinkedList<InetAddress>();
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

}
