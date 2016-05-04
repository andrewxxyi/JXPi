package cn.ijingxi.communication;

import cn.ijingxi.util.jxLog;

import static cn.ijingxi.communication.pkgTrans_cmdResponse.checkResult;

/**
 * RTU
 *
 * Created by andrew on 16-4-27.
 */
public class modbusTrans {

    private pkgTrans_cmdResponse trans = null;


    //public modbus getResponse() {
    //    return response;
    //}

    /*
    private static jxStateMachine<state, event> dualSM = null;
    private jxStateMachine.realSM sm = null;

    private modbus cmd = null;
    private modbus response = null;

    static {
        dualSM = new jxStateMachine();
        //状态机定义
        dualSM.AddTrans(state.idel, event.send, state.waiting, null);
        //dualSM.AddTrans(state.waiting, event.response_no, state.waiting, null);
        dualSM.AddTrans(state.waiting, event.timeout, state.idel, (p1, p2) -> {
            jxLog.error("modbus等待超时");
        });

        dualSM.AddTrans(state.waiting, event.response_ok, state.idel, (p1, p2) -> {
            jxStateMachine.realSM m = (jxStateMachine.realSM) p1;
            ((modbusTrans) m.getStateObject()).response = (modbus) p2;
        });

        //dualSM.AddTrans(state.process, event.dualover, state.idel, null);

        dualSM.AddTrans(state.waiting, event.error, state.idel, (p1, p2) -> {
            jxStateMachine.realSM sm = (jxStateMachine.realSM) p1;
            jxLog.error("modbus处理出现错误:" + p2);
        });
        //dualSM.AddTrans(state.error, event.dualover, state.idel, null);

    }

    class waitSend {
        ComData data;
        comTrans_CmdResponse.getResult getResultDual;
        comTrans_CmdResponse.getErrorMsg errorDual;
    }


    private BlockingDeque<waitSend> sendQueue = new LinkedBlockingDeque<>();
    private Thread sendThread = null;
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

    */


    public modbusTrans(jxNIOTCPClient client) {
        trans = new pkgTrans_cmdResponse(p -> client.sendData(p));
        //sm = dualSM.newRealSM(this, state.idel);
        //jxLog.logger.debug("ComData_USR_IOT");
    }

    /**
     * 为了避免冲突，此处强制将
     *
     * @param data
     * @param datalen
     * @return
     * @throws Exception
     */
    public modbus send(modbus data, int datalen) throws Exception {

        return (modbus) trans.send_serial(data.getPacket(), (di, getresult) -> {
            modbus m = modbus.read(di, datalen);
            if (m != null)
                if (data.getAddress() == m.getAddress()) {
                    //sm.happen(event.response_ok, null);
                    getresult.Do(m);
                    return checkResult.OK;
                } else
                    return checkResult.Waiting;
            return checkResult.Error;
        }, (et, msg) -> {
            switch (et) {
                case Timeout:
                    jxLog.logger.error("modbus处理出现超时:" + msg);
                    break;
                default:
                    jxLog.logger.error("modbus处理出现错误:" + msg);
                    break;
            }
        });
    }


}
