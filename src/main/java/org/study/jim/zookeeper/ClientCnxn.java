package org.study.jim.zookeeper;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientCnxn {
    private final LinkedBlockingDeque<Packet> outgoingQueue = new LinkedBlockingDeque();
    final SendThread sendThread;
    final int port;
    public ClientCnxn(CnxnSocketNIO cnxnSocketNIO,int port){
        this.port = port;
        sendThread = new SendThread(cnxnSocketNIO,port);
    }
    public void start(){
        sendThread.start();
    }

    public void submitRequest(Packet packet) {
        outgoingQueue.add(packet);
        sendThread.getCnxnSocketNIO().packetAdded();
    }

    class SendThread extends Thread {
        private final CnxnSocketNIO cnxnSocketNIO;
        private int port;
        public SendThread(CnxnSocketNIO cnxnSocketNIO,int port){
            this.cnxnSocketNIO = cnxnSocketNIO;
            this.port = port;
        }
        @Override
        public void run() {
            try {
                while (true){
                    if(!cnxnSocketNIO.isConnected()){
                        cnxnSocketNIO.registerAndConnect(port);
                    }else{
                        cnxnSocketNIO.doTransport(outgoingQueue);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        CnxnSocketNIO getCnxnSocketNIO(){
            return cnxnSocketNIO;
        }
    }
    static class Packet{
        String content;
        public Packet(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }
}
