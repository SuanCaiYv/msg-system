package com.cwb.pushsystem;

import com.cwb.pushsystem.transport.Msg;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/7/22 8:13 下午
 */
public class Client {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1", 8290));
            String text = "init";
            Msg msg = Msg.builder()
                    .head(Msg.MsgHead.builder()
                            .size(text.length())
                            .type(Msg.MsgType.INIT)
                            .createdTime(System.currentTimeMillis())
                            .arriveTime(0L)
                            .sender(args.length == 0 ? 1L : Long.parseLong(args[0]))
                            .receiver(2L)
                            .build())
                    .body(Msg.MsgBody.builder()
                            .body(text)
                            .build())
                    .tail(Msg.MsgTail.builder()
                            .placeholder(0)
                            .build())
                    .build();
            new Thread(() -> {
                try {
                    while (true) {
                        if (socket.getInputStream().available() > 0) {
                            byte[] bytes = new byte[socket.getInputStream().available()];
                            socket.getInputStream().read(bytes);
                            String s = new String(bytes, 0, bytes.length);
                            System.out.println(s);
                            System.out.println(Msg.parseLightweight(s).getHead().getReceiver());
                        }
                    }
                } catch (Throwable throwable) {
                    ;
                }
            }).start();
            socket.getOutputStream().write(msg.asStringLightweight().getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();
        } catch (Throwable throwable) {
            ;
        }
    }
}
