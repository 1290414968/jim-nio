package org.study.jim.zookeeper;

import java.io.IOException;

public class TestDemo {
    public static void main(String[] args) throws IOException {
        Zookeeper zookeeper = new Zookeeper(1234);
        zookeeper.create("创建成功....");
    }
}
