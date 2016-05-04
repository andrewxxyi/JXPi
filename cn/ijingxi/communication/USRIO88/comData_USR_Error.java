package cn.ijingxi.communication.USRIO88;

import cn.ijingxi.communication.ComData;
import cn.ijingxi.util.jxLog;

/**
 * Created by andrew on 16-1-10.
 */
public class comData_USR_Error  extends ComData {
    private static final String pkg_name="comData_USR_Busy";
    static {
        try {
            defPackgeFormat(pkg_name,"Tail",0, new byte[]{0x00, (byte)00});
        } catch (Exception e) {
            jxLog.error(e);
        }

    }

    public comData_USR_Error(){
        super(pkg_name);
    }
}