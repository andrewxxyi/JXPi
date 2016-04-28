package cn.ijingxi.com.USRIO88;

import cn.ijingxi.communication.ComData;
import cn.ijingxi.util.jxLog;

/**
 * Created by andrew on 15-12-13.
 */
public class comData_USR_Init extends ComData {
    private static final String pkg_name="USR_IO88_Init";
    static {
        try {
            jxLog.logger.debug("static init");
            defPackgeFormat(pkg_name,"Passwd",0, null);
            defPackgeFormat(pkg_name,"Tail",0, new byte[]{0x0D, (byte) 0x0A});
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setPasswd(String passwd){
        try {
            setValue("Passwd",passwd.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public comData_USR_Init(){
        super(pkg_name);
        setPasswd("admin");
    }
}
