package org.oyach.jmh.server.mysql;

import org.oyach.jmh.server.engine.Engine;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author liuzhenyuan
 * @version Last modified 15/4/21
 * @since 0.0.1
 */
public class MysqlProxyServer implements Runnable {

    public ServerSocket serverSocket = null;

    @Override
    public void run() {
        try {
            this.serverSocket = new ServerSocket(3307);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ExecutorService executorService = Executors.newCachedThreadPool();
        while (true){
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            executorService.submit(new Engine(socket));
        }

    }
}
