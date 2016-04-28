package cn.ijingxi.com.USRIO88;

import cn.ijingxi.communication.ComData;
import cn.ijingxi.communication.comTrans_CmdResponse;
import cn.ijingxi.communication.jxNIOTCPClient;
import cn.ijingxi.util.Trans;
import cn.ijingxi.util.jxLog;
import cn.ijingxi.util.utils;

import java.io.DataInputStream;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by andrew on 16-1-9.
 */
public class comTrans_USR {

    private jxNIOTCPClient client=null;

    private comTrans_CmdResponse trans=null;

    public comTrans_USR(jxNIOTCPClient client) throws Exception {
        utils.Check(client == null, "必须预习设置好和USR通信的TCP客户端");
        this.client = client;

        trans = new comTrans_CmdResponse(
                data -> client.sendData(data.getPacket()),
                in -> getComData(in)
        );

        //发送init
        trans.addTrans(comData_USR_Init.class,
                comData_USR_InitResponse.class,
                (data, result, getResultDual, errorDual) -> {
                    //得到正确响应
                    for (ComData com : result) {
                        comData_USR_InitResponse pkg = (comData_USR_InitResponse) com;
                        if (pkg.getType() == comData_USR_InitResponse.InitResponse.OK) {
                            if (getResultDual != null)
                                //初始化正确
                                getResultDual.Do(result);
                        } else {
                            if (errorDual != null)
                                errorDual.Do("设备未能正确初始化");
                            jxLog.logger.warn("comData_USR_Init:未得到正确响应");
                        }
                    }
                });
        trans.addTrans(comData_USR_Init.class,
                comData_USR_Busy.class,
                (data, result, getResultDual, errorDual) -> {
                    if (errorDual != null)
                        errorDual.Do("设备忙");
                });

        //发送命令
        trans.addTrans(ComData_USR_Cmd.class,
                ComData_USR_Response.class,
                (data, result, getResultDual, errorDual) -> {
                    //发送命令得到响应，则向发送方返回响应
                    if (getResultDual != null)
                        getResultDual.Do(result);
                });
        trans.addTrans(ComData_USR_Cmd.class,
                comData_USR_Busy.class,
                (data, result, getResultDual, errorDual) -> {
                    if (errorDual != null)
                        errorDual.Do("设备忙");
                });
        trans.addTrans(ComData_USR_Cmd.class,
                comData_USR_InitResponse.class,
                (data, result, getResultDual, errorDual) -> {
                    //命令发送失败，则先发送初始化命令，然后重传该命令
                    trans.send(new comData_USR_Init(),
                            result1 -> trans.send(data, getResultDual, errorDual),
                            param -> {
                                if (errorDual != null)
                                    errorDual.Do("设备未能正确初始化");
                                jxLog.logger.warn("comData_USR_Init:未得到正确响应");
                                return true;
                            });
                });
        trans.addTrans(ComData_USR_Cmd.class,
                comData_USR_Error.class,
                (data, result, getResultDual, errorDual) -> {
                    //命令发送失败，则先发送初始化命令，然后重传该命令
                    trans.send(new comData_USR_Init(),
                            result1 -> trans.send(data, getResultDual, errorDual),
                            param -> {
                                if (errorDual != null)
                                    errorDual.Do("设备未能正确初始化");
                                jxLog.logger.warn("comData_USR_Init:未得到正确响应");
                                return true;
                            });
                });
        trans.addTrans_Default(
                (data, result, getResultDual, errorDual) -> {
                    //发送命令得到了未定义的响应
                    if (errorDual != null)
                        errorDual.Do("命令得到了未定义的响应");
                    jxLog.logger.warn("comTrans_USR发送包:未得到正确响应");
                });
        trans.send(new comData_USR_Init(),
                null,
                param -> {
                    jxLog.logger.warn("设备未能正确初始化");
                    return true;
                });
    }

    public void close(){
        client.close();
    }
    /**
     * 发送一条命令，有可能返回多条响应，目前默认返回的包格式一致
     * @param cmd
     * @return
     * @throws Exception
     */
    public comTrans_CmdResponse.syncResult send(ComData cmd) throws Exception {
        return trans.send_Sync(cmd);
    }

    public static Queue<ComData> getComData(DataInputStream ins) throws Exception {
        //jxLog.logger.debug("start:getComData");
        Queue<ComData> rs=new LinkedList<>();
        byte[] data = new byte[ComData.packetBufSize];
        int count=ins.read(data);
        //jxLog.logger.debug("count:"+count);
        //jxLog.logger.debug("data : " + Trans.TransToHexString(data," "));
        int start=0;
        while (start<count){
            ComData pkg=null;
            if (data[start] == 0x00 && data[start+1] == 0x00)
                pkg=new comData_USR_Error();
            else if (data[start] == 0x7f && data[start+1] == 0x7f)
                pkg=new comData_USR_Busy();
            else if(comData_USR_InitResponse.check(data)!=comData_USR_InitResponse.InitResponse.None)
                pkg=new comData_USR_InitResponse(data,start);
            else {
                ComData_USR_Cmd cmd = null;
                if (data[start] == (byte) 0x55 && data[start+1] ==(byte) 0xaa)
                    cmd = new ComData_USR_Cmd();
                else if (data[start] == (byte)0xaa && data[start+1] ==(byte) 0x55)
                    cmd = new ComData_USR_Response();
                else {
                    jxLog.logger.warn("无法解析的错误包数据！！");
                    jxLog.logger.debug("data : " + Trans.TransToHexString(data, start, 20, " "));
                    break;
                }
                cmd.readFromInputStream(data,start);
                pkg=cmd;
            }
            start+=pkg.getPkgLength();
            //jxLog.logger.debug("start:"+start);
            rs.offer(pkg);
        }
        return rs;
    }

}
