package cn.ijingxi.com.USRIO88;

import cn.ijingxi.communication.ComData;
import cn.ijingxi.util.jxLog;

/**
 * Created by andrew on 16-1-9.
 */
public class comData_USR_InitResponse extends ComData {

    public enum InitResponse {None, OK, NO}

    private static final String pkg_name = "USR_IO88_InitResponse";

    static {
        try {
            jxLog.logger.debug("static init");
            defPackgeFormat(pkg_name, "Tail", 0, new byte[]{0x4F, (byte) 0x4B});
        } catch (Exception e) {
            jxLog.error(e);
        }

    }

    public comData_USR_InitResponse() {
        super(pkg_name);
    }

    public comData_USR_InitResponse(byte[] data, int start) throws Exception {
        super(pkg_name, data, start);
    }

    public InitResponse getType() throws Exception {
        byte[] data = getValue("Tail");
        return check(data);
    }

    public static InitResponse check(byte[] data) {
        if (data != null && data.length >= 2)
            if (data[0] == (byte) 0x4F && data[1] == (byte) 0x4B)
                return InitResponse.OK;
            else if (data[0] == (byte) 0x4E && data[1] == (byte) 0x4F)
                return InitResponse.NO;
        return InitResponse.None;
    }

}
