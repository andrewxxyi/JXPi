package cn.ijingxi.common.com;

import cn.ijingxi.common.util.jxLog;
import cn.ijingxi.common.util.jxTimer;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by andrew on 15-12-29.
 */
public class jxTCPServer {

    private ServerSocket serverSocket = null;
    private int port = 0;

    private ITCPServer dual = null;

    public Thread start(int port, ITCPServer dual, Object param) throws IOException {
        jxLog.logger.debug("jxTCPServer start at:" + port);
        this.port = port;
        this.dual = dual;
        serverSocket = new ServerSocket(port);
        return jxTimer.asyncRun_Repeat(param1 -> run(param1), param, true);
    }


    private void run(Object param) throws IOException {
        Socket socket = serverSocket.accept();
        jxLog.logger.debug("incoming:" + socket.getRemoteSocketAddress().toString());
        Thread th = new Thread(() -> {
            try {
                dual.Incoming(param,
                        new DataInputStream(socket.getInputStream()),
                        socket.getOutputStream());
            } catch (Exception ex) {
                //最好自己处理异常
                jxLog.error(ex);
                return;
            }
        });
        th.start();
    }
}
