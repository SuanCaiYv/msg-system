package com.cwb.pushsystem.util;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/7/24 12:32 上午
 */
public interface EventRegister<T> {
    void register(EventListener<T> listener);

    EventListener<T> listener();
}
