
package com.dinstone.jrpc.benchmark.client;

public class CaseConfig {

    public int concurrents = 10;

    public int runTimeSeconds = 90;

    public String caseClassName = StringCaseRunnable.class.getName();

    public int dataLength = 1024;

    public String transportSchema = "netty";

    public int connectPoolSize = 10;
}
