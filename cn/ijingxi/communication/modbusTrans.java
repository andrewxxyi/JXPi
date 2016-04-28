package cn.ijingxi.communication;

import cn.ijingxi.util.*;

import java.io.DataInputStream;

/**
 * RTU
 *
 * Created by andrew on 16-4-27.
 */
public class modbusTrans {

    private jxNIOTCPClient client = null;

    private static jxStateMachine<state, event> dualSM = null;

    private modbus cmd = null;
    private modbus response = null;

    public modbus getResponse() {
        return response;
    }

    private jxStateMachine.realSM sm = null;

    static {
        dualSM = new jxStateMachine();
        //状态机定义
        dualSM.AddTrans(state.idel, event.send, state.waiting, null);
        dualSM.AddTrans(state.waiting, event.response_no, state.waiting, null);
        dualSM.AddTrans(state.waiting, event.timeout, state.error, (p1, p2) -> {
            jxStateMachine.realSM sm = (jxStateMachine.realSM) p1;
            jxLog.error("modbus等待超时");
            sm.happen(event.dualover, null);
        });

        dualSM.AddTrans(state.waiting, event.response_ok, state.process, (p1, p2) -> {
            jxStateMachine.realSM m = (jxStateMachine.realSM) p1;
            ((modbusTrans) m.getStateObject()).response = (modbus) p2;
        });

        dualSM.AddTrans(state.process, event.dualover, state.idel, null);

        dualSM.AddTrans(state.process, event.error, state.error, (p1, p2) -> {
            jxStateMachine.realSM sm = (jxStateMachine.realSM) p1;
            jxLog.error("modbus处理出现错误:" + p2);
            sm.happen(event.dualover, null);
        });
        dualSM.AddTrans(state.error, event.dualover, state.idel, null);

    }

    enum state {
        idel,
        waiting,
        process,
        error
    }

    enum event {
        send,
        response_no,
        response_ok,
        timeout,
        error,
        dualover
    }

    public modbusTrans(jxNIOTCPClient client) {
        this.client = client;
        sm = dualSM.newRealSM(this, state.idel);
        //jxLog.logger.debug("ComData_USR_IOT");
    }


    public modbus send(modbus data, int datalen) throws Exception {
        synchronized (this) {
            response = null;
            cmd = data;
            sm.happen(event.send, null);
            DataInputStream di = client.sendData(cmd.getPacket());
            recevice(di,datalen);
        }
        return response;
    }

    private void recevice(DataInputStream di, int datalen) throws Exception {
        try {
            jxTimer.asyncRun(p -> {
                modbusTrans trans = ((modbusTrans) p);
                modbus m = modbus.read(di, datalen);
                if (m != null)
                    synchronized (trans) {
                        trans.response = m;
                        trans.notify();
                    }
            }, this);
            this.wait(3000);
            if (response != null) {
                if (cmd.getAddress() == response.getAddress())
                    sm.happen(event.response_ok, null);
                else {
                    sm.happen(event.response_no, null);
                    //继续接收
                    recevice(di,datalen);
                }
            } else
                sm.happen(event.timeout, null);
        } catch (InterruptedException e) {
            sm.happen(event.timeout, null);
        }
    }


}
