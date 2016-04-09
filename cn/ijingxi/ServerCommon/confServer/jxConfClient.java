package cn.ijingxi.ServerCommon.confServer;

import cn.ijingxi.ServerCommon.httpClient.jxHttpClient;
import cn.ijingxi.common.orm.jxJson;
import cn.ijingxi.common.system.Config;
import cn.ijingxi.common.util.Trans;

/**
 * Created by andrew on 15-12-6.
 */
public class jxConfClient {

    private static jxHttpClient confClient=null;

    public static boolean registerService(String serviceName,jxJson store) {
        try {
            if(checkClient()) {
                store.AddValue("Name",serviceName);
                jxJson rs= confClient.post("/registerService",store);
                if(rs!=null)
                    return jxJson.checkResult(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static jxJson getServiceInfo(String serviceName) {
        try {
            if(checkClient()) {
                jxJson param=jxJson.GetObjectNode("Service");
                param.AddValue("Name",serviceName);
                return confClient.post("/getServiceInfo",param);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean checkClient(){
        try {
            if(confClient==null) {
                String IP = (String) Config.getConfig(Config.ConfService_Name, Config.ConfService_IP);
                if (IP == null) {
                    //jxLog.log(LogPriority.alert, 0, Message.MsgID_Config, "jxConfClient", "未查找到配置服务");
                    return false;
                }
                int port = Trans.TransToInteger(Config.getConfig(Config.ConfService_Name,
                        Config.ConfService_Port));
                confClient = new jxHttpClient(IP,port);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
