package cn.ijingxi.com.TCP232;

import cn.ijingxi.ServerCommon.httpServer.RES;
import cn.ijingxi.ServerCommon.httpServer.jxHttpData;
import cn.ijingxi.app.ActiveRight;
import cn.ijingxi.communication.jxNIOTCPClient;
import cn.ijingxi.intelControl.FrontDevice;
import cn.ijingxi.intelControl.jxLua;
import cn.ijingxi.orm.jxJson;
import cn.ijingxi.util.jxLog;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;

import java.util.Map;

/**
 * Created by andrew on 16-3-5.
 */
@ActiveRight(policy= ActiveRight.Policy.Accept)
public class FrontUSR232 {

    public static final String confFileName = "./conf/FrontUSR232_conf.lua";
    public static final String FrontDeviceTypeName_USR = "USR232";
    public static final int FrontDeviceType_USR = 2;


    @RES
    public static jxHttpData call(Map<String, Object> ps, jxJson Param) throws Exception {

        String fn = (String) Param.GetSubValue("Function");
        LuaValue p1=null,p2=null;
        Object name = Param.GetSubValue("DevName");
        if(name!=null)
            p1=LuaValue.valueOf((String) name);
        else
            p1=LuaValue.NIL;
        Object cmd = Param.GetSubValue("Cmd");
        if(cmd!=null)
            p2=LuaValue.valueOf((String) cmd);
        else
            p2=LuaValue.NIL;

        jxLua.runFile_Async(jxLua.LuaScriptRoot + fn+".lua",null,p1,p2);

        jxHttpData rs = new jxHttpData(200, "OK");
        rs.addValue("Result", true);
        return rs;
    }

    public static void init() throws Exception {
        jxLog.debug("FrontUSR232 init");
        FrontDevice.addDevType(FrontDeviceTypeName_USR, FrontDeviceType_USR);

        jxLua.addConf(confFileName, map -> {
            String name = ((LuaString) map.get("name")).checkjstring();
            int ver = ((LuaInteger) map.get("ver")).checkint();
            String ip = ((LuaString) map.get("ip")).checkjstring();
            int port = ((LuaInteger) map.get("port")).checkint();
            jxLog.debug(String.format("FrontUSR232 opening %s at %s:%d", name, ip, port));
            jxNIOTCPClient client = open(ip, port);
            if (client != null) {
                try {
                    luaFront_USR232 lf = new luaFront_USR232(name,"USR232",ver,client);
                    lf.setAttribute("ip", ip);
                    lf.setAttribute("port", port);
                    jxLog.debug("FrontUSR232 open end");
                    return lf;
                } catch (Exception e) {
                    jxLog.error(e);
                }
            } else
                jxLog.debug(String.format("无法连接到:%s at %s:%d", name, ip, port));
            return null;
        });

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
