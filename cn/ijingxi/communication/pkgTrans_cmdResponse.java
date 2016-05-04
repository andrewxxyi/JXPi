package cn.ijingxi.communication;

import cn.ijingxi.util.IDo;
import cn.ijingxi.util.jxLog;
import cn.ijingxi.util.jxTimer;
import cn.ijingxi.util.utils;

import java.io.DataInputStream;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 命令响应形式，即一发一答 *
 *
 * Created by andrew on 16-4-29.
 */
public class pkgTrans_cmdResponse {

    private BlockingDeque<waitSend> sendQueue=new LinkedBlockingDeque<>();
    private Thread sendThread=null;
    private Object sync_lock=new Object();
    private Object sync_result=null;

    enum checkResult{
        OK,
        Error,
        Waiting,
        Again
    }
    enum sendError{
        Timeout,
        SendError
    }

    /**
     * 发送后不管
     * @param data
     * @return
     * @throws Exception
     */
    public void send(byte[] data) throws Exception {
        waitSend ws = new waitSend();
        ws.data = data;
        sendQueue.offer(ws);
    }

    /**
     * 异步调用，返回值在checkDual中即可获得
     * @param data
     * @param checkDual
     * @param errorDual
     * @throws Exception
     */
    public void send(byte[] data,check checkDual,getErrorMsg errorDual) throws Exception {
        utils.Check(checkDual == null, "接收包检查函数不能为空");
        //jxLog.logger.debug("send:"+ Trans.TransToHexString(data.getPacket()," "));
        waitSend ws = new waitSend();
        ws.directGet = true;
        ws.data = data;
        ws.checkDual = checkDual;
        ws.errorDual = errorDual;
        sendQueue.offer(ws);
    }
    /**
     * 串行化发送：即一发一收，当前包处理完毕后一个包才能继续
     * @param data
     * @param checkDual
     * @param errorDual
     * @return
     * @throws Exception
     */
    public Object send_serial(byte[] data,check checkDual,getErrorMsg errorDual) throws Exception {
        utils.Check(checkDual==null,"接收包检查函数不能为空");
        synchronized (this) {
            try {
                synchronized (sync_lock) {
                    sync_result = null;
                    //jxLog.logger.debug("send:"+ Trans.TransToHexString(data.getPacket()," "));
                    waitSend ws = new waitSend();
                    ws.data = data;
                    ws.checkDual = checkDual;
                    ws.errorDual = errorDual;
                    sendQueue.offer(ws);
                    sync_lock.wait(3000);
                }
            } catch (InterruptedException e) {
                jxLog.error(e);
                if (errorDual != null)
                    errorDual.Do(sendError.Timeout, "发送超时");
                return null;
            }
            return sync_result;
        }
    }

    void senddata(waitSend ws,sendData sendDataFunc,byte[] data) throws Exception {
        DataInputStream di = sendDataFunc.Do(ws.data);
        if (ws.directGet) {
            ws.checkDual.Do(di, null);
            return;
        }
        checkResult cr = ws.checkDual.Do(di, p -> sync_result = p);
        switch (cr) {
            case OK:
                synchronized (sync_lock) {
                    sync_lock.notify();
                }
                break;
            case Again:
                senddata(ws, sendDataFunc, data);
                break;
            case Waiting:
                break;
            default:
                ws.errorDual.Do(sendError.SendError, "出现错误");
                break;
        }
    }
    public pkgTrans_cmdResponse(sendData sendDataFunc) {
        sendThread = jxTimer.asyncRun_Repeat(param -> {
            pkgTrans_cmdResponse ct = (pkgTrans_cmdResponse) param;
            waitSend ws = ct.sendQueue.take();
            try {
                if (ws.checkDual == null)
                    sendDataFunc.Do(ws.data);
                else
                    senddata(ws, sendDataFunc, ws.data);
            } catch (Exception e) {
                jxLog.error(e);
            }
        }, this, true);
    }

    class waitSend{
        //用户是否在checkDual中直接获取结果
        boolean directGet=false;
        byte[] data;
        check checkDual;
        getErrorMsg errorDual;
    }

    public interface getErrorMsg{
        void Do(sendError errorType,String msg) throws Exception;
    }
    public interface check{
        checkResult Do(DataInputStream result, IDo getResult) throws Exception;
    }
    public interface sendData{
        DataInputStream Do(byte[] data) throws Exception;
    }
}
