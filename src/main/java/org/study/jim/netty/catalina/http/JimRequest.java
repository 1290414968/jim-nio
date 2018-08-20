package org.study.jim.netty.catalina.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Auther: jim
 * @Date: 2018/8/19 16:42
 * @Description:
 */
public class JimRequest {
    private ChannelHandlerContext ctx;
    private HttpRequest request;
    public JimRequest(ChannelHandlerContext ctx, HttpRequest request) {
        this.ctx = ctx;
        this.request = request;
    }
    public String getUrl(){
        return request.getUri();
    }
    public Map<String, List<String>> getParam(){
        QueryStringDecoder decoder = new QueryStringDecoder(getUrl());
        return decoder.parameters();
    }
    public String getParam(String name){
        Map<String, List<String>> params = getParam();
        List<String> param = params.get(name);
        if(null != param){
            return param.get(0);
        }else{
            return null;
        }
    }
}
