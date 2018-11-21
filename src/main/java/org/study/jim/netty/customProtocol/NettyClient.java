package org.study.jim.netty.customProtocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
//客户端
public class NettyClient {
    public static void main(String[] args) throws InterruptedException {
        new NettyClient().connect("127.0.0.1",6666);
    }
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    EventLoopGroup group = new NioEventLoopGroup();
    public void connect(String address,int port) throws InterruptedException {
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //添加一系列的逻辑处理Handler
                            //1、编解码
                            //2、心跳机制
                            //3、安全验证
                            //4、超时机制
                            socketChannel.pipeline()
                                    .addLast(new NettyMessageDecoder())
                                ;
                        }
                    });
            ChannelFuture channelFuture =  bootstrap.connect(address,port).sync();
            channelFuture.channel().closeFuture().sync();
        }finally {
            //客户端的重连
            executor.execute(new Runnable() {
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                        connect("127.0.0.1",6666);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
