package com.cwb.pushsystem;

import com.cwb.pushsystem.transport.Msg;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/7/24 12:15 上午
 */
public class Main {

    public static void main(String[] args) {
        String text = "init";
        Msg msg = Msg.builder()
                .head(Msg.MsgHead.builder()
                        .size(text.length())
                        .type(Msg.MsgType.TEXT)
                        .createdTime(System.currentTimeMillis())
                        .arriveTime(0L)
                        .sender(0L)
                        .receiver(0L)
                        .build())
                .body(Msg.MsgBody.builder()
                        .body(text)
                        .build())
                .tail(Msg.MsgTail.builder()
                        .placeholder(0)
                        .build())
                .build();
        long curr = System.currentTimeMillis();
        Msg.parseLightweight(msg.asStringLightweight());
        System.out.println(System.currentTimeMillis() - curr);
        curr = System.currentTimeMillis();
        Msg.parse(msg.asString());
        System.out.println(System.currentTimeMillis() - curr);
    }
}
