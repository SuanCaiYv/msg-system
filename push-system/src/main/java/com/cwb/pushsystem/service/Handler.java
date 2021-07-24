package com.cwb.pushsystem.service;

import com.cwb.pushsystem.transport.Msg;
import org.reactivestreams.Publisher;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

import java.util.List;
import java.util.Map;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/7/21 9:16 下午
 */
public interface Handler {

    /**
     * 通用处理
     */
    Publisher<Void> handler(NettyInbound in, NettyOutbound out);

    /**
     * 一个用户一条(异步版)
     */
    void writeAsync(long receiverId, Msg msg);

    /**
     * 多个用户同一条(异步版)
     */
    void writeAsync(List<Long> ids, Msg msg, boolean isReceive);

    /**
     * 一个用户多条(异步版)
     */
    void writeAsync(long receiverId, List<Msg> msgs);

    /**
     * 多个用户多条(异步版)
     */
    void writeAsync(Map<Long, Msg> msgMap);

    /**
     * 全部用户同一条(异步版)
     */
    void writeAsync(Msg msg);
}
