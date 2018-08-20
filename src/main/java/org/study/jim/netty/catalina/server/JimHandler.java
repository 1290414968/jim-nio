package org.study.jim.netty.catalina.server;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.log4j.Logger;
import org.study.jim.netty.catalina.http.JimRequest;
import org.study.jim.netty.catalina.http.JimResponse;
import org.study.jim.netty.catalina.servlets.MyServlet;

/**
 * @Auther: jim
 * @Date: 2018/8/19 16:34
 * @Description:
 */
public class JimHandler extends ChannelInboundHandlerAdapter {
    private Logger LOG = Logger.getLogger(JimHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest)msg;
            JimRequest  jimRequest = new JimRequest(ctx,request);
            JimResponse jimResponse = new JimResponse(ctx,request);
            LOG.info(String.format("Uri:%s ", jimRequest.getUrl()));
            new MyServlet().doGet(jimRequest,jimResponse);
        }
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
