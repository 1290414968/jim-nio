package org.study.jim.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @Auther: jim
 * @Date: 2018/8/19 10:32
 * @Description:
 */
public class AioServerDemo {
    private int port;
    public AioServerDemo(int port){
        this.port = port;
    }
    private void listen() throws Exception {
        //异步通道
        AsynchronousServerSocketChannel channel = AsynchronousServerSocketChannel.open();
        //绑定端口
        channel.bind(new InetSocketAddress(port));
        //异步客户端请求
        Future<AsynchronousSocketChannel> future =   channel.accept();
        AsynchronousSocketChannel clientChannel =  future.get();
        //缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocate(2014);
        //请求读入缓存区
        Future<Integer> futureInteger  = clientChannel.read(byteBuffer);
        int len =  futureInteger.get();
        System.out.println("request:"+new String(byteBuffer.array(),0,len));
        byteBuffer.flip();
        //写入缓存区，响应请求
        clientChannel.write(ByteBuffer.wrap("hhhh".getBytes()));
        clientChannel.close();
        Thread.sleep(Integer.MAX_VALUE);
    }

    public static void main(String[] args) throws Exception {
        new AioServerDemo(1234).listen();
    }
}
