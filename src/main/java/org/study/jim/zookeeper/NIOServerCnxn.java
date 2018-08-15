package org.study.jim.zookeeper;

import org.study.jim.demo.CharsetHelper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class NIOServerCnxn {
    private Selector selector;
    private ByteBuffer byteBuffer;
    public static void main(String[] args) {
        NIOServerCnxn cnxn = new NIOServerCnxn();
        cnxn.init(1234);
        cnxn.listen();
    }
    private void init(int port){
        try {
            byteBuffer = ByteBuffer.allocate(1024);
            selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            //绑定端口
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void listen(){
        while (true){
            try {
                selector.select();
                Iterator ite = selector.selectedKeys().iterator();

                while(ite.hasNext()){
                    SelectionKey key = (SelectionKey) ite.next();
                    ite.remove();//确保不重复处理

                    handleKey(key);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void handleKey(SelectionKey key) throws IOException {
        SocketChannel channel =  null;
        if(key.isAcceptable()){
            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            channel = serverChannel.accept();//接受连接请求
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
        }else if(key.isReadable()){
            channel = (SocketChannel) key.channel();
            byteBuffer.clear();
            int count =  channel.read(byteBuffer);
            if(count>0){
                byteBuffer.flip();
                channel.write(Charset.forName("UTF-8").newEncoder().encode(CharBuffer.wrap("zookeeper响应......")));
            }
        }

    }
}
