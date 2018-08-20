package org.study.jim.netty.catalina.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.apache.log4j.Logger;

/**
 * @Auther: jim
 * @Date: 2018/8/19 16:22
 */
public class NettyTomcat {
    private static Logger LOG = Logger.getLogger(NettyTomcat.class);
    private void start(int port) throws InterruptedException {
        //主
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel client) throws Exception {
                            /**
                             * 备注：这部分逻辑位置很重要否则会出现请求响应党的死循环
                             */
                            //服务端发送的是httpResponse，所以要使用HttpResponseEncoder进行编码
                            client.pipeline().addLast(new HttpResponseEncoder());
                            //服务端接收到的是httpRequest，所以要使用HttpRequestDecoder进行解码
                            client.pipeline().addLast(new HttpRequestDecoder());
                            client.pipeline().addLast(new JimHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true);
                //绑定端口
            ChannelFuture future =  bootstrap.bind(port).sync();
            LOG.info("HTTP服务已启动，监听端口:" + port);
            //接收客户端请求
            future.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
    public static void main(String[] args) {
        try {
            new NettyTomcat().start(7070);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
