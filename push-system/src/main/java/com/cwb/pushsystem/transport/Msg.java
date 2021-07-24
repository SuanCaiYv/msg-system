package com.cwb.pushsystem.transport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/7/21 11:10 下午
 * <br/>
 * 为了性能，我们的推送实现不采用ACK。推送的消息本身属于一次性的，且具有不可复现性，所以只能保证在线的设备可以接收推送。
 */
@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Msg {

    private static final Logger logger = LoggerFactory.getLogger(Msg.class);

    @Data
    @With
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MsgHead {

        /**
         * 消息ID，和接收者有绑定关系
         */
        private String id;

        /**
         * 消息大小
         */
        private int size;

        /**
         * 消息类型
         */
        private MsgType type;

        /**
         * 客户端创建的时间
         */
        private long createdTime;

        /**
         * 到达服务器的时间
         */
        private long arriveTime;

        /**
         * 发送者
         */
        private long sender;

        /**
         * 接收者
         */
        private long receiver;

        public String asStringLightweight(String splitStr) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder
                    .append(id)
                    .append(splitStr)
                    .append(size)
                    .append(splitStr)
                    .append(type.getDesc())
                    .append(splitStr)
                    .append(createdTime)
                    .append(splitStr)
                    .append(arriveTime)
                    .append(splitStr)
                    .append(sender)
                    .append(splitStr)
                    .append(receiver)
                    .append(splitStr);
            return stringBuilder.toString();
        }

        public static MsgHead parseLightweight(String input) {
            String splitStr = input.substring(input.length() - 20);
            String[] str = input.split(splitStr);
            MsgHead head = MsgHead.builder()
                    .id(str[0])
                    .size(Integer.parseInt(str[1]))
                    .type(MsgType.create(str[2]))
                    .createdTime(Long.parseLong(str[3]))
                    .arriveTime(Long.parseLong(str[4]))
                    .sender(Long.parseLong(str[5]))
                    .receiver(Long.parseLong(str[6]))
                    .build();
            return head;
        }
    }

    @Data
    @With
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MsgBody {

        /**
         * 消息体
         */
        private String body;

        public String asStringLightweight(String splitStr) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder
                    .append(body)
                    .append(splitStr);
            return stringBuilder.toString();
        }

        public static MsgBody parseLightweight(String input) {
            String splitStr = input.substring(input.length() - 20);
            String[] str = input.split(splitStr);
            MsgBody body = MsgBody.builder()
                    .body(str[0])
                    .build();
            return body;
        }
    }

    @Data
    @With
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MsgTail {

        // TODO 添加一些安全措施，比如加密
        private int placeholder;

        public String asStringLightweight(String splitStr) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder
                    .append(placeholder)
                    .append(splitStr);
            return stringBuilder.toString();
        }

        public static MsgTail parseLightweight(String input) {
            String splitStr = input.substring(input.length() - 20);
            String[] str = input.split(splitStr);
            MsgTail tail = MsgTail.builder()
                    .placeholder(Integer.parseInt(str[0]))
                    .build();
            return tail;
        }
    }

    private MsgHead head;

    private MsgBody body;

    private MsgTail tail;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static enum MsgType {
        TEXT("text"),

        IMG("img"),

        VIDEO("video"),

        FILE("file"),

        INIT("init"),

        DEFAULT("default");

        private final String desc;

        MsgType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return this.desc;
        }

        public static MsgType create(String desc) {
            return switch (desc) {
                case "text" -> TEXT;
                case "img" -> IMG;
                case "video" -> VIDEO;
                case "file" -> FILE;
                case "init" -> INIT;
                default -> DEFAULT;
            };
        }

        @Override
        public String toString() {
            return getDesc().toUpperCase();
        }
    }

    public static Msg parse(ByteBuf byteBuf) {
        String text = byteBuf.toString(StandardCharsets.UTF_8);
        return parse(text);
    }

    public static Msg parse(String input) {
        Msg msg = null;
        try {
            msg = objectMapper.readValue(input, Msg.class);
        } catch (JsonProcessingException ignored) {
            logger.error("捕获错误: {}", ignored.toString());
        }
        return msg;
    }

    /**
     * 这是为了追求极致的解析性能而设计的，经测试，比直接使用JSON快了十倍以上。
     * 但是缺点很明显，就是无法处理解析错误，所以要求前后端必须确保数据格式正确性。
     */
    public static Msg parseLightweight(String input) {
        try {
            String splitStr = input.substring(input.length() - 20);
            String[] str = input.split(splitStr);
            MsgHead head = MsgHead.parseLightweight(str[0]);
            MsgBody body = MsgBody.parseLightweight(str[1]);
            MsgTail tail = MsgTail.parseLightweight(str[2]);
            Msg msg = Msg.builder()
                    .head(head)
                    .body(body)
                    .tail(tail)
                    .build();
            return msg;
        } catch (Throwable throwable) {
            return null;
        }
    }

    public byte[] asByteArray() {
        try {
            return objectMapper.writeValueAsBytes(this);
        } catch (JsonProcessingException ignored) {
            return null;
        }
    }

    public String asString() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException ignored) {
            return null;
        }
    }

    public String asStringLightweight() {
        long curr = System.nanoTime();
        String splitStr1 = String.format("%020d", curr);
        String splitStr2 = String.format("%020d", curr + 1);
        String head = this.head.asStringLightweight(splitStr2);
        String body = this.body.asStringLightweight(splitStr2);
        String tail = this.tail.asStringLightweight(splitStr2);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(head)
                .append(splitStr1)
                .append(body)
                .append(splitStr1)
                .append(tail)
                .append(splitStr1);
        return stringBuilder.toString();
    }

    public static Msg emptyMsg() {
        return Msg.builder()
                .head(MsgHead.builder()
                        .id("no-id")
                        .size(0)
                        .type(MsgType.DEFAULT)
                        .createdTime(System.currentTimeMillis())
                        .arriveTime(System.currentTimeMillis())
                        .sender(-1L)
                        .receiver(-1L)
                        .build())
                .body(MsgBody.builder()
                        .body("empty")
                        .build())
                .tail(MsgTail.builder()
                        .placeholder(0)
                        .build())
                .build();
    }

    public static Msg initMsg(long sender) {
        Msg msg = emptyMsg();
        msg.getHead().setId(sender + "-0");
        msg.getHead().setType(MsgType.INIT);
        msg.getHead().setSender(sender);
        msg.getBody().setBody("init");
        return msg;
    }
}
