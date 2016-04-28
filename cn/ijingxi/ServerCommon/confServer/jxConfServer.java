package cn.ijingxi.ServerCommon.confServer;

import cn.ijingxi.ServerCommon.httpServer.jxHttpRes;
import cn.ijingxi.ServerCommon.httpServer.jxHttpServer;
import cn.ijingxi.system.Config;

/**
 * Created by andrew on 15-12-6.
 */
public class jxConfServer {

    private static jxHttpServer server=null;

    public static void start() throws Exception {
        jxHttpRes.InitResClass(ConfServer.class);
        server=new jxHttpServer(Config.ConfServer_Port, ".", null);
        server.start();
    }

}
