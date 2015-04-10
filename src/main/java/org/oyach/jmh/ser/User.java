package org.oyach.jmh.ser;

import java.io.Serializable;

/**
 * @author liuzhenyuan
 * @version Last modified 15/4/9
 * @since 0.0.1
 */
public class User implements Serializable{
    private long id;
    private String username;
    private String nickename;
    private boolean locked;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickename() {
        return nickename;
    }

    public void setNickename(String nickename) {
        this.nickename = nickename;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
