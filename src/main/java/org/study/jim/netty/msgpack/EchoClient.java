package org.study.jim.netty.msgpack;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class EchoClient {
    private final String host;
    private final int port;
    private final int sendNumber;

    public EchoClient(String host, int port, int sendNumber) {
        this.host = host;
        this.port = port;
        this.sendNumber = sendNumber;
    }
    public void run() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY,true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,3000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast("msgpack decoder",new MsgpackDecoder())
                                .addLast("msgpack encoder",new MsgpackEncoder())
                                .addLast(new EchoClientHandler(sendNumber));
                    }
                });
        //客户端请求连接
        ChannelFuture future =  bootstrap.connect(host,port).sync();
        //通道同步阻塞
        future.channel().closeFuture().sync();
    }
    public class EchoClientHandler extends ChannelInboundHandlerAdapter {
        private final int sendNumber;
        public EchoClientHandler(int sendNumber) {
            this.sendNumber = sendNumber;
        }
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            UserInfo[] userInfos = userInfo();
            for(UserInfo userInfo : userInfos){
                ctx.write(userInfo);
            }
            ctx.flush();
        }
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("client receive message :"+msg);
            ctx.write(msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        private UserInfo[] userInfo(){
            UserInfo[] userInfos = new UserInfo[sendNumber];
            UserInfo userInfo = null;
            for(int i=0;i<userInfos.length;i++){
                userInfo = new UserInfo();
                userInfo.setAge(i);
                userInfo.setName("abcd-->"+i);
                userInfos[i] = userInfo;
            }
            return userInfos;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new EchoClient("127.0.0.1",6666,10).run();
    }
}
