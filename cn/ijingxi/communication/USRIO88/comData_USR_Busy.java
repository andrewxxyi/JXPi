package cn.ijingxi.communication.USRIO88;

import cn.ijingxi.communication.ComData;
import cn.ijingxi.util.jxLog;

/**
 * Created by andrew on 16-1-9.
 */
public class comData_USR_Busy extends ComData {
    private static final String pkg_name="comData_USR_Busy";
    static {
        try {
            defPackgeFormat(pkg_name,"Tail",0, new byte[]{0x7F, (byte)7F});
        } catch (Exception e) {
            jxLog.error(e);
        }

    }

    public comData_USR_Busy(){
        super(pkg_name);
    }
}
