package org.oyach.jmh.cglib;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserDaoTest {

    @Test
    public void testGetName() throws Exception {
        Enhancer en = new Enhancer();
        en.setSuperclass(UserDao.class);
//        en.setCallbacks(new Callback[]{authProxy,NoOp.INSTANCE});
        en.setCallbackFilter(new UserDaoProxy());

    }

    public void testDao(UserDao userDao){
        System.out.println(userDao.getName());
    }
}