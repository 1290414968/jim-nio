package org.study.jim.netty.msgpack;
import com.sun.corba.se.internal.CosNaming.BootstrapServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class EchoServer {
    private final int port;
    public EchoServer(int port) {
        this.port = port;
    }
    public void run() throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast("frameDecoder",new LengthFieldBasedFrameDecoder(65535,0,2,0,2))
                                .addLast("msgpack decoder",new MsgpackDecoder())
                                .addLast("frameEncoder",new LengthFieldPrepender(2))
                                .addLast("msgpack encoder",new MsgpackEncoder())
                                .addLast(new EchoClientHandler());
                    }
                });
        ChannelFuture future =  bootstrap.bind(port).sync();
        future.channel().closeFuture().sync();
    }
    public class EchoClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("server receive message :"+msg);
            ctx.writeAndFlush(msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new EchoServer(6666).run();
    }
}
