package org.study.jim.netty.chat.server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.apache.log4j.Logger;
import org.study.jim.netty.chat.server.handler.HttpHandler;
import org.study.jim.netty.chat.server.handler.WebSocketHandler;

/**
 * @Auther: jim
 * @Date: 2018/8/21 21:15
 */
public class ChatServer {
    private static Logger LOG = Logger.getLogger(ChatServer.class);
    public static void main(String[] args) {
        new ChatServer().start(80);
    }
    private void start(int port){
        //boss线程
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel client) throws Exception {
                            ChannelPipeline pipeline =  client.pipeline();
                            //---- Http 协议handler
                            pipeline.addLast(new HttpServerCodec());
                            //主要是将同一个http请求或响应的多个消息对象变成一个 fullHttpRequest完整的消息对象
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            pipeline.addLast(new HttpHandler());

                            //-----WebSocket 协议handler
                            pipeline.addLast(new WebSocketServerProtocolHandler("/im"));
                            pipeline.addLast(new WebSocketHandler());
                        }
                    });
            //绑定端口启动
            ChannelFuture future =  serverBootstrap.bind(port).sync();
            LOG.info("服务端启动成功，端口："+port);
            //监听客户端访问
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
