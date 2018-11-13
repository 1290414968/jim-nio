package org.study.jim.netty.time;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.nio.ByteBuffer;
import java.util.Date;

public class TimeServer {
    public static void main(String[] args) throws InterruptedException {
        new TimeServer().bind(8080);
    }
    public void bind(int port) throws InterruptedException {
        //主从模型
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childHandler(new ChildChannelHandler());
            //监听端口
            ChannelFuture future =  bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        }finally {
            //优雅的关闭
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    public class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            socketChannel.pipeline().addLast(new TimeServerHandler());
        }
    }
    public class TimeServerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf byteBuffer = (ByteBuf)msg;
            byte[] req = new byte[byteBuffer.readableBytes()];
            byteBuffer.readBytes(req);
            String body = new String(req,"UTF-8");
            System.out.println("time server receive order:"+body);
            String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body)
                    ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
            ByteBuf resp =  Unpooled.copiedBuffer(currentTime.getBytes());
            ctx.write(resp);
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
