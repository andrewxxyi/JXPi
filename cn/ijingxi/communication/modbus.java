package cn.ijingxi.communication;

import cn.ijingxi.util.jxLog;
import cn.ijingxi.util.utils;

import java.io.DataInputStream;

/**
 * RTU
 *
 * Created by andrew on 16-4-27.
 */
public class modbus extends ComData {
    private static final String pkg_name="modbus";

    static {


        try {
            //utils.P("ComData_USR_IOT","static init");
            defPackgeFormat(pkg_name, "Address", 1, new byte[]{0x0});
            defPackgeFormat(pkg_name, "Code", 1, new byte[]{0x0});
            //保留，不要设置
            defPackgeFormat(pkg_name, "Data", 0, null);
            defPackgeFormat(pkg_name, "CRC", 2, null);
        } catch (Exception e) {
            jxLog.error(e);
        }
    }



    @Override
    protected byte[] computeCRC() throws Exception {
        //utils.P("computeCRC","ComData_USR_IOT");
        byte[] crc = new byte[]{(byte) 0xff, (byte) 0xff};
        int len = getPkgLength() - 2;
        for (int i = 0; i < len; i++) {
            byte b = getValue(i);
            crc[0] ^= b;
            for (int j = 0; j < 8; j++) {
                boolean bs = utils.checkLSB(crc[0]);
                crc = utils.rightShift_unsigned(crc);
                if (bs) {
                    crc[0] ^= 1;
                    crc[1] ^= 0xa0;
                }
            }
        }
        return crc;
    }
    public modbus(String pkg_name){
        super(pkg_name);
        //jxLog.logger.debug("ComData_USR_IOT");
    }
    public modbus(String pkg_name,byte[] data,int start) throws Exception {
        super(pkg_name,data,start);
        //jxLog.logger.debug("ComData_USR_IOT");
    }
    public void setAddress(byte Address){
        try {
            setValue("Address",new byte[]{Address});
        } catch (Exception e) {
            jxLog.error(e);
        }
    }
    public byte getAddress(){
        try {
            return getValue("Address")[0];
        } catch (Exception e) {
            jxLog.error(e);
            return 0;
        }
    }
    public void setCode(byte Code){
        try {
            setValue("Code",new byte[]{Code});
        } catch (Exception e) {
            jxLog.error(e);
        }
    }
    public byte getCode(){
        try {
            return getValue("Code")[0];
        } catch (Exception e) {
            jxLog.error(e);
            return 0;
        }
    }
    public void setData(byte[] data){
        try {
            setValue("Data",data);
        } catch (Exception e) {
            jxLog.error(e);
        }
    }
    public byte[] getData(){
        try {
            return getValue("Data");
        } catch (Exception e) {
            jxLog.error(e);
            return null;
        }
    }

    public static modbus read(DataInputStream di, int datalen) throws Exception {
        modbus m = new modbus(pkg_name);
        byte[] data = new byte[packetBufSize];
        int count = di.read(data);
        utils.Check(count < datalen + 4, String.format("读入的包数据(%d)小于指定长度(%d)", count, datalen + 4));

        byte[] buf = new byte[1];
        System.arraycopy(data, 0, buf, 0, 1);
        m.setAddress(buf[0]);

        buf = new byte[1];
        System.arraycopy(data, 1, buf, 0, 1);
        m.setCode(buf[0]);

        buf = new byte[datalen];
        System.arraycopy(data, 2, buf, 0, datalen);
        m.setData(buf);

        buf = new byte[2];
        System.arraycopy(data, datalen + 2, buf, 0, 2);
        m.setValue("CRC", buf);

        if (m.checkCRC())
            return m;
        return null;
    }
}
