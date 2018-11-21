package org.study.jim.netty.customProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.jboss.marshalling.Marshaller;
import java.util.List;
/*
编码类
* */
public final class NettyMessageEncoder extends MessageToMessageEncoder<NettyMessage> {
    MarshallingEncoder marshallingEncoder;

    public NettyMessageEncoder() {
        this.marshallingEncoder = new MarshallingEncoder();
    }

    protected void encode(ChannelHandlerContext channelHandlerContext, NettyMessage nettyMessage, List<Object> list) throws Exception {

    }
    public class MarshallingEncoder{
        private final byte[] LENGTH_PLACEHOLDER = new byte[4];
        Marshaller marshaller;
        public MarshallingEncoder() {
            marshaller = null;
        }
    }
}
