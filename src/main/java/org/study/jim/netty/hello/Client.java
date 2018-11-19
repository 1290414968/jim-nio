package org.study.jim.netty.hello;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Date;

/**
 * @Auther: jim
 * @Date: 2018/11/16 08:28
 * @Description:
 */
public final class Client {
    public static void main(String[] args) throws InterruptedException, IOException {
        //主从Reactor模型
        //接收处理线程组
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            //服务器启动类
            Bootstrap bootstrap = new Bootstrap();
            //设置相关属性
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)//Channel参数配置
                    .handler(new HandlerInitializer());//IO逻辑处理器
            //客户端连接端口，并返回连接通道
            Channel channel =  bootstrap.connect("127.0.0.1",6666).channel();
            ChannelFuture future =  null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            for(;;){
                String line = reader.readLine();
                if(line == null){
                    break;
                }
                future = channel.writeAndFlush(line +"\r\n");
                if("bye".equals(line.toLowerCase())){
                    channel.closeFuture().sync();
                    break;
                }
            }
            //监听服务关闭，阻塞等待
            if(future!=null){
                future.sync();
            }
        }finally {
            group.shutdownGracefully();
        }
    }
    private static class HandlerInitializer extends ChannelInitializer<SocketChannel> {
        private final StringEncoder encoder = new StringEncoder();
        private final StringDecoder decoder = new StringDecoder();
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            //编解码，先解码再编码
            socketChannel.pipeline()
                    .addLast(new DelimiterBasedFrameDecoder(8192,Delimiters.lineDelimiter()))
                    .addLast(decoder).addLast(encoder)
                    .addLast(new ClientHandler());
        }
    }
    private static class ClientHandler extends ChannelInboundHandlerAdapter{
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("connected success!");
        }
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println(msg.toString());
        }
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }
}
