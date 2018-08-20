package org.study.jim.nio.buffer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Auther: jim
 * @Date: 2018/8/19 08:53
 * @Description:
 */
public class DemoBuffer {
    public static void main(String[] args) throws IOException {
        FileInputStream file = new FileInputStream("D:\\test.txt");
        //文件流通道
        FileChannel channel =  file.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(5);
        output("初始化",byteBuffer);

        channel.read(byteBuffer);
        output("read也就是put",byteBuffer);

        byteBuffer.flip();
        output("flip",byteBuffer);

        while (byteBuffer.remaining()>0){
            byteBuffer.get();
        }
        output("get",byteBuffer);

        byteBuffer.clear();
        output("clear",byteBuffer);
        channel.close();
    }
    private static void output(String step,ByteBuffer byteBuffer){
        System.out.println(step+" " + byteBuffer.toString());
    }
}
