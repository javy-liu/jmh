package org.oyach.jmh.ser;

import org.junit.Test;

/**
 * @author liuzhenyuan
 * @version Last modified 15/4/13
 * @since 0.0.1
 */
public class ThriftUtilTest {

    @Test
    public void testObj2byte() throws Exception {

    }

//    @Test
//    public void testByte2obj() throws Exception {
//        org.oyach.jmh.domain.User user = new org.oyach.jmh.domain.User();
//        user.setId(3L);
//        user.setUsername("oyach");
//        user.setNickname("欧阳澄泓");
//
//        byte[] bytes = ThriftUtil.obj2byte(user);
//
//        org.oyach.jmh.domain.User newUser = ThriftUtil.byte2obj(bytes, org.oyach.jmh.domain.User.class);
//
//        User user2 = new User();
//        user2.setId(3);
//        user2.setUsername("oyach");
//        user2.setNickename("欧阳澄泓");
//        user2.setLocked(false);
//
//        String json = JsonUtil.obj2json(user);
//        String json2 = JsonUtil.obj2json(user2);
//
//        byte[] bytes1 = json.getBytes();
//        byte[] bytes2 = json2.getBytes();
//        System.out.println();
//    }
//
//
    @Test
    public void testName() throws Exception {
        org.oyach.jmh.domain.User user = new org.oyach.jmh.domain.User();
        user.setId(3L);
        user.setUsername("oyach");
        user.setNickname("欧阳澄泓");

        byte[] bytes = ThriftUtil.obj2byte(user);


        org.oyach.jmh.domain.User2 newUser = ThriftUtil.byte2obj(bytes, org.oyach.jmh.domain.User2.class);

        System.out.println();

    }
}