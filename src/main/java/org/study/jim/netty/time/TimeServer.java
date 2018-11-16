package org.study.jim.netty.time;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

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
            System.out.println("timeServer start success!");
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
            //解码器的添加
            socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024));
            socketChannel.pipeline().addLast(new StringDecoder());
            socketChannel.pipeline().addLast(new TimeServerHandler());
        }
    }
    public class TimeServerHandler extends ChannelInboundHandlerAdapter {
        private int counter;
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//            ByteBuf byteBuffer = (ByteBuf)msg;
//            byte[] req = new byte[byteBuffer.readableBytes()];
//            byteBuffer.readBytes(req);
//            //在高并发情况下同样存在粘包问题
////            String body = new String(req,"UTF-8");
//            //模拟粘包问题出现的故障场景
//            String errorBody = new String(req,"UTF-8").substring(0,req.length -
//            System.getProperty("line.separator").length());
            String body = (String)msg;
            System.out.println("time server receive order:"+body
            +" ; the counter is : "+ ++counter);
            String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body)
                    ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
            currentTime = currentTime+System.getProperty("line.separator");
            ByteBuf resp =  Unpooled.copiedBuffer(currentTime.getBytes());
            ctx.writeAndFlush(resp);
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
