package org.study.jim.imitate;

import org.study.jim.SelectSockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client implements Runnable {
    private Selector selector;
    private ByteBuffer byteBuffer;
    public Client() {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        for(int i=0;i<5;i++){
            new Thread(new Client(),"Thread"+i).start();
        }
    }

    private synchronized void sendRequest(int port) throws IOException {
        byteBuffer = ByteBuffer.allocateDirect(1024);
        //1、先发送请求，将连接通道注册到选择器上
        SocketChannel requestChannel = SocketChannel.open();
        requestChannel.configureBlocking(false);
        requestChannel.connect(new InetSocketAddress(port));
        requestChannel.register(selector,SelectionKey.OP_CONNECT);
        //2、请求和服务器响应的回应
        selector.select();
        Iterator<SelectionKey> it =  selector.selectedKeys().iterator();
        while (it.hasNext()){
            it.remove();
            SelectionKey key = it.next();
            if(key.isConnectable()){
                if(requestChannel.isConnectionPending()&&requestChannel.finishConnect()){
                    key.interestOps(SelectionKey.OP_READ);
                    byteBuffer.clear();
                    byteBuffer.put("send request".getBytes());
                    requestChannel.write(byteBuffer);
                }
            }else if(key.isReadable()){
                byteBuffer.clear();
                requestChannel.read(byteBuffer);
                System.out.println(byteBuffer.toString());
                byteBuffer.flip();
                byteBuffer.put("server response reply".getBytes());
                requestChannel.write(byteBuffer);
            }
        }
    }

    @Override
    public void run() {
        try {
            sendRequest(1234);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
