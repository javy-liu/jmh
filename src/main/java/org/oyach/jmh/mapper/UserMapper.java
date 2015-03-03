package org.oyach.jmh.mapper;

/**
 * @author liuzhenyuan
 * @version Last modified 15/3/3
 * @since 0.0.1
 */
public interface UserMapper {

    String findById(long id);

    void insertUser(String user);
}
