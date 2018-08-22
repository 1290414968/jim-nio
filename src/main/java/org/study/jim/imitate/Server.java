package org.study.jim.imitate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private Selector selector;
    private ByteBuffer byteBuffer;

    public Server() {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            new Server().init(1234);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void init(int port) throws IOException {
        byteBuffer = ByteBuffer.allocate(1024);
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);

        while (true){
            selector.select();
            Iterator<SelectionKey> it =  selector.selectedKeys().iterator();
            while (it.hasNext()){
                SelectionKey key = it.next();
                it.remove();
                SocketChannel socketChannel = null;
                if(key.isAcceptable()){
                    ServerSocketChannel channel =  (ServerSocketChannel) key.channel();
                    socketChannel = channel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector,SelectionKey.OP_READ);
                }else if(key.isReadable()) {
                    byteBuffer.clear();
                    socketChannel = (SocketChannel) key.channel();
                    int count = socketChannel.read(byteBuffer);
                    if(count>0){
                        System.out.println(Charset.forName("UTF-8").newDecoder().decode(byteBuffer).toString());
                        byteBuffer.flip();
                        byteBuffer.put("server to request reply".getBytes());
                        socketChannel.write(byteBuffer);
                    }else{
                        socketChannel.close();
                    }
                }
            }
        }

    }
}
