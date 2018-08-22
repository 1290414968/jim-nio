package org.study.jim.netty.chat.server.handler;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private URL baseURL = HttpHandler.class.getProtectionDomain().getCodeSource().getLocation();
    private final String WebRoot = "WebRoot";
    @Override
    protected void channelRead0(ChannelHandlerContext context, FullHttpRequest request) throws Exception {
        String uri = request.uri();
        String page = uri.equals("/")?"chat.html":uri;
        File file = getFile(page);
        if(file!=null&&file.exists()){
            RandomAccessFile accessFile = new RandomAccessFile(file,"r");
            HttpResponse response = new DefaultHttpResponse(request.protocolVersion(),HttpResponseStatus.OK);
            String contentType = "text/html;";
            if(uri.endsWith(".css")){
                contentType = "text/css;";
            }else if(uri.endsWith(".js")){
                contentType = "text/javascript;";
            }else if(uri.toLowerCase().matches("(jpg|png|gif)$")){
                String ext = uri.substring(uri.lastIndexOf("."));
                contentType = "image/" + ext;
            }
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE,contentType);
            boolean isKeepAlive = HttpHeaders.isKeepAlive(request);
            if(isKeepAlive){
                response.headers().set(HttpHeaders.Names.CONTENT_LENGTH,accessFile.length());
                response.headers().set(HttpHeaders.Names.CONNECTION,HttpHeaders.Values.KEEP_ALIVE);
            }
            context.write(response);
            context.write(new DefaultFileRegion(accessFile.getChannel(),0,accessFile.length()));
            ChannelFuture future =  context.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            if(!isKeepAlive){
                future.addListener(ChannelFutureListener.CLOSE);
            }
            accessFile.close();
        }
    }
    private File getFile(String pageName) throws URISyntaxException {
        String path = baseURL.toURI() + WebRoot + "/" + pageName;
        path = !path.contains("file:") ? path : path.substring(5);
        path = path.replaceAll("//", "/");
        return new File(path);
    }
}
