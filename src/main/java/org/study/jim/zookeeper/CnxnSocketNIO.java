package org.study.jim.zookeeper;
import org.study.jim.demo.CharsetHelper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

public class CnxnSocketNIO {
    private final Selector selector = Selector.open();
    private SelectionKey sockKey;
    void packetAdded(){
        wakeupCnxn();
    }
    private synchronized void wakeupCnxn() {
        selector.wakeup();
    }
    public CnxnSocketNIO() throws IOException {
    }
    void doTransport(LinkedBlockingDeque<ClientCnxn.Packet> outgoingQueue) throws IOException {
        Set<SelectionKey> selected;
        synchronized (this) {
            selected = selector.selectedKeys();
        }
        for (SelectionKey k : selected) {
            SocketChannel sc = ((SocketChannel) k.channel());

            if(sockKey.isReadable()){
                ByteBuffer byteBuffer = ByteBuffer.allocate(128);
                sc.read(byteBuffer);
                byteBuffer.flip();
                CharBuffer charBuffer = CharsetHelper.decode(byteBuffer);
                String answer = charBuffer.toString();
                System.out.println(Thread.currentThread().getName() + "---" + answer);
            }

            if ((k.readyOps() & SelectionKey.OP_CONNECT) != 0) {

            } else if ((k.readyOps() & (SelectionKey.OP_READ | SelectionKey.OP_WRITE)) != 0) {
                doIO(outgoingQueue);
            }
        }
    }
    private void doIO(LinkedBlockingDeque<ClientCnxn.Packet> outgoingQueue) throws IOException {
        SocketChannel sock = (SocketChannel) sockKey.channel();
        if (sock == null) {
            throw new IOException("Socket is null!");
        }
        ClientCnxn.Packet packet =  outgoingQueue.getFirst();
        if(packet!=null){
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
            byteBuffer.clear();
            byteBuffer.put(packet.getContent().getBytes());
            byteBuffer.flip();
            sock.write(byteBuffer);
        }
    };
    //注册建立和服务端的连接
    void registerAndConnect(int port)
            throws IOException {
        InetSocketAddress address = new InetSocketAddress(port);
        SocketChannel sock = createSock();
        sock.connect(address);
        sockKey = sock.register(selector, SelectionKey.OP_CONNECT);
    }
    SocketChannel createSock() throws IOException {
        SocketChannel sock;
        sock = SocketChannel.open();
        sock.configureBlocking(false);
        return sock;
    }
    boolean isConnected() {
        return sockKey != null;
    }
}
