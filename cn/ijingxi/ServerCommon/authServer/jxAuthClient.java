package cn.ijingxi.ServerCommon.authServer;

import cn.ijingxi.ServerCommon.httpClient.jxHttpClient;
import cn.ijingxi.orm.jxJson;
import cn.ijingxi.system.Config;
import cn.ijingxi.system.jxAutoDeleteMap;
import cn.ijingxi.util.Trans;

/**
 * 本类只有webserver会用到，其它服务都是以webserver为用户代理的，
 * 默认只要webserver发出响应就认为webserver已经进行过身份认证和权限检验了
 *
 * Created by andrew on 15-12-6.
 */
public class jxAuthClient {

    private static jxHttpClient authClient=null;

    //保留用户信息15分钟，用户有动作则刷新，key为authCode
    private static jxAutoDeleteMap admap=new jxAutoDeleteMap(15*60,null);

    public static boolean checkUserPasswd(String userName,String password) {
        try {
            if(checkClient()) {
                jxJson param=jxJson.GetObjectNode("Auth");
                param.AddValue("Name",userName);
                param.AddValue("Passwd",password);
                jxJson rs= authClient.post("/checkUserPasswd",param);
                if(rs!=null&&jxJson.checkResult(rs)){
                    rs.AddValue("UserName",userName);
                    rs.AddValue("Passwd",password);
                    admap.put((String) rs.GetSubValue("AuthCode"),rs);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 只在本地进行检测，如果超过15分钟没有活动，则弹回到登陆界面，
     * 本功能目前实现的前提是系统中只有一台webserver
     * @param authCode
     * @return
     */
    public static boolean checkUserToken(String authCode) {
        try {
            if(checkClient()) {
                jxJson rs= (jxJson) admap.get(authCode);
                if(rs!=null){
                    admap.retick(authCode);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 用户是否有权执行某service的某项active
     * @param authCode
     * @param serviceName
     * @param active get意为读取、post意为创建、put修改、delete删除
     * @return
     */
    public static boolean checkUserRight(String authCode,String serviceName,String active){
        try {
            if(checkClient()) {
                jxJson p = (jxJson) admap.get(authCode);
                jxJson param=jxJson.GetObjectNode("Auth");
                param.AddValue("Code",authCode);
                //如果时间过长，authserver已经将authcode超时删除了，则需要重新认证，并返回新的authcode
                param.AddValue("Name",p.GetSubValue("UserName"));
                param.AddValue("Passwd",p.GetSubValue("Passwd"));
                param.AddValue("Service",serviceName);
                param.AddValue("Active",active);
                jxJson rs= authClient.post("/checkUserRight",param);
                if(rs!=null){
                    String code=(String) rs.GetSubValue("AuthCode");
                    if(code.compareTo(authCode)!=0){
                        //authcode在authserver已超时
                        rs.AddValue("UserName",p.GetSubValue("UserName"));
                        rs.AddValue("Passwd",p.GetSubValue("Passwd"));
                        admap.remove(authCode);
                        admap.put(code,rs);
                    }
                    return Trans.TransToBoolean(rs.GetSubValue("Result"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean checkClient(){
        try {
            if(authClient==null) {
                String IP = (String) Config.getConfig(jxAuthServer.AuthService_Name,
                        jxAuthServer.AuthService_IP);
                if (IP == null) {
                    //jxLog.log(LogPriority.alert, 0, Message.MsgID_Auth, "jxAuthClient", "未查找到认证服务");
                    return false;
                }
                int port = Trans.TransToInteger(Config.getConfig(jxAuthServer.AuthService_Name,
                        jxAuthServer.AuthService_Port));
                authClient = new jxHttpClient(IP,port);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



}
