package org.oyach.jmh.cglib;

import net.sf.cglib.proxy.CallbackFilter;

import java.lang.reflect.Method;

/**
 * @author liuzhenyuan
 * @version Last modified 15/3/4
 * @since 0.0.1
 */
public class UserDaoProxy implements CallbackFilter {

    @Override
    public int accept(Method method) {
        System.out.println(method.getName());
        return 1;
    }
}
