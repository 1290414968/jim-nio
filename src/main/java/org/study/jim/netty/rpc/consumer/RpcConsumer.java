package org.study.jim.netty.rpc.consumer;

import org.study.jim.netty.rpc.api.IHello;
import org.study.jim.netty.rpc.consumer.proxy.RpcProxy;

public class RpcConsumer {
	
    public static void main(String [] args){  
        IHello rpcHello = RpcProxy.create(IHello.class);
        System.out.println(rpcHello.say("jim"));
    }
    
}
