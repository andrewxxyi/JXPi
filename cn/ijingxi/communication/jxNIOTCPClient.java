package cn.ijingxi.communication;

import cn.ijingxi.util.jxLog;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by andrew on 15-11-23.
 */
public class jxNIOTCPClient {
    private Socket socket=null;
    private DataInputStream ins=null;
    private OutputStream outs=null;
    private String serverIP=null;
    private int port=0;

    public void open(String serverIP,int port) throws Exception {
        this.serverIP=serverIP;
        this.port=port;
        open();
    }
    void open() throws IOException {
        socket=new Socket(serverIP, port);
        ins = new DataInputStream(socket.getInputStream());
        outs = socket.getOutputStream();
    }
    public void close() {
        try {
            if(socket!=null) {
                ins.close();
                outs.close();
                socket.close();
                socket=null;
            }
        } catch (IOException e) {
            jxLog.error(e);
        }
    }
    private byte[] readrs=null;
    private Object readwait=new Object();
    public byte[] send(byte[] data) {
        try {
            outs.write(data);
            outs.flush();
        } catch (IOException e) {
            jxLog.error(e);
            try {
                open();
                outs.write(data);
                outs.flush();
            } catch (IOException e1) {
                jxLog.error(e1);
                return null;
            }
        }
        Thread tr=null;
        synchronized (readwait) {
            try {
                readrs = null;
                tr=read();
                readwait.wait(3000);
                //当本函数返回后，readrs被清除掉也不影响
                return readrs;
            } catch (InterruptedException e) {
                jxLog.error(e);
                tr.interrupt();
            }
        }
        return null;
    }
    private Thread read(){
        Thread th=new Thread(() -> {
            synchronized (readwait) {
                int readBytes = 0;
                byte[] buf = new byte[1024];
                try {
                    readBytes = ins.read(buf);
                } catch (IOException e) {
                    jxLog.error(e);
                }
                if(readBytes>0) {
                    readrs = new byte[readBytes];
                    System.arraycopy(buf, 0, readrs, 0, readBytes);
                }
                readwait.notify();
            }
        });
        th.start();
        return th;
    }
    public DataInputStream sendData(byte[] data) {
        try {
            outs.write(data);
            outs.flush();
        } catch (IOException e) {
            jxLog.error(e);
            try {
                open();
                outs.write(data);
                outs.flush();
            } catch (IOException e1) {
                jxLog.error(e1);
                return null;
            }
        }
        return ins;
    }


}
