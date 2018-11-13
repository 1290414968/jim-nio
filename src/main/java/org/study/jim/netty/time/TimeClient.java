package org.study.jim.netty.time;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.nio.ByteBuffer;

public class TimeClient {
    public static void main(String[] args) throws InterruptedException {
        new TimeClient().connect(8080,"127.0.0.1");
    }
    public void connect(int port,String host) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new TimeClientHandler());
                        }
                    });
           ChannelFuture future =  bootstrap.connect(host,port).sync();
           future.channel().closeFuture().sync();
        }finally {
            group.shutdownGracefully();
        }
    }
    public class TimeClientHandler extends ChannelInboundHandlerAdapter {
        private final ByteBuf firstMessage;
        public TimeClientHandler() {
            byte[] req = "QUERY TIME ORDER".getBytes();
            firstMessage = Unpooled.buffer(req.length);
            firstMessage.writeBytes(req);
        }
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(firstMessage);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf byteBuf = (ByteBuf)msg;
            byte[] req = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(req);
            String body = new String(req,"UTF-8");
            System.out.println("Now is:"+body);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }
}
