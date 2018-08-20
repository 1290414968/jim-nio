package org.study.jim.bio;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
/**
 * @Auther: jim
 * @Date: 2018/8/19 10:17
 * @Description:
 */
public class Client {
    public static void main(String[] args) throws IOException {
        Socket socket =  new Socket("localhost",1234);
        OutputStream outputStream =  socket.getOutputStream();
        outputStream.write("client,hi".getBytes());
        InputStream inputStream =  socket.getInputStream();
        //缓冲区，数组而已
        byte [] buff = new byte[1024];
        int len = inputStream.read(buff);
        //只要一直有数据写入，len就会一直大于0
        if(len > 0){
            String msg = new String(buff,0,len);
            System.out.println("收到响应" + msg);
        }
        outputStream.flush();
        outputStream.close();
        socket.close();
    }
}
