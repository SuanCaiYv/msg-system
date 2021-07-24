package com.cwb.pushsystem.util;

import reactor.core.Disposable;

import java.util.function.LongConsumer;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/7/24 12:33 上午
 */
public interface EventListener<T> {
    EventListener<T> next(T data);

    EventListener<T> error(Throwable throwable);

    void complete();

    default EventListener<T> onRequest(LongConsumer l) {
        l.accept(1L);
        return this;
    }

    default EventListener<T> onCancel(Disposable disposable) {
        return this;
    }

    default EventListener<T> onDispose(Disposable disposable) {
        return this;
    }
}
