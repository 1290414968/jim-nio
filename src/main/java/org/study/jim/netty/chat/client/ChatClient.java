package org.study.jim.netty.chat.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.study.jim.netty.chat.client.handler.ChatClientHandler;
import org.study.jim.netty.chat.protocol.IMDecoder;
import org.study.jim.netty.chat.protocol.IMEncoder;

public class ChatClient {
    public static void main(String[] args) {
        new ChatClient("IDEA").connection("127.0.0.1",80);
    }
    private ChatClientHandler clientHandler;
    public  ChatClient(String nickName){
        this.clientHandler = new ChatClientHandler(nickName);
    }
    private void connection(String address,int port){
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workGroup).channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new IMDecoder());
                            socketChannel.pipeline().addLast(new IMEncoder());
                            socketChannel.pipeline().addLast(clientHandler);
                        }
                    });
            ChannelFuture future =  bootstrap.connect(address,port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workGroup.shutdownGracefully();
        }
    }
}
