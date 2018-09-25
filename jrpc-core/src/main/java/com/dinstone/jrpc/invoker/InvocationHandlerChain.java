package com.dinstone.jrpc.invoker;

public class InvocationHandlerChain {
	
	<T> Object handle(Invocation<T> invocation) throws Throwable {
		return invocation;
	}
}
