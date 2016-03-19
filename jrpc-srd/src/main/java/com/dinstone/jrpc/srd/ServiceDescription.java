
package com.dinstone.jrpc.srd;

import java.io.Serializable;

/**
 * service description
 * 
 * @author dinstone
 * @version 1.0.0
 */
public class ServiceDescription implements Serializable {

    /**  */
    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private String group;

    private String host;

    private Integer port;

    private String uri;

    private long registryTime;

    private ServiceAttribute serviceAttribute;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public long getRegistryTime() {
        return registryTime;
    }

    public void setRegistryTime(long registryTime) {
        this.registryTime = registryTime;
    }

    public ServiceAttribute getServiceAttribute() {
        return serviceAttribute;
    }

    public void setServiceAttribute(ServiceAttribute serviceAttribute) {
        this.serviceAttribute = serviceAttribute;
    }

    @Override
    public String toString() {
        return "ServiceDescription [id=" + id + ", name=" + name + ", group=" + group + ", host=" + host + ", port="
                + port + ", uri=" + uri + ", registryTime=" + registryTime + ", serviceAttribute=" + serviceAttribute
                + "]";
    }

}
