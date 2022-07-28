package com.alistair.community.util;

import com.alistair.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，用于代替session对象
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users  = new ThreadLocal<>();

    //根据当前线程作为key，user作为值存放
    public void setUsers(User user) {
        users.set(user);
    }

    public User getUsers() {
        return users.get();
    }

    public void clear() {
        users.remove();
    }
}
