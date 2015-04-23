package org.oyach.jmh.server.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author liuzhenyuan
 * @version Last modified 15/4/21
 * @since 0.0.1
 */
public class Engine implements Runnable {

    private Socket socket;

    public Engine(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("--------");
        try {
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();


            outputStream.write(new byte[]{74, 0, 0, 0, 10, 53, 46, 54, 46, 50, 51, 0, 10, 0, 0, 0, 40, 55, 123, 41, 87, 91, 69,
                    38, 0, -1, -9, 33, 2, 0, 127, -128, 21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 43, 69, 90, 117, 96, 93, 53, 48,
                    46, 53, 96, 102, 0, 109, 121, 115, 113, 108, 95, 110, 97, 116, 105, 118, 101, 95, 112, 97, 115,
                    115, 119, 111, 114, 100, 0}, 0, 78);
            outputStream.flush();

            outputStream.write(new byte[]{7, 0, 0, 2}, 0, 4);
            outputStream.flush();

            outputStream.write(new byte[]{0, 0, 0, 2, 0, 0, 0}, 0, 7);
            outputStream.flush();

            outputStream.write(new byte[]{1, 0, 0, 1}, 0, 4);
            outputStream.flush();


            //702
//            outputStream.write(ByteArrayUtil.toBytes(78), 0, 4);
            outputStream.flush();
//            outputStream.close();
            while (true){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int bufferSize222 = socket.getReceiveBufferSize();
                System.out.println("bufferSize:"+bufferSize222);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
