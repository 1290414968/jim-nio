package org.study.jim.netty.http;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
public class FileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    //文件逻辑处理类
    public FileServerHandler(String url) {
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest request) throws Exception {

    }
}
