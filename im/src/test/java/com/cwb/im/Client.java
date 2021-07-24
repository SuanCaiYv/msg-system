package com.cwb.im;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/7/21 12:45 上午
 */
public class Client {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("127.0.0.1", 8190));
        OutputStream outputStream = socket.getOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream);
        printWriter.print("qwer");
        printWriter.flush();
        InputStream inputStream = socket.getInputStream();
        byte[] bytes = new byte[1024];
        int i = inputStream.read(bytes);
        System.out.println(new String(bytes, 0, i));
    }
}
