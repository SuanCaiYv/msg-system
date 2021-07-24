package com.cwb.pushsystem;

import com.cwb.pushsystem.transport.Msg;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/7/22 8:13 下午
 */
public class Client {

    public static void main(String[] args) {
        TcpClient tcpClient = TcpClient.create()
                .host("127.0.0.1")
                .port(8290)
                .doOnConnected(connection -> {
                    connection
                            .channel()
                            .writeAndFlush(connection
                                    .outbound()
                                    .alloc()
                                    .directBuffer()
                                    .writeBytes(Msg.initMsg(Long.parseLong(args[0]))
                                            .asStringLightweight()
                                            .getBytes()
                                    )
                            );
                })
                .handle((in, out) -> {
                    return in.receive().asString().flatMap(p -> {
                        System.out.println(Msg.parseLightweight(p));
                        return Mono.empty();
                    }).then();
                });
        tcpClient.warmup().block();
        Connection connection = tcpClient.connectNow();
        connection.onDispose().block();
    }
}
