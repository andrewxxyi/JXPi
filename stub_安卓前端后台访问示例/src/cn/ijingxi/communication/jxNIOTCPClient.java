package cn.ijingxi.communication;

import cn.ijingxi.stub.general.IDo;
import cn.ijingxi.stub.general.jxJson;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by andrew on 15-11-23.
 */
public class jxNIOTCPClient {
    protected static final int bufferSize = 65536;
    protected byte[] buffer = new byte[bufferSize];
    private Socket socket = null;
    protected DataInputStream ins = null;
    protected OutputStream outs = null;
    private String serverIP = null;
    private int port = 0;
    IDo<jxJson> receiveDual=null;

    public void open(String serverIP, int port) throws Exception {
        this.serverIP = serverIP;
        this.port = port;
        open();
    }

    protected  void open() throws IOException {
        socket = new Socket(serverIP, port);
        ins = new DataInputStream(socket.getInputStream());
        outs = socket.getOutputStream();
    }


    public void close() {
        try {
            if (socket != null) {
                ins.close();
                outs.close();
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DataInputStream sendData(byte[] data) {
        try {
            outs.write(data);
            outs.flush();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                open();
                outs.write(data);
                outs.flush();
            } catch (IOException e1) {
                e1.printStackTrace();
                return null;
            }
        }
        return ins;
    }

    public byte[] send(byte[] data) throws IOException {
        DataInputStream is = sendData(data);
        //byte[] buffer = new byte[bufferSize];
        int count = is.read(buffer);
        if (count == 0) return null;
        if (count < bufferSize) {
            byte[] rs = new byte[count];
            System.arraycopy(buffer, 0, rs, 0, count);
            return rs;
        }
        return buffer;
    }

    public jxJson send(jxJson json) throws Exception {
        //utils.checkAssert(json != null, "需给出发送数据");
        DataInputStream is = sendData(json.TransToString().getBytes());
        int count = is.read(buffer);
        if (count == 0) return null;
        //utils.checkAssert(count <= bufferSize, "接收到的数据超过缓冲区限制：" + bufferSize);
        byte[] rs = new byte[count];
        System.arraycopy(buffer, 0, rs, 0, count);
        String s = String.valueOf(buffer);
        return jxJson.JsonToObject(s);
    }




}
