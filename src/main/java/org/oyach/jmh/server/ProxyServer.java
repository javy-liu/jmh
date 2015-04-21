package org.oyach.jmh.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author liuzhenyuan
 * @version Last modified 15/4/21
 * @since 0.0.1
 */
public class ProxyServer {



    private int port = 3306;

    public Socket clientSocket = null;
    public InputStream clientIn = null;
    public OutputStream clientOut = null;
}
