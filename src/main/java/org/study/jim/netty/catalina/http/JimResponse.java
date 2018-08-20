package org.study.jim.netty.catalina.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @Auther: jim
 * @Date: 2018/8/19 16:42
 * @Description:
 */
public class JimResponse {
    private ChannelHandlerContext ctx;
    private HttpRequest request;
    public JimResponse(ChannelHandlerContext ctx, HttpRequest request) {
        this.ctx = ctx;
        this.request = request;
    }
    public void write(String outString){
        try{
            if(outString==null) return;
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(outString.getBytes("UTF-8")));
            response.headers().set(CONTENT_TYPE, "text/json");
            response.headers().set(CONTENT_LENGTH,response.content().readableBytes());
            response.headers().set(EXPIRES, 0);

            ctx.write(response);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            ctx.flush();
        }
    }
}
