package com.cwb.im.service;

import org.reactivestreams.Publisher;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

import java.util.function.BiFunction;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/7/21 9:16 下午
 */
@FunctionalInterface
public interface HandlerFirst {

    Publisher<Void> handler(NettyInbound in, NettyOutbound out);
}
