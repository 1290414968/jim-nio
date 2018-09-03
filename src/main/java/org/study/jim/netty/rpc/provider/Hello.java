package org.study.jim.netty.rpc.provider;

import org.study.jim.netty.rpc.api.IHello;

/**
 * @Auther: jim
 * @Date: 2018/9/3 08:24
 * @Description:
 */
public class Hello implements IHello {
    @Override
    public String say(String userName) {
        return "welcome to ju world :"+userName;
    }
}
