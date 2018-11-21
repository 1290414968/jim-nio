package org.study.jim.netty.customProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class NettyMessageDecoder extends MessageToMessageDecoder<NettyMessage> {
    protected void decode(ChannelHandlerContext channelHandlerContext, NettyMessage nettyMessage, List<Object> list) throws Exception {

    }
}
