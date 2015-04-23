package org.oyach.jmh.ser;

import org.junit.Test;

/**
 * @author liuzhenyuan
 * @version Last modified 15/4/9
 * @since 0.0.1
 */
public class KryoUtilTest {

    @Test
    public void testObject2byte() throws Exception {
        User user = new User();
        user.setId(3);
        user.setUsername("oyach");
        user.setNickename("欧阳澄泓");
        user.setLocked(false);
//
//        byte[] bytes = KryoUtil.object2byte(user);
//        System.out.println();
    }

    @Test
    public void testByte2object() throws Exception {
        User user = new User();
        user.setId(3);
        user.setUsername("oyach");
        user.setNickename("欧阳澄泓");
        user.setLocked(false);

//        byte[] bytes = KryoUtil.object2byte(user);
//
//        User newUser = KryoUtil.byte2object(bytes, User.class);
//
//        System.out.println(newUser);
    }


    @Test
    public void testName() throws Exception {
        User user = new User();
        user.setId(3);
        user.setUsername("oyach");
        user.setNickename("欧阳澄泓");
        user.setLocked(false);

        String json = JsonUtil.obj2json2(user);

        User u = JsonUtil.json2Obj2(json, User.class);
        System.out.println();
    }
}