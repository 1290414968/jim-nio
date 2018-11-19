package org.study.jim.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpFileServer {
    private static final String DEFAULT_URL = "/src/com/netty";

    public static void main(String[] args) throws InterruptedException {
        new HttpFileServer().run(6666,DEFAULT_URL);
    }
    public void run(final int port,final String url) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast("http-decoder",new HttpRequestDecoder())
                                    .addLast("http-aggregator",new HttpObjectAggregator(65536))
                                    .addLast("http-encoder",new HttpRequestEncoder())
                                    .addLast("http-chunked",new ChunkedWriteHandler())
                                    .addLast("fileServerHandler",new FileServerHandler(url));
                        }
                    });
            ChannelFuture channelFuture =  bootstrap.bind(port).sync();
            System.out.println("http 文件目录服务器启动");
            channelFuture.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
