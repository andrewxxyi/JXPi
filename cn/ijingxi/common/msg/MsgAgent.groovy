package cn.ijingxi.common.msg

import cn.ijingxi.common.util.utils

/**
 * Created by andrew on 15-9-4.
 */
class MsgAgent {
    static def map = [:]
    static def receiveMsgThread;
    static void register(UUID mid,IMsgDual res){
        def mq=map.get(mid)
        if(mq==null){
            IMsgService service = Message.getMsgService();
            if(service!=null)
                service.Register(mid);
            mq=[:]
            map.put(mid,mq)
        }
        mq.put(res.getID(),res)
    }
    static void unRegister(UUID mid,UUID resid){
        def mq=map.get(mid)
        if(mq!=null){
            def res=mq.get(resid)
            if(res!=null){
                mq.remove(resid)
                if(mq.size()==0){
                    IMsgService service = Message.getMsgService();
                    if(service!=null)
                        service.UnRegister(mid);
                    map.remove(mid)
                }
            }
        }
    }
    private static void post(Message msg){
        def mq=map.get(msg.ID)
        if(mq!=null){
            mq.each {
                k,v->v.DualMsg(msg)
            }
        }
    }
    static void Start(){
        receiveMsgThread = new ReceiveMsgThread();
        receiveMsgThread.setDaemon(false);
        receiveMsgThread.start();
    }

    private static class ReceiveMsgThread extends Thread {
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    IMsgService service = Config.getMsgService();
                    if(service!=null)
                        Message msg=service.ReceiveMsg()
                    if(msg!=null)
                        post(msg)

                } catch (InterruptedIOException ex) {
                    break;
                } catch (IOException e) {
                    e.printStackTrace()
                    utils.P("ReceiveMsgThread", "error: "+ e.getMessage())
                    break;
                }
            }
        }

    }



}
