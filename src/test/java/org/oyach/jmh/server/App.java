package org.oyach.jmh.server;

import org.oyach.jmh.server.mysql.MysqlProxyServer;

/**
 * @author liuzhenyuan
 * @version Last modified 15/4/21
 * @since 0.0.1
 */
public class App {

    public static void main(String[] args) {
        MysqlProxyServer mysqlProxyServer = new MysqlProxyServer();

        new Thread(mysqlProxyServer).start();
    }
}
