package cn.ijingxi.common.system;

import cn.ijingxi.common.msg.IMsgDual;
import cn.ijingxi.common.msg.Message;
import cn.ijingxi.common.msg.MsgAgent;
import cn.ijingxi.common.orm.jxJson;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统配置服务前端，一个系统存在系统级别的配置服务，单方面的广播服务
 * 由配置服务提供者，广播系统基本的配置信息，如果需要注册，则通过广播接收到的配置服务器用REST进行访问
 *
 *
 *
 * Created by andrew on 15-9-4.
 */
public class Config {
    public static final String ConfServer_IP="172.31.253.253";
    public static final int ConfServer_Port=65534;

    public static final String ConfService_Name="ConfService";
    public static final String ConfService_IP="IP";
    public static final String ConfService_Port="Port";

    private static Object lock=new Object();
    private static Map<String,jxJson> config_System=new HashMap<>();
    private static Map<String,jxJson> config_Local=new HashMap<>();

    public static void setConfig(String serviceName,String confName,Object value) throws Exception {
        synchronized (lock){
            jxJson store=config_Local.get(serviceName);
            if(store==null){
                store=jxJson.GetObjectNode("config");
                config_Local.put(serviceName,store);
            }
            store.AddValue(confName,value);
        }
    }
    public static Object getConfig(String serviceName,String confName) throws Exception {
        synchronized (lock) {
            jxJson store = config_Local.get(serviceName);
            if (store == null)
                store=config_System.get(serviceName);
            if(store != null)
                return store.getSubObjectValue(confName);
            return null;
        }
    }

    public static void start(){
        MsgAgent.register(Message.MsgID_Config, new IMsgDual() {
            @Override
            public void DualMsg(Message msg) {
                try {
                    switch (msg.MsgUsedID){
                        case 1:
                            //重置所有系统配置
                            synchronized (lock){
                                config_System=DualConfigMsg_Service(msg);
                            }
                            break;
                        case 2:
                            //修正或添加配置
                            synchronized (lock){
                                Map<String, jxJson> cs = DualConfigMsg_Service(msg);
                                for(Map.Entry<String, jxJson> entry:cs.entrySet()){
                                    config_System.remove(entry.getKey());
                                    config_System.put(entry.getKey(),entry.getValue());
                                }
                            }
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static Map<String,jxJson> DualConfigMsg_Service(Message msg) throws Exception {
        Map<String,jxJson> rs=new HashMap<>();
        if(msg.Info!=null&&msg.Info!=""){
            jxJson json=jxJson.JsonToObject(msg.Info);
            if(json!=null){
                jxJson js=json.GetSubObject("Services");
                if(js!=null) {
                    //js是service的数组
                    for (jxJson service : js) {
                        jxJson store = jxJson.GetObjectNode("Service");
                        for (jxJson sub : service) {
                            if (sub.getName().compareTo("Name") == 0)
                                rs.put((String) sub.getValue(), store);
                            else
                                store.AddValue(sub.getName(),sub.getValue());
                        }
                    }
                }
            }
        }
        return rs;
    }


}
