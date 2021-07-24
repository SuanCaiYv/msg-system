package com.cwb.pushsystem;

import com.cwb.pushsystem.service.Handler;
import com.cwb.pushsystem.service.impl.HandlerImpl;
import io.netty.channel.ChannelOption;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.netty.DisposableServer;
import reactor.netty.NettyOutbound;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpServer;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/7/23 7:56 下午
 */
public class TestServer {

    static interface FluxListener {
        void onDataChunk(String data);
        void processComplete();
    }

    static interface FluxProcessor {
        void register(FluxListener fluxListener);
        FluxListener getListener();
    }

    public static void main(String[] args) {
        FluxProcessor fluxProcessor = new FluxProcessor() {
            FluxListener fluxListener;

            @Override
            public void register(FluxListener fluxListener) {
                this.fluxListener = fluxListener;
            }

            @Override
            public FluxListener getListener() {
                return this.fluxListener;
            }
        };
        Flux<String> output = Flux.create(sink -> {
            fluxProcessor.register(new FluxListener() {
                @Override
                public void onDataChunk(String data) {
                    sink.next(data);
                }

                @Override
                public void processComplete() {
                    sink.complete();
                }
            });
        });
        int cpuNum = Runtime.getRuntime().availableProcessors();
        LoopResources loopResources = LoopResources.create("event-loop", cpuNum >= 16 ? cpuNum / 8 : 1, cpuNum, true);
        Handler handler = new HandlerImpl();
        AtomicInteger count = new AtomicInteger(0);
        TcpServer server = TcpServer.create()
                .runOn(loopResources)
                .host("127.0.0.1")
                .port(8290)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .handle((in, out) -> {
                    System.out.println(Thread.currentThread().getName());
                    in.receive().asString().publishOn(Schedulers.newSingle("tmp")).subscribe(p -> {
                        System.out.println(Thread.currentThread().getName());
                        System.out.println(p);
                        fluxProcessor.getListener().onDataChunk("456");
                    });
                    NettyOutbound outbound = out.sendString(output);
                    return outbound;
                });
        server.warmup().block();
        DisposableServer bindNow = server.bindNow();
        new Thread(() -> {
            Client.main(args);
        }).start();
        new Thread(() -> {
            Client.main(args);
        }).start();
        LockSupport.parkNanos(Duration.ofMillis(100).toNanos());
        fluxProcessor.getListener().onDataChunk("123");
        bindNow.onDispose().block();
    }
}
