package org.study.jim;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private volatile static int  c = 1;
    public static void main(String[] args) throws IOException {
        Socket s = new Socket("localhost", 1234);

        InputStream inStream = s.getInputStream();
        OutputStream outStream = s.getOutputStream();

        // 输出
        PrintWriter out = new PrintWriter(outStream, true);
        out.println("getPublicKey你好！");
        out.flush();

        s.shutdownOutput();// 输出结束

        // 输入
        Scanner in = new Scanner(inStream);

        StringBuilder sb = new StringBuilder();
        while (in.hasNextLine()) {
            String line = in.nextLine();
            sb.append(line);
        }

        String response = sb.toString();
        System.out.println("response=" + response);
    }

}