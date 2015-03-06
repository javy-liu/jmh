package org.oyach.jmh.cglib;

/**
 * @author liuzhenyuan
 * @version Last modified 15/3/4
 * @since 0.0.1
 */
public class UserDao {
    private String name;

    public String getName() {
        System.out.println("===getName==");
        return name;
    }
}
