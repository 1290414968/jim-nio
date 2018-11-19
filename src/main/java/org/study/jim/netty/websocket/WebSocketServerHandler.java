package org.study.jim.netty.websocket;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
    private WebSocketServerHandshaker handshaker;
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        if(o instanceof FullHttpRequest){
            //http 请求的业务逻辑处理
        }else if(o instanceof WebSocketFrame){
            //webSocket 请求的业务逻辑处理
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

}
