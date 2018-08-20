package org.study.jim.netty.catalina.http;

/**
 * @Auther: jim
 * @Date: 2018/8/19 16:43
 * @Description:
 */
public  abstract  class JimServlet {
    public  void doGet(JimRequest request,JimResponse response){}
    public  void doPost(JimRequest request, JimResponse response){}
}
