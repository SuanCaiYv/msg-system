package com.cwb.pushsystem;

import com.cwb.pushsystem.service.Handler;
import com.cwb.pushsystem.service.impl.HandlerImpl;
import com.cwb.pushsystem.transport.Msg;
import io.netty.channel.ChannelOption;
import reactor.netty.DisposableServer;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpServer;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/7/20 8:47 下午
 * <br/>
 * 推送系统本身不做消息处理，仅负责推送，也就是仅存在第一次连接，客户端发送必要初始化信息初始化连接Channel之后；只能由服务端去推送消息给客户端。
 * <br/>
 * 出了初始连接，客户端只能被动的接收消息。
 */
public class Server {

    public static void main(String[] args) {
        int cpuNum = Runtime.getRuntime().availableProcessors();
        LoopResources loopResources = LoopResources.create("event-loop", cpuNum >= 16 ? cpuNum / 8 : 1, cpuNum, true);
        Handler handler = new HandlerImpl();
        TcpServer server = TcpServer.create()
                .runOn(loopResources)
                .host("127.0.0.1")
                .port(8290)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .handle(handler::handler);
        server.warmup().block();
        DisposableServer bindNow = server.bindNow();
        LockSupport.parkNanos(Duration.ofMillis(100).toNanos());
        new Thread(() -> {
            Client.main(new String[]{"3"});
        }).start();
        new Thread(() -> {
            Client.main(new String[]{"4"});
        }).start();
        LockSupport.parkNanos(Duration.ofMillis(300).toNanos());
        handler.writeAsync(3L, Msg.emptyMsg(3L));
        handler.writeAsync(4L, Msg.emptyMsg(4L));
        handler.writeAsync(4L, Msg.emptyMsg(4L));
        bindNow.onDispose().block();
    }
}
