package cn.ijingxi.communication.VirtualTCPDev;


import cn.ijingxi.stub.general.jxJson;
import cn.ijingxi.stub.general.utils;

/**
 * Created by andrew on 16-7-1.
 */
public class vDevJSON {

    private static final int msgidRandomBase=0x20000000;
    private static Integer msgid = 0;
    public static String name = null;
    static {
        //发送方、接收方、发送方msgid组成一个唯一性的会话标识，但如果都从0开始则很有可能会出现碰撞，
        //所以使用一个较大的随机空间来降低碰撞概率
        msgid= utils.getRandomInt(msgidRandomBase);
    }


    private static int getMsgID() {
        synchronized (msgid) {
            msgid++;
            if (msgid == Integer.MAX_VALUE)
                msgid = 1;
            return msgid;
        }
    }




    public static jxJson getCmd_reg(String peer,String id) throws Exception {
        jxJson json = getCmd(peer,"reg");
        json.AddValue("ID", id);
        return json;
    }
    public static jxJson getCmd_keeplive(String peer) throws Exception {
        jxJson json = getCmd(peer,"l");
        return json;
    }
    public static jxJson getCmd_response(jxJson json) throws Exception {
        jxJson rs = jxJson.GetObjectNode("x");
        //自己的名字
        rs.AddValue("n", name);
        //对端的名字
        rs.AddValue("e", getSender(json));
        //消息ID，这三者就构成了一次通信的标识（n,p,mid）
        rs.AddValue("mid", getMsgID(json));
        rs.AddValue("c", "r");
        return rs;
    }

    public static jxJson getCmd_inform(String peer, int pin, int state) throws Exception {
        jxJson json = getCmd(peer,"i");
        json.AddValue("p", pin);
        json.AddValue("d", state);
        return json;
    }

    public static Object getData(jxJson json) throws Exception {
        if(json==null)return null;
        return json.getSubObjectValue("d");
    }
    public static String getMsg(jxJson json) throws Exception {
        if(json==null)return "";
        return json.GetSubValue_String("m");
    }
    public static String getCmd(jxJson json) throws Exception {
        if(json==null)return "";
        return json.GetSubValue_String("c");
    }

    public static jxJson getCmd(String peer, String cmd) throws Exception {
        jxJson json = jxJson.GetObjectNode("x");
        //自己的名字
        json.AddValue("n", name);
        //对端的名字
        json.AddValue("e", peer);
        //消息ID，这三者就构成了一次通信的标识（n,p,mid）
        json.AddValue("mid", getMsgID());
        json.AddValue("c", cmd);
        return json;
    }

    public static String getReceiver(jxJson json) throws Exception {
        return json.GetSubValue_String("e");
    }

    //对于接收方来说
    public static String getSender(jxJson json) throws Exception {
        return json.GetSubValue_String("n");
    }
    public static String getSenderID(jxJson json) throws Exception {
        return json.GetSubValue_String("ID");
    }

    public static int getMsgID(jxJson json) throws Exception {
        return json.GetSubValue_Integer("mid");
    }


}
