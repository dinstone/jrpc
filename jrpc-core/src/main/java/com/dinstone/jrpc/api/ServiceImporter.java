
package com.dinstone.jrpc.api;

public interface ServiceImporter {

    public static final int DEFAULT_TIMEOUT = 3000;

    public abstract <T> T importService(Class<T> sic);

    public abstract <T> T importService(Class<T> sic, String group);

    public abstract <T> T importService(Class<T> sic, String group, int timeout);

    public void setDefaultTimeout(int defaultTimeout);

    public abstract void destroy();

}