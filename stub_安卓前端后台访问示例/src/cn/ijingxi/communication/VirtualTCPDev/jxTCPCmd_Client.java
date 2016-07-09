package cn.ijingxi.communication.VirtualTCPDev;

import android.annotation.TargetApi;
import android.os.Build;
import cn.ijingxi.communication.jxNIOTCPClient;
import cn.ijingxi.stub.general.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by andrew on 16-1-9.
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class jxTCPCmd_Client extends jxNIOTCPClient {

    class waitSend {
        jxJson json;
        IDo<String> errorDual;
    }

    private BlockingDeque<waitSend> sendQueue = new LinkedBlockingDeque<>();
    //发送方、接收方、发送方msgid组成一个唯一性的会话标识
    private jxMatrix<String,IDo<jxJson>> getResultDuals= new jxMatrix<>();
    private void putDual(String sender, String receiver, Integer msgid, IDo<jxJson> dual) throws Exception {
        Map<String,String> map=new HashMap<>();
        map.put("sender",sender);
        map.put("receiver",receiver);
        map.put("msgid",msgid.toString());

        getResultDuals.put(map,dual);
    }
    private IDo<jxJson> getDual(String sender, String receiver, Integer msgid) throws Exception {
        Map<String,String> map=new HashMap<>();
        map.put("sender",sender);
        map.put("receiver",receiver);
        map.put("msgid",msgid.toString());

        return getResultDuals.get(map);
    }

    private Thread sendThread = null;
    private Thread receiveThread = null;

    public boolean logged = false;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public jxTCPCmd_Client(String serverIP, int port, final IDo<jxJson> cmdDual) throws Exception {
        open(serverIP, port);
        sendThread = jxTimer.asyncRun_Repeat(
                new IFunc<Boolean, Object>() {
                    @Override
                    public Boolean Do(Object param) throws Exception {
                        jxTCPCmd_Client ct = (jxTCPCmd_Client) param;
                        waitSend ws = ct.sendQueue.take();
                        try {
                            ct.send_json(ws.json);
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (ws.errorDual != null)
                                ws.errorDual.Do(e.getMessage());
                            return false;
                        }
                    }
                }, this);
        receiveThread = jxTimer.asyncRun_Repeat(
                new IFunc<Boolean, Object>() {
                    @Override
                    public Boolean Do(Object param) throws Exception {
                        jxTCPCmd_Client ct = (jxTCPCmd_Client) param;
                        try {
                            int count = ct.ins.read(buffer);
                            //jxLog.logger.debug("receive:" + count);
                            if (count == 0) return true;
                            if (count < 0) {
                                close();
                                return false;
                            }
                            byte[] rs = new byte[count];
                            System.arraycopy(buffer, 0, rs, 0, count);
                            String sss = new String(rs);
                            //if (logged)
                            //    jxLog.logger.debug("receive:" + sss);
                            String[] ss = utils.StringSplit(sss, "\n");
                            //jxLog.logger.debug("接收到的消息数量："+ss.length);
                            for (String s : ss) {
                                jxJson json = jxJson.JsonToObject(s);
                                //查找自己主动发送信息的响应处理函数
                                IDo<jxJson> dual = getDual(vDevJSON.getReceiver(json), vDevJSON.getSender(json), vDevJSON.getMsgID(json));
                                if (dual != null)
                                    //自己发送的信息的响应应如何处理
                                    jxTimer.asyncRun(dual, json);
                                else if (cmdDual != null)
                                    //对端发送过来的消息应该如何处理
                                    jxTimer.asyncRun(cmdDual, json);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                        return true;
                    }
                }, this);
    }

    public void send(jxJson json, IDo<jxJson> getResultDual, IDo<String> errorDual) throws Exception {
        utils.checkAssert(json != null, "不能发送空数据");
        //if (logged)
        //    jxLog.logger.debug("send:" + json.TransToString());
        putDual(vDevJSON.getSender(json), vDevJSON.getReceiver(json), vDevJSON.getMsgID(json), getResultDual);
        waitSend ws = new waitSend();
        ws.json = json;
        ws.errorDual = errorDual;
        sendQueue.offer(ws);
    }

    public void send_json(jxJson json) {
        if (json == null) return;
        byte[] data = (json.TransToString()+"\n").getBytes();
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
            }
        }
    }


}
