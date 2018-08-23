package org.study.jim.netty.chat.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.log4j.Logger;
import org.study.jim.netty.chat.processor.MessageProcessor;

public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static Logger LOG = Logger.getLogger(WebSocketHandler.class);
    private MessageProcessor messageProcessor = new MessageProcessor();
    //用来接收页面发送的消息内容，然后分发给所有的登录用户
    @Override
    protected void channelRead0(ChannelHandlerContext context, TextWebSocketFrame msg) throws Exception {
        messageProcessor.sendMessage(context.channel(),msg.text());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel client = ctx.channel();
        String addr = messageProcessor.getAddress(client);
        LOG.info("WebSocket Client:" + addr + "加入");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel client = ctx.channel();
        String addr = messageProcessor.getAddress(client);
        LOG.info("WebSocket Client:" + addr + "上线");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel client = ctx.channel();
        String addr = messageProcessor.getAddress(client);
        LOG.info("WebSocket Client:" + addr + "掉线");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel client = ctx.channel();
        String addr = messageProcessor.getAddress(client);
        LOG.info("WebSocket Client:" + addr + "异常");
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel client = ctx.channel();
        messageProcessor.logout(client);
        LOG.info("WebSocket Client:" + messageProcessor.getNickName(client) + "登出");
    }
}
