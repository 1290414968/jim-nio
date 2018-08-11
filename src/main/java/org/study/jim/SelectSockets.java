package org.study.jim;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;

public class SelectSockets {
    // 本地字符集
    private static final String LocalCharSetName = "UTF-8";
    public static int port_num = 1234;
    public static void main(String[] args) throws IOException {
        new SelectSockets().go();
    }
    public void go() throws IOException {
        System.out.println("Listening on port"+port_num);
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();//打开一个ServerSocket通道
        serverSocketChannel.configureBlocking(false);//通道非阻塞
        ServerSocket serverSocket = serverSocketChannel.socket();//获取通道中的Socket
        serverSocket.bind(new InetSocketAddress(port_num));//socket绑定端口
        Selector selector =  Selector.open();//创建一个选择器
        serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);//将通道注册绑定到选择器上
        while (true){
            int n = selector.select();//获取选择器上已就绪的通道
            if(n==0){
                continue;
            }
            Iterator<SelectionKey> it =   selector.selectedKeys().iterator();//获取选择器上的就绪通道与之通信
            while (it.hasNext()){
                SelectionKey key = it.next();
                if(key.isAcceptable()){//连接是否可接收，客户端连接过来，那么选择一个channel
                    SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
                    clientChannel.configureBlocking(false);
                    clientChannel.register(selector, SelectionKey.OP_READ,ByteBuffer.allocate(1024));
                    //模拟处理连接-sayHello->服务器端向客户端响应
                    sayHello(clientChannel);
                }
                if(key.isReadable()){//连接是否可读，客户端进行写入时，服务端读取客户端内容存储到ByteBuffer
                    //读取方法
                    readData(key);
                }
                it.remove();
            }
        }
    }
    //定义缓冲区
    private ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
    //响应通道输出缓冲区内容
    private void sayHello(SocketChannel sc) throws IOException {
        byteBuffer.clear();
        byteBuffer.put("hello------->".getBytes());
        //写模式转换成读模式
        byteBuffer.flip();
        sc.write(byteBuffer);
    }
    //读取
    private void readData(SelectionKey key) throws IOException {
        SocketChannel channel =  (SocketChannel)key.channel();
        int count;
        byteBuffer.clear();
        while ((count=channel.read(byteBuffer))>0){
            //写模式转换成读模式
            byteBuffer.flip();
            while (byteBuffer.hasRemaining()){
                channel.write(byteBuffer);
            }
            byteBuffer.clear();
        }
        // 将获得字节字符串(使用Charset进行解码)
        String receivedString = Charset
                .forName(LocalCharSetName).newDecoder().decode(byteBuffer).toString();
        // 控制台打印出来
        System.out.println("接收到信息:" + receivedString);
        //读取结束后关闭通道，使key失效
        if(count<0){
            channel.close();
        }
    }
}
