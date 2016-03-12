package cn.ijingxi.common.msg;

import cn.ijingxi.common.system.Config;
import cn.ijingxi.common.util.jxLog;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by andrew on 15-11-20.
 */
public class MsgAgent {
    public static IMsgService service;
    private static Map<UUID, msgDualStore> map = new HashMap<>();
    private static ReceiveMsgThread receiveMsgThread;

    public static Integer register(UUID mid, IMsgDual res) {
        msgDualStore store = map.get(mid);
        Map<Integer,IMsgDual> dq=null;
        if (store == null) {
            if (service != null)
                service.Register(mid);
            store = new msgDualStore();
            map.put(mid, store);
        }
        store.count++;
        store.dualMap.put(store.count,res);
        return store.count;
    }

    public static void unRegister(UUID mid, Integer count) {
        msgDualStore store = map.get(mid);
        if (store != null) {
            store.dualMap.remove(count);
            if(store.dualMap.size()==0){
                map.remove(mid);
                if (service != null)
                    service.UnRegister(mid);
            }
        }
    }

    private static void post(Message msg) {
        msgDualStore store = map.get(msg.ReceiverID);
        if (store != null) {
            for (IMsgDual dual : store.dualMap.values())
                dual.DualMsg(msg);
        }
    }

    public static void send(Message msg) {
        service.SendMsg(msg);
    }

    public static void Start() throws Exception {
        service = (IMsgService) Config.getConfig("MsgService","local");
        receiveMsgThread = new ReceiveMsgThread();
        receiveMsgThread.setDaemon(true);
        receiveMsgThread.start();
    }

    private static class ReceiveMsgThread extends Thread {
        @Override
        public void run() {
            if (service == null) {
                //jxLog.log(LogPriority.crit, ORMType.jxSystem.ordinal(),jxSystem.SystemID,"MsgAgent","MsgService未设置，消息系统无法运行！！！")
                return;
            }
            while (!Thread.interrupted()) {
                try {
                    Message msg = service.ReceiveMsg();
                    if (msg != null)
                        post(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    jxLog.logger.debug("ReceiveMsgThread error: " + e.getMessage());
                    break;
                }
            }
        }

    }

    private static class msgDualStore{
        int count=0;
        Map<Integer, IMsgDual> dualMap=new HashMap<>();
    }

}
