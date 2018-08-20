package org.study.jim.netty.catalina.servlets;

import org.study.jim.netty.catalina.http.JimRequest;
import org.study.jim.netty.catalina.http.JimResponse;
import org.study.jim.netty.catalina.http.JimServlet;

/**
 * @Auther: jim
 * @Date: 2018/8/19 16:44
 * @Description:
 */
public class MyServlet extends JimServlet {
    @Override
    public void doGet(JimRequest request, JimResponse response) {
        doPost(request,response);
    }
    @Override
    public void doPost(JimRequest request, JimResponse response) {
        response.write(request.getParam("name"));
    }
}
