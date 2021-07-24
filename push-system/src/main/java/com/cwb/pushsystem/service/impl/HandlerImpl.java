package com.cwb.pushsystem.service.impl;

import com.cwb.pushsystem.service.Handler;
import com.cwb.pushsystem.transport.Msg;
import com.cwb.pushsystem.util.EventListener;
import com.cwb.pushsystem.util.EventRegister;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/7/21 9:19 下午
 */
public class HandlerImpl implements Handler {

    // 这里必须使用Concurrent版的，不然会出现并发put导致数据丢失问题
    private final ConcurrentHashMap<Long, EventListener<String>> hashMap = new ConcurrentHashMap<>();

    /**
     * 此方法每个连接只会调用一次
     */
    @Override
    public Publisher<Void> handler(NettyInbound in, NettyOutbound out) {
        // 通过注册回调的方式实现动态添加数据
        EventRegister<String> eventRegister = new EventRegister<>() {
            private EventListener<String> eventListener;

            @Override
            public void register(EventListener<String> listener) {
                this.eventListener = listener;
            }

            @Override
            public EventListener<String> listener() {
                return this.eventListener;
            }
        };
        Flux<String> output = Flux.push(sink -> {
            // 这里的register方法巧妙地侵入了push方法，实现把sink操作暴露到lambda之外的策略。
            eventRegister.register(new EventListener<>() {
                @Override
                public EventListener<String> next(String data) {
                    sink.next(data);
                    return this;
                }

                @Override
                public EventListener<String> error(Throwable throwable) {
                    sink.error(throwable);
                    return this;
                }

                @Override
                public void complete() {
                    sink.complete();
                }
            });
        });
        in.receive().asString()
                // 虽然我们在这里进行了subscribe，它本身是一个阻塞调用，是不推荐的，但是因为是纯内存操作，所以I/O耗时可以忽略不计。
                // 此外，我们不得已必须进行这样的书写，因为无法做到把in作为publisher传递给out，那样的话我们无法实现侵入式主动发送数据，即实现EventListener功能。
                // 如果真的不想阻塞在NioEventLoop线程的话，可以使用publishOn方法切换执行上下文，但是无法使用subscribeOn方法，因为reactor-netty强绑定数据生产到NioEventLoop线程上了。
                .subscribe(input -> {
                    Msg msg = Msg.parseLightweight(input);
                    if (msg != null && msg.getHead().getType().equals(Msg.MsgType.INIT)) {
                        in.withConnection(connection -> {
                            // 初始化连接
                            hashMap.put(msg.getHead().getSender(), eventRegister.listener());
                        });
                    }
                });
        // 这个方法会在每个连接建立之前被调用，然后设置触发链，其中NettyOutbound会被链接到写事件上，此时整个Server还没有开始接受连接。
        // 所以对于output传参sink的操作会早于可读事件的发生，因此不会为空。
        NettyOutbound outbound = out.sendString(output);
        return outbound;
    }

    @Override
    public void writeAsync(long receiverId, Msg msg) {
        EventListener<String> eventListener = hashMap.get(receiverId);
        if (eventListener != null) {
            eventListener.next(msg.asStringLightweight());
        }
    }

    @Override
    public void writeAsync(List<Long> receiverIds, Msg msg, boolean isReceive) {
        if (isReceive) {
            for (long receiverId : receiverIds) {
                writeAsync(receiverId, msg);
            }
        } else {
            Set<Long> excludeSet = new HashSet<>(receiverIds);
            for (Map.Entry<Long, EventListener<String>> entry : hashMap.entrySet()) {
                if (!excludeSet.contains(entry.getKey())) {
                    entry.getValue().next(msg.asStringLightweight());
                }
            }
        }
    }

    @Override
    public void writeAsync(long receiverId, List<Msg> msgs) {
        EventListener<String> eventListener = hashMap.get(receiverId);
        if (eventListener != null) {
            for (Msg msg : msgs) {
                eventListener.next(msg.asStringLightweight());
            }
        }
    }

    @Override
    public void writeAsync(Map<Long, Msg> msgMap) {
        for (Map.Entry<Long, Msg> entry : msgMap.entrySet()) {
            EventListener<String> eventListener = hashMap.get(entry.getKey());
            eventListener.next(entry.getValue().asStringLightweight());
        }
    }

    @Override
    public void writeAsync(Msg msg) {
        for (EventListener<String> eventListener : hashMap.values()) {
            eventListener.next(msg.asStringLightweight());
        }
    }
}
