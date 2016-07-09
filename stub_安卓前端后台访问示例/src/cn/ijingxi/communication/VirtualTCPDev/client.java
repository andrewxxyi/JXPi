package cn.ijingxi.communication.VirtualTCPDev;

import cn.ijingxi.stub.general.IDo;
import cn.ijingxi.stub.general.IFunc;
import cn.ijingxi.stub.general.IFunc3;
import cn.ijingxi.stub.general.jxJson;

/**
 * Created by andrew on 16-6-30.
 */
public class client {

    private Object lock = new Object();
    private jxTCPCmd_Client cmdCliend = null;

    //接收到的消息
    private jxJson rs = null;

    //自己发送消息后接收到服务端的响应处理
    private IDo<jxJson> getResultDual = new IDo<jxJson>() {
        @Override
        public void Do(jxJson param) throws Exception {
            String sender = vDevJSON.getSender(param);
            String cmd = vDevJSON.getCmd(param);
            jxJson js=null;
            switch (cmd) {

                case "r":
                    synchronized (lock) {
                        rs = param;
                        lock.notify();
                    }
                    break;
            }
        }
    };

    IDo<String> errorDual = null;

    public void setLogged(boolean logged) {
        cmdCliend.logged = logged;
    }

    /**
     *
     * @param serverIP
     * @param port
     * @param cmdDual 用户自定义的扩展处理
     * @param informDual 处理服务端发送的通知的函数
     * @throws Exception
     */
    public client(String serverIP, int port, final IFunc<jxJson,jxJson> cmdDual, final IFunc3<jxJson,Integer,Integer,jxJson> informDual) throws Exception {
        cmdCliend = new jxTCPCmd_Client(serverIP, port, new IDo<jxJson>() {
            @Override
            public void Do(jxJson param) throws Exception {
                //接收到服务端发送过来的命令
                //jxLog.logger.debug(p.TransToString());

                String sender = vDevJSON.getSender(param);
                String cmd = vDevJSON.getCmd(param);
                jxJson js = null;
                switch (cmd) {
                    case "reg":
                        js = vDevJSON.getCmd_reg(sender, "0000");
                        cmdCliend.send_json(js);
                        break;
                    case "l":
                        //jxLog.logger.debug("receive keeplive from:" + sender);
                        js = vDevJSON.getCmd_response(param);
                        cmdCliend.send_json(js);
                        break;
                    case "i":
                        Integer pin = (Integer) param.getSubObjectValue("p");
                        Integer state = (Integer) param.getSubObjectValue("d");
                        //jxLog.logger.debug(String.format("receive inform from %s:%d(pin)/%d(state)", sender, pin, state));
                        if (informDual != null) {
                            js = informDual.Do(pin, state, param);
                            cmdCliend.send_json(js);
                        }
                        break;
                    default:
                        if (cmdDual != null) {
                            js = cmdDual.Do(param);
                            cmdCliend.send_json(js);
                        }
                }
            }
        });
    }

    public jxJson send(jxJson json) throws Exception {
        rs = null;
        synchronized (lock) {
            cmdCliend.send(json, getResultDual, errorDual);
            try {
                lock.wait(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return rs;
    }

    /**
     * 主动向服务端发出一个通知
     * @param json 需要调用vDevJSON.getCmd_inform，然后再根据自己的需要进行设置
     * @return
     */
    public jxJson inform(jxJson json) throws Exception {
        return send(json);
    }


}
