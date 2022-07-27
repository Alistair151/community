package com.alistair.community.util;

public interface CommunityConstant {

    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认登录凭证的超时时间, 12h
     */
    int DEFAULT_EXPIRED_SECOND = 3600 * 12;

    /**
     * 勾选记住我后的超时时间, 100days
     */
    int REMEMBER_EXPIRED_SECOND = 3600 * 24 * 100;

}
