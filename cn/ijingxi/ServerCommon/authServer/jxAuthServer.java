package cn.ijingxi.ServerCommon.authServer;

import cn.ijingxi.ServerCommon.httpServer.jxHttpRes;
import cn.ijingxi.ServerCommon.httpServer.jxHttpServer;
import cn.ijingxi.common.system.Config;
import cn.ijingxi.common.util.Trans;

/**
 * Created by andrew on 15-12-6.
 */
public class jxAuthServer {

    public static final String AuthService_Name="AuthService";
    public static final String AuthService_IP="IP";
    public static final String AuthService_Port="Port";

    private static jxHttpServer server=null;

    public static void start() throws Exception {

        jxHttpRes.InitResClass(AuthServer.class);

        String IP = (String) Config.getConfig(jxAuthServer.AuthService_Name,
                jxAuthServer.AuthService_IP);
        if (IP == null) {
            //jxLog.log(LogPriority.alert, 0, Message.MsgID_Auth, "jxAuthClient", "未查找到认证服务");
        }
        int port = Trans.TransToInteger(Config.getConfig(jxAuthServer.AuthService_Name,
                jxAuthServer.AuthService_Port));
        server=new jxHttpServer(port, ".", null);
        server.start();
    }


}
