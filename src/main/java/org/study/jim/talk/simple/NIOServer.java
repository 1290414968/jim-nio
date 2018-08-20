package org.study.jim.talk.simple;
import com.sun.org.apache.xerces.internal.dom.PSVIAttrNSImpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * @Auther: jim
 * @Date: 2018/8/18 15:03
 *简单的通信聊天demo：
 *1、
 *2、
 *3、
 *4、
 */
public class NIOServer {
    private InetSocketAddress address;
    private Charset charset = Charset.forName("UTF-8");

    private Selector selector;

    public NIOServer(int port) {
        address = new InetSocketAddress(port);
        try {
            // 打开通道，先修高速路
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(address);
            serverSocketChannel.configureBlocking(false);
            //选择器的打开，叫号大厅的打开
            selector = Selector.open();
            //通道注册，通知操作类型
            serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);
            System.out.println("服务器监听准备完成，启动端口："+port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void listen() throws IOException {
        while (true){
            selector.select();
            Set<SelectionKey> selectionKeySet=  selector.selectedKeys();
            Iterator<SelectionKey> it =  selectionKeySet.iterator();
            while (it.hasNext()){
                 SelectionKey key =  it.next();
                 it.remove();
                 process(key);
            }
        }
    }
    private void process(SelectionKey key) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        if(key.isAcceptable()){
           ServerSocketChannel serverSocketChannel =   (ServerSocketChannel) key.channel();
           SocketChannel channel =  serverSocketChannel.accept();
           channel.configureBlocking(false);
           channel.register(selector,SelectionKey.OP_READ);
        }else if(key.isReadable()){
            SocketChannel channel  =  (SocketChannel) key.channel();
            int count = channel.read(byteBuffer);
            if(count>0){
                String content = new String(byteBuffer.array(), 0,count);
                System.out.println("request:"+content);
                byteBuffer.flip();
                channel.configureBlocking(false);
                channel.register(selector,SelectionKey.OP_WRITE);
            }
        }else if(key.isWritable()){
            byteBuffer.clear();
            SocketChannel channel  =  (SocketChannel) key.channel();
            channel.write(ByteBuffer.wrap("write hello".getBytes()));
            channel.close();
        }
    }
    public static void main(String[] args) throws IOException {
        new NIOServer(1234).listen();
    }

}
