package cn.ijingxi.common.com;

import cn.ijingxi.common.util.*;

import java.io.DataInputStream;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 命令响应形式，即一发一答
 * 每个comTrans对象代表和一个受控对象的通信控制
 *
 * Created by andrew on 16-1-9.
 */
public class comTrans_CmdResponse{

    private getPacket readPacketFunc = null;
    private sendData sendDataFunc = null;

    private jxSparseTable<String,String,check> duals=new jxSparseTable<>();
    private BlockingDeque<wangSend> sendQueue=new LinkedBlockingDeque<>();
    private Thread sendThread=null;

    public void send(ComData data,getResult getResultDual,IDo errorDual) throws Exception {
        //jxLog.logger.debug("send:"+ Trans.TransToHexString(data.getPacket()," "));
        wangSend ws=new wangSend();
        ws.data=data;
        ws.getResultDual=getResultDual;
        ws.errorDual=errorDual;
        sendQueue.offer(ws);
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
            wangSend ws=ct.sendQueue.take();
            try {
                int sendnum=0;
                String s = utils.GetClassName(ws.data.getClass());
                while (sendnum<3) {
                    DataInputStream di = ct.sendDataFunc.Do(ws.data);
                    //jxLog.logger.debug("send : " + Trans.TransToHexString(ws.data.getPacket()," "));
                    Queue<ComData> pkgs = ct.readPacketFunc.Do(di);
                    if(pkgs.size()>0){
                        ComData pkg=pkgs.poll();
                        check cr = ct.duals.Search(s, utils.GetClassName(pkg.getClass()));
                        if (cr != null) {
                            //jxLog.logger.debug("send : " + s + " receive : " + r);
                            cr.Do(ws.data, pkgs, ws.getResultDual, ws.errorDual);
                            return;
                        } else
                            jxLog.logger.debug("send : " + s + " receive : " +
                                    utils.GetClassName(pkg.getClass()) + " No DualFunc defined!");
                    }
                    sendnum++;
                }
                jxLog.logger.debug("send : " + s + " get error responese,try 3 times!");
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

    class wangSend{
        ComData data;
        getResult getResultDual;
        IDo errorDual;
    }

    public interface getPacket {
        Queue<ComData> Do(DataInputStream in) throws Exception;
    }
    public interface getResult{
        void Do(Queue<ComData> result) throws Exception;
    }
    public interface check{
        void Do(ComData data,Queue<ComData> result,getResult getResultDual,
                IDo errorDual) throws Exception;
    }
    public interface sendData{
        DataInputStream Do(ComData data) throws Exception;
    }
}
