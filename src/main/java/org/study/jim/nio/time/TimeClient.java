package org.study.jim.nio.time;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TimeClient {
    public static void main(String[] args) {
        new Thread(new TimeClientHandler("127.0.0.1",8080),"TimeClient-001").start();
    }
    private static class TimeClientHandler implements Runnable {
        private String host;
        private int port;
        private Selector selector;
        private SocketChannel socketChannel;
        private volatile boolean stop;
        public TimeClientHandler(String host,int port) {
            this.host = host;
            this.port = port;
            try {
                selector = Selector.open();
                socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        @Override
        public void run() {
            //连接请求
            try {
                doConnect();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            while (!stop){
                try {
                    selector.select(1000);
                    Set<SelectionKey> selectionKeySet =  selector.selectedKeys();
                    Iterator<SelectionKey> it =  selectionKeySet.iterator();
                    SelectionKey key = null;
                    while (it.hasNext()){
                        key = it.next();
                        it.remove();
                        handleInput(key);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(selector!=null){
                try {
                    selector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        private void doConnect() throws IOException {
            if(socketChannel.connect(new InetSocketAddress(host,port))){
                socketChannel.register(selector,SelectionKey.OP_READ);
                doWrite(socketChannel);
            }else{
                socketChannel.register(selector,SelectionKey.OP_CONNECT);
            }
        }
        private void handleInput(SelectionKey key) throws IOException {
            if(key.isValid()){
                SocketChannel channel = (SocketChannel)key.channel();
                if(key.isConnectable()){
                    if(channel.finishConnect()){
                        channel.register(selector,SelectionKey.OP_READ);
                        doWrite(channel);
                    }else
                        System.exit(1);
                }
                if(key.isReadable()){
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    int readBytes = channel.read(readBuffer);
                    if(readBytes>0){
                        readBuffer.flip();
                        byte[] bytes = new byte[readBuffer.remaining()];
                        readBuffer.get(bytes);
                        String body = new String(bytes,"UTF-8");
                        System.out.println("Now is : "+body);
                        this.stop = true;
                    }else if(readBytes<0){
                        key.cancel();
                        socketChannel.close();
                    }else {
                        ;
                    }
                }
            }
        }
        private void doWrite(SocketChannel socketChannel) throws IOException {
            byte[] req = "QUERY TIME ORDER".getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
            writeBuffer.put(req);
            writeBuffer.flip();
            socketChannel.write(writeBuffer);
            if(!writeBuffer.hasRemaining()){
                System.out.println("Send order 2 server succeed...");
            }
        }
    }
}
