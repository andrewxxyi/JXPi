package cn.ijingxi.com.TCP232;

import cn.ijingxi.communication.jxNIOTCPClient;
import cn.ijingxi.intelControl.FrontCommunication;

/**
 * Created by andrew on 16-3-5.
 */
public class luaFront_USR232 extends FrontCommunication {

    jxNIOTCPClient client=null;
    public luaFront_USR232(String devName,String devType,int ver,
                           jxNIOTCPClient client) throws Exception {
        super(devName,devType);
        this.ver=ver;
        this.client=client;
    }

    @Override
    public Object send(Object data) throws Exception {
        byte b=(byte)data;
        return client.send(new byte[]{b});
    }

    @Override
    public void close() {
        client.close();
    }
}
