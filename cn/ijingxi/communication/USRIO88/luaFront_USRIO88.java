package cn.ijingxi.com.USRIO88;

import cn.ijingxi.communication.ComData;
import cn.ijingxi.communication.comTrans_CmdResponse;
import cn.ijingxi.communication.jxNIOTCPClient;
import cn.ijingxi.intelControl.FrontCommunication;
import cn.ijingxi.intelControl.jxLua;
import cn.ijingxi.util.*;

/**
 * Created by andrew on 16-3-5.
 */
public class luaFront_USRIO88 extends FrontCommunication {

    private comTrans_USR trans=null;
    private Thread readInputThread=null;

    private jxStateMachine.realSM[] sms=null;

    public luaFront_USRIO88(String devName,String devType,int ver,
                            jxNIOTCPClient client,boolean startRead) throws Exception {
        super(devName, devType);
        this.ver = ver;
        trans = new comTrans_USR(client);
        if (startRead)
            startReadInput();
    }

    @Override
    public Object send(Object data) throws Exception {
        ComData cd=(ComData)data;
        return trans.send(cd);
    }

    @Override
    public void close() {
        if (readInputThread != null)
            readInputThread.interrupt();
        readInputThread = null;
        trans.close();
    }


    private static jxStateMachine<InputState,InputEvent> inputDual=null;
    static {
        inputDual=new jxStateMachine<>();

        inputDual.AddTrans(InputState.Opened,InputEvent.Close,InputState.Closed, (p1,p2) -> {
            Integer channel = (Integer) p2;
            switch (channel){
                case 0:
                    jxLog.debug("Channel 1:closed");
                    FrontUSR.call("setChannel","USR1","Close",1,null,0,0);
                    break;
                case 1:
                    jxLog.debug("Channel 2:closed");
                    FrontUSR.call("setChannel","USR1","Open",1,null,0,0);
                    break;
                case 2:
                    jxLog.debug("Channel 3:closed");
                    FrontUSR.call("setChannel","USR1","Close",2,null,0,0);
                    break;
                case 3:
                    jxLog.debug("Channel 4:closed");
                    FrontUSR.call("setChannel","USR1","Open",2,null,0,0);
                    break;
                case 4:
                    jxLog.debug("Channel 5:closed");
                    FrontUSR.call("setChannel","USR1","Close",3,null,0,0);
                    break;
                case 5:
                    jxLog.debug("Channel 6:closed");
                    FrontUSR.call("setChannel","USR1","Open",3,null,0,0);
                    break;

            }
        });
        inputDual.AddTrans(InputState.Closed,InputEvent.Open,InputState.Opened,null);
    }

    public void startReadInput() {

        sms = new jxStateMachine.realSM[8];
        for (int i = 0; i < 8; i++)
            sms[i] = inputDual.newRealSM(this, InputState.Opened);

        ComData_USR_Cmd com = new ComData_USR_Cmd();
        com.setCmd((byte) 0x14);

        readInputThread = new Thread(() -> {

            jxTimer.DoPeriod(2, param -> {
                //jxLog.debug("read input,send:"+Trans.TransToHexString(com.getPacket()," "));
                comTrans_CmdResponse.syncResult srs = trans.send(com);
                if (srs.OK) {
                    for (ComData pkg : srs.Result) {
                        if (pkg instanceof ComData_USR_Response) {
                            byte[] cmd = ((ComData_USR_Response) pkg).getCmd();
                            byte[] p = ((ComData_USR_Response) pkg).getParam();
                            //jxLog.debug("receive:"+ Trans.TransToHexString(pkg.getPacket()," "));
                            if (cmd[0] == (byte) 0x94) {
                                //jxLog.debug("receive response");
                                int num = 1;
                                for (int i = 0; i <= 7; i++) {
                                    //jxLog.logger.debug("num "+(byte)num);
                                    if ((p[0] & (byte) num) == 0)
                                        sms[i].happen(InputEvent.Open,i);
                                    else
                                        sms[i].happen(InputEvent.Close,i);
                                    num = num << 1;
                                }
                            }
                        }
                    }
                } else
                    jxLog.debug((String) srs.error);
            }, null);

        });
        readInputThread.start();
    }

    /**
     * 需要lua脚本设置相应的状态机：
     *  luaStateMachine
     *      1、updown
     *      2、setInitState
     */
    public void startReadInput_luaSM() {

        ComData_USR_Cmd com = new ComData_USR_Cmd();
        com.setCmd((byte) 0x14);

        readInputThread = new Thread(() -> {
            jxTimer.DoPeriod(2, param -> {
                //jxLog.debug("read input,send:"+Trans.TransToHexString(com.getPacket()," "));
                comTrans_CmdResponse.syncResult srs = trans.send(com);
                if (srs.OK) {
                    for (ComData pkg : srs.Result) {
                        if (pkg instanceof ComData_USR_Response) {
                            byte[] cmd = ((ComData_USR_Response) pkg).getCmd();
                            byte[] p = ((ComData_USR_Response) pkg).getParam();
                            //jxLog.debug("receive:"+ Trans.TransToHexString(pkg.getPacket()," "));
                            if (cmd[0] == (byte) 0x94) {
                                //jxLog.debug("receive response");
                                int num = 1;
                                for (int i = 0; i <= 7; i++) {
                                    jxLua.informPinData_PeriodRead_SM(this.devName, i, (p[0] & (byte) num));
                                    num = num << 1;
                                }
                            }
                        }
                    }
                } else
                    jxLog.debug((String) srs.error);
            }, null);

        });
        readInputThread.start();
    }



    enum InputState{Opened,Closed}
    enum InputEvent{Open,Close}




}
