package cn.ijingxi.communication;

import cn.ijingxi.util.*;

import java.io.DataInputStream;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 命令响应形式，即一发一答
 * 每个comTrans对象代表和一个受控对象的通信控制
 * 需要注意的是：
 * 1、本类的send函数是异步收发，所有的接收和错误通知都由调用者自行处理，当然系统在出现系统错误时，如没有响应等也会送出错误通知
 * 2、但send_Sync函数，是利用send来实现的同步收发，其有自定义的getResultDual和errorDual，但受send的影响主要是trans的实现方式，即利用状态机来判断命令与响应的对应关系，但这样的对应关系是否正确来自上层应用的业务逻辑判断，所以send_Sync在实现时将自定义的getResultDual和errorDual暴露给了上层应用，由其决定在什么情况下接收该包还是一个错误的接收，所以如果使用send_Sync，则每个trans要么调用getResultDual（接收正确），要么调用errorDual。
 *
 *
 *
 * Created by andrew on 16-1-9.
 */
public class comTrans_CmdResponse{

    private getPacket readPacketFunc = null;
    private sendData sendDataFunc = null;

    private jxSparseTable<String,String,check> duals=new jxSparseTable<>();
    private check defaultDual=null;
    private BlockingDeque<waitSend> sendQueue=new LinkedBlockingDeque<>();
    private Thread sendThread=null;

    public void send(ComData data,getResult getResultDual,getErrorMsg errorDual,boolean notWait) throws Exception {
        //jxLog.logger.debug("send:"+ Trans.TransToHexString(data.getPacket()," "));
        waitSend ws=new waitSend();
        ws.data=data;
        ws.getResultDual=getResultDual;
        ws.errorDual=errorDual;
        ws.notWait=notWait;
        sendQueue.offer(ws);
    }
    public void send(ComData data,getResult getResultDual,getErrorMsg errorDual) throws Exception {
        send(data, getResultDual, errorDual, false);
    }

    public class syncResult{
        public boolean OK;
        public Queue<ComData> Result;
        public Object error;
    }
    private Object sync_lock=new Object();
    private syncResult sync_result=null;
    public syncResult send_Sync(ComData data) throws Exception {
        synchronized (sync_lock) {
            sync_result=new syncResult();
            sync_result.OK = false;
            send(data, result -> {
                synchronized (sync_lock) {
                    sync_result.OK=true;
                    sync_result.Result = result;
                    sync_lock.notify();
                }
            },param -> {
                synchronized (sync_lock) {
                    sync_result.error = param;
                    sync_lock.notify();
                }
                return true;
            });
            try {
                sync_lock.wait(3000);
            } catch (InterruptedException e) {
                sync_result.error = "等待响应超时";
                jxLog.error(e);
            }
            return sync_result;
        }
    }

    public comTrans_CmdResponse(sendData sendDataFunc,
                                getPacket readPacketFunc) {
        this.sendDataFunc = sendDataFunc;
        this.readPacketFunc = readPacketFunc;
        sendThread= jxTimer.asyncRun_Repeat(param -> {
            comTrans_CmdResponse ct=(comTrans_CmdResponse)param;
            waitSend ws=ct.sendQueue.take();
            try {
                if(ws.notWait){
                    ct.sendDataFunc.Do(ws.data);
                    return;
                }
                int sendnum=0;
                String s = utils.GetClassName(ws.data.getClass());
                while (sendnum<3) {
                    //如果超时没有响应或响应错误会有三次重发
                    DataInputStream di = ct.sendDataFunc.Do(ws.data);
                    //jxLog.logger.debug("send : " + Trans.TransToHexString(ws.data.getPacket()," "));
                    Queue<ComData> pkgs = ct.readPacketFunc.Do(di);
                    if(pkgs.size()>0) {
                        ComData pkg = pkgs.poll();
                        //此处的处理即默认一问一答（可以有多个响应包，但这些响应包的类型必须相同）
                        check cr = ct.duals.Search(s, utils.GetClassName(pkg.getClass()));
                        if (cr != null) {
                            //jxLog.logger.debug("send : " + s + " receive : " + r);
                            cr.Do(ws.data, pkgs, ws.getResultDual, ws.errorDual);
                            return;
                        } else {
                            if(defaultDual!=null){
                                defaultDual.Do(ws.data, pkgs, ws.getResultDual, ws.errorDual);
                                return;
                            }
                            String em="send : " + s + " receive : " +
                                    utils.GetClassName(pkg.getClass()) + " No DualFunc defined!";
                            jxLog.logger.warn(em);
                            if(ws.errorDual!=null)
                                if(ws.errorDual.Do(em))
                                    return;
                        }
                    }
                    sendnum++;
                    String em="send : " + s + " no correct response, try again!";
                    jxLog.logger.warn(em);
                    if(ws.errorDual!=null)
                        ws.errorDual.Do(em);
                }
                String em="send : " + s + " no correct responese,try 3 times!";
                jxLog.logger.warn(em);
                if(ws.errorDual!=null)
                    ws.errorDual.Do(em);
            }
            catch (Exception e){
                jxLog.error(e);
            }
        },this,true);
    }

    /**
     * 发送cmd后如果得到response则该如何处理
     * @param cmd
     * @param response
     * @param checkResponse
     */
    public void addTrans(Class<?> cmd, Class<?> response,check checkResponse) {
        duals.Add(utils.GetClassName(cmd), utils.GetClassName(response), checkResponse);
    }
    public void addTrans_Default(check checkResponse){
        defaultDual=checkResponse;
    }
    class waitSend{
        ComData data;
        getResult getResultDual;
        getErrorMsg errorDual;
        boolean notWait=false;
    }

    public interface getPacket {
        Queue<ComData> Do(DataInputStream in) throws Exception;
    }
    public interface getResult{
        void Do(Queue<ComData> result) throws Exception;
    }
    public interface getErrorMsg{
        boolean Do(String msg) throws Exception;
    }
    public interface check{
        void Do(ComData data,Queue<ComData> result,getResult getResultDual,
                getErrorMsg errorDual) throws Exception;
    }
    public interface sendData{
        DataInputStream Do(ComData data) throws Exception;
    }
}
