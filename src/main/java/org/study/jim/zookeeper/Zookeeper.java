package org.study.jim.zookeeper;

import java.io.IOException;

public class Zookeeper {
    protected final ClientCnxn cnxn;
    public Zookeeper(int port) throws IOException {
        cnxn = new ClientCnxn(new CnxnSocketNIO(),port);
        cnxn.start();
    }
    public void create(String content){
        cnxn.submitRequest(new ClientCnxn.Packet(content));
    }
}
