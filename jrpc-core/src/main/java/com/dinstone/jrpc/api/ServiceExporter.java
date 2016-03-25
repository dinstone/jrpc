
package com.dinstone.jrpc.api;

public interface ServiceExporter {

    public static final int DEFAULT_TIMEOUT = 3000;

    public abstract <T> void exportService(Class<T> serviceInterface, T serviceImplement);

    public abstract <T> void exportService(Class<T> serviceInterface, String group, T serviceImplement);

    public abstract <T> void exportService(Class<T> serviceInterface, String group, int timeout, T serviceImplement);

    public void setDefaultTimeout(int defaultTimeout);

    public abstract void destroy();

}