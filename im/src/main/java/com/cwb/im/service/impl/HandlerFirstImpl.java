package com.cwb.im.service.impl;

import com.cwb.im.service.HandlerFirst;
import org.reactivestreams.Publisher;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/7/21 9:19 下午
 */
public class HandlerFirstImpl implements HandlerFirst {
    @Override
    public Publisher<Void> handler(NettyInbound in, NettyOutbound out) {
        return null;
    }
}
