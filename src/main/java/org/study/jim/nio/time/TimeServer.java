package org.study.jim.nio.time;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class TimeServer {
    public static void main(String[] args) {
        int port = 8080;
        MultiplexerTimeServer timeServer = new MultiplexerTimeServer(port);
        new Thread(timeServer,"NIO-MultiplexerTimeServer-001").start();
    }
    private static class MultiplexerTimeServer implements Runnable{
        public MultiplexerTimeServer(int port) {
            try {
                //创建选择器
                selector = Selector.open();
                //打开通道
                serverSocketChannel = ServerSocketChannel.open();
                //设置通道为非阻塞
                serverSocketChannel.configureBlocking(false);
                //通道绑定监听端口
                serverSocketChannel.bind(new InetSocketAddress(port),1024);
                //通道注册到选择器上
                serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);
                System.out.println("time server start in port :"+port);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        //选择器
        private Selector selector;
        //服务端socketChannel
        private ServerSocketChannel serverSocketChannel;
        private volatile boolean stop;
        @Override
        public void run() {
            while (!stop){//死循环接收客户端请求
                try {
                    //选择器轮询
                    selector.select(1000);
                    //获取到事件
                    Set<SelectionKey> selectionKeySet =  selector.selectedKeys();
                    Iterator<SelectionKey> it =   selectionKeySet.iterator();
                    SelectionKey key = null;
                    while (it.hasNext()){
                        key = it.next();
                        it.remove();
                        try {
                            //处理连接请求
                            handleInput(key);
                        }catch (Exception e){
                            key.cancel();
                            if(key.channel()!=null)key.channel().close();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(selector!=null){
                try {
                    selector.close();//关闭选择器，相关联的Channel和Pipe会自动关闭
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //读取处理
        private void handleInput(SelectionKey key) throws IOException {
            if(key.isValid()){//合法连接
                if(key.isAcceptable()){
                    ServerSocketChannel scc = (ServerSocketChannel) key.channel();
                    SocketChannel sc =  scc.accept();
                    sc.configureBlocking(false);
                    sc.register(selector,SelectionKey.OP_READ);
                }
                if(key.isReadable()){
                        //读取Channel
                        SocketChannel sc = (SocketChannel) key.channel();
                        //分配缓冲区
                        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                        //将channel中数据读取到缓冲区
                        int readBytes = sc.read(readBuffer);
                        if(readBytes>0){
                            //固定缓冲区
                            readBuffer.flip();
                            //创建byte数组,将缓冲区数据取出放入到数组中，进行解码成字符串
                            byte[] bytes = new byte[readBuffer.remaining()];
                            readBuffer.get(bytes);
                            String body = new String(bytes,"UTF-8");
                            System.out.println("time server receive order : "+body);
                            String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body)
                                    ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
                            doWrite(sc,currentTime);
                        }else if(readBytes<0){
                            key.cancel();
                            sc.close();
                        } else {
                            ;
                        }
                    }
                }
            }
        //输出处理
        private void doWrite(SocketChannel sc, String response) throws IOException {
            if(response!=null && response.trim().length()>0){
                byte[] bytes = response.getBytes();
                ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
                writeBuffer.put(bytes);
                writeBuffer.flip();
                sc.write(writeBuffer);
            }
        }
        //停止标识
        public void stop(){
            this.stop = true;
        }
    }
}
