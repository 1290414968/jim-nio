package org.study.jim.netty.hello;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.net.InetAddress;
import java.util.Date;
/**
 * @Auther: jim
 * @Date: 2018/11/16 08:28
 * @Description:
 */
public final class Server {
    public static void main(String[] args) throws InterruptedException {
        //主从Reactor模型
        //接收处理线程组
        //服务端和客户端传递数据协议的一致性
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        //工作处理线程组
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try{
            //服务器启动类
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //设置相关属性
            serverBootstrap.group(bossGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,1024)//Channel参数配置
                    .handler(new LoggingHandler(LogLevel.INFO))//连接处理器
                    .childHandler(new ChildHandlerInitializer());//IO逻辑处理器
            //服务器绑定端口，并同步连接次数
            ChannelFuture future =  serverBootstrap.bind(6666).sync();
            //监听服务关闭，阻塞等待
            future.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
    private static class ChildHandlerInitializer extends ChannelInitializer<SocketChannel> {
        private final StringEncoder encoder = new StringEncoder();
        private final StringDecoder decoder = new StringDecoder();
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            //编解码，先解码再编码
            socketChannel.pipeline()
                    .addLast(new DelimiterBasedFrameDecoder(8192,Delimiters.lineDelimiter()))
                    .addLast(decoder).addLast(encoder)
                    .addLast(new ServerHandler());
        }
    }
    private static class ServerHandler extends ChannelInboundHandlerAdapter{
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("new connect receive!");
            ctx.write("Welcome to "+InetAddress.getLocalHost().getHostName()+"!\r\n");
            ctx.write("It is :"+new Date()+"!\r\n");
            ctx.flush();
        }
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            String request = (String)msg;
            System.out.println("receive msg:"+msg);
            String response = "";
            boolean close = false;
            if(request.isEmpty()){
                response = "please type something!"+"\r\n";
            }else if(request.equals("bye")){
                response = "bye ,have a good day!"+"\r\n";
                close = true;
            }else {
                response = "Did you say :"+request+"?"+"\r\n";
            }
            ChannelFuture future = ctx.write(response);
            if(close){
                future.addListener(ChannelFutureListener.CLOSE);
            }
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
