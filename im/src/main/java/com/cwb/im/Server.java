package com.cwb.im;

import com.cwb.im.service.HandlerFirst;
import com.cwb.im.service.impl.HandlerFirstImpl;
import io.netty.channel.ChannelOption;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpServer;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/7/20 8:47 下午
 */
public class Server {

    public static void main(String[] args) {
        int cpuNum = Runtime.getRuntime().availableProcessors();
        LoopResources loopResources = LoopResources.create("event-loop", cpuNum >= 16 ? cpuNum / 8 : 1, cpuNum, true);
        HandlerFirst handlerFirst = new HandlerFirstImpl();
        TcpServer server = TcpServer.create()
                .runOn(loopResources)
                .host("127.0.0.1")
                .port(8190)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .handle(handlerFirst::handler);
        server.warmup().block();
        server.bindNow().onDispose().block();
    }
}
