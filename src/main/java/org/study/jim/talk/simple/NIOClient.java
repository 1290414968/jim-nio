package org.study.jim.talk.simple;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @Auther: jim
 * @Date: 2018/8/18 15:03
 */
public class NIOClient {
    private Selector selector;
    private InetSocketAddress address;
    private volatile boolean out = true;
    private ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    public NIOClient(int port) throws Exception {
        this.address = new InetSocketAddress("localhost",port);
        this.selector = Selector.open();
        SocketChannel channel = SocketChannel.open(address);
        channel.configureBlocking(false);
        //客户端请求写出内容
        channel.register(selector,SelectionKey.OP_WRITE);
    }
    private void session() throws IOException {
        while (out){
            int channels  = selector.select();
            if (channels==0) continue;
            Set<SelectionKey> set =  selector.selectedKeys();
            Iterator<SelectionKey> it =  set.iterator();
            while (it.hasNext()){
                SelectionKey key =  it.next();
                it.remove();
                if(key.isWritable()){
                    SocketChannel socketChannel = (SocketChannel)key.channel();
                    socketChannel.write(ByteBuffer.wrap("hello,sever".getBytes()));
                    socketChannel.register(selector,SelectionKey.OP_READ);
                }else if(key.isReadable()){
                    //接收服务端响应内容，当得到响应之后退出监听死循环
                    SocketChannel socketChannel = (SocketChannel)key.channel();
                    int len = socketChannel.read(byteBuffer);
                    if(len>0){
                        byteBuffer.flip();
                        System.out.println(new String(byteBuffer.array(),0,len));
                        out = false;
                    }
                }
            }
        }
    }
    public static void main(String[] args) throws Exception {
        new NIOClient(1234).session();
    }
}
