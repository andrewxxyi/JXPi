package cn.ijingxi.communication.USRIO88;

import cn.ijingxi.ServerCommon.httpServer.RES;
import cn.ijingxi.ServerCommon.httpServer.jxHttpData;
import cn.ijingxi.app.ActiveRight;
import cn.ijingxi.communication.jxNIOTCPClient;
import cn.ijingxi.intelControl.jxLua;
import cn.ijingxi.orm.jxJson;
import cn.ijingxi.util.Trans;
import cn.ijingxi.util.jxLog;
import cn.ijingxi.util.jxTimer;
import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;

import java.util.Map;

/**
 * Created by andrew on 16-3-5.
 */
@ActiveRight(policy= ActiveRight.Policy.Accept)
public class FrontUSR {

    //public static final String confFileName = "./conf/FrontUSR_conf.lua";
    public static final String FrontDeviceTypeName_USR = "USR88";
    //public static final int FrontDeviceType_USR = 1;


    @RES
    public static jxHttpData call(Map<String, Object> ps, jxJson Param) throws Exception {

        String fn = (String) Param.GetSubValue("Function");
        String p1 = null, p2 = null, p4 = null;
        int p3 = 0, p5 = 0, p6 = 0;
        Object name = Param.GetSubValue("DevName");
        if (name != null)
            p1 = (String) name;
        Object cmd = Param.GetSubValue("Cmd");
        if (cmd != null)
            p2 = (String) cmd;
        Object channel = Param.GetSubValue("Channel");
        if (channel != null)
            p3 = Trans.TransToInteger(channel);
        Object lockname = Param.GetSubValue("Lock");
        if (lockname != null) {
            p4 = (String) lockname;
            if ("".compareTo(p4) == 0 || "null".compareTo(p4) == 0)
                p4 = null;
        }
        Object inverserchannelid = Param.GetSubValue("InverseChannel");
        if (inverserchannelid != null)
            p5 = Trans.TransToInteger(inverserchannelid);
        Object delaysecond = Param.GetSubValue("DelaySecond");
        if (delaysecond != null)
            p6 = Trans.TransToInteger(delaysecond);


        Object async = Param.GetSubValue("Async");
        if (async != null) {
            final String finalP = p1;
            final String finalP1 = p2;
            final int finalP2 = p3;
            final String finalP3 = p4;
            final int finalP4 = p5;
            final int finalP5 = p6;

            jxTimer.asyncRun(param -> call(fn, finalP, finalP1,
                    finalP2, finalP3, finalP4, finalP5));
            jxHttpData rs = new jxHttpData(200, "OK");
            rs.addValue("Result", true);
            return rs;
        } else {
            LuaValue luars = call(fn, p1, p2, p3, p4, p5, p6);
            jxHttpData rs = new jxHttpData(200, "OK");
            rs.addValue("Result", luars.toString());
            return rs;
        }
    }

    static LuaValue call(String scriptName,String devName,String cmd,int channelID,
                                       String lockName,int inverseChannel,
                                       int delaySecond) throws Exception {
        LuaValue p1=null,p2=null,p3=null,p4=null,p5=null,p6=null;
        if(devName!=null)
            p1=LuaValue.valueOf(devName);
        else
            p1=LuaValue.NIL;
        if(cmd!=null)
            p2=LuaValue.valueOf(cmd);
        else
            p2=LuaValue.NIL;
        if(channelID!=0)
            p3=LuaValue.valueOf(channelID);
        else
            p3=LuaValue.NIL;
        if(lockName!=null)
            p4=LuaValue.valueOf(lockName);
        else
            p4=LuaValue.NIL;
        if(inverseChannel!=0)
            p5=LuaValue.valueOf(inverseChannel);
        else
            p5=LuaValue.NIL;
        if(delaySecond!=0)
            p6=LuaValue.valueOf(delaySecond);
        else
            p6=LuaValue.NIL;

        return jxLua.runFile(jxLua.LuaScriptRoot + scriptName+".lua",p1,p2,p3,p4,p5,p6);

    }

    public static void init() throws Exception {
        jxLog.logger.debug("FrontUSR init");

        jxLua.registerFront(FrontDeviceTypeName_USR,map -> {
            String name = ((LuaString) map.get("name")).checkjstring();
            String passwd = ((LuaString) map.get("passwd")).checkjstring();
            int ver = ((LuaInteger) map.get("ver")).checkint();
            String ip = ((LuaString) map.get("ip")).checkjstring();
            int port = ((LuaInteger) map.get("port")).checkint();
            boolean startRead = ((LuaBoolean) map.get("startRead")).checkboolean();
            jxLog.logger.debug(String.format("FrontUSR open %s at %s:%d", name, ip, port));
            jxNIOTCPClient client = open(ip, port);
            if (client != null) {
                try {
                    luaFront_USRIO88 lf = new luaFront_USRIO88(name,passwd, "USR", ver, client, startRead);
                    lf.setAttribute("ip", ip);
                    lf.setAttribute("port", port);
                    lf.setAttribute("startRead", startRead);
                    jxLog.logger.debug("FrontUSR open end");
                    return lf;
                } catch (Exception e) {
                    jxLog.error(e);
                }
            } else
                jxLog.logger.debug(String.format("无法连接到:%s at %s:%d", name, ip, port));
            return null;
        });

        //FrontDevice.addDevType(FrontDeviceTypeName_USR, FrontDeviceType_USR);

        /*
        jxLua.addConf(confFileName, map -> {
            String name = ((LuaString) map.get("name")).checkjstring();
            int ver = ((LuaInteger) map.get("ver")).checkint();
            String ip = ((LuaString) map.get("ip")).checkjstring();
            int port = ((LuaInteger) map.get("port")).checkint();
            boolean startRead = ((LuaBoolean) map.get("startRead")).checkboolean();
            jxLog.debug(String.format("FrontUSR open %s at %s:%d", name, ip, port));
            jxNIOTCPClient client = open(ip, port);
            if (client != null) {
                try {
                    luaFront_USRIO88 lf = new luaFront_USRIO88(name, "USR", ver, client, startRead);
                    lf.setAttribute("ip", ip);
                    lf.setAttribute("port", port);
                    lf.setAttribute("startRead", startRead);
                    jxLog.debug("FrontUSR open end");
                    return lf;
                } catch (Exception e) {
                    jxLog.error(e);
                }
            } else
                jxLog.debug(String.format("无法连接到:%s at %s:%d", name, ip, port));
            return null;
        });
        */
    }
    private static jxNIOTCPClient open(String ip, int port) {
        try {
            jxNIOTCPClient client = new jxNIOTCPClient();
            client.open(ip, port);
            return client;
        } catch (Exception e) {
            //jxLog.error(e);
            return null;
        }
    }


}
