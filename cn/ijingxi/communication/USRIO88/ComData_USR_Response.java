package cn.ijingxi.com.USRIO88;

/**
 * Created by andrew on 16-1-9.
 */
public class ComData_USR_Response extends ComData_USR_Cmd {
    private static final String pkg_name_Response="ComData_USR_Response";
    static {
        try {
            //utils.P("ComData_USR_IOT","static init");
            defPackgeFormat(pkg_name_Response,"Head",2, new byte[]{(byte)0xaa, (byte)0x55});
            defPackgeFormat(pkg_name_Response,"Length",2, null);
            //保留，不要设置
            defPackgeFormat(pkg_name_Response,"ID",1, null);
            defPackgeFormat(pkg_name_Response,"Cmd",1, null);
            defPackgeFormat(pkg_name_Response,"Param",0, null);
            defPackgeFormat(pkg_name_Response,"CRC",1, null);
            setLengthFiled(pkg_name_Response,"Length");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ComData_USR_Response(){
        this.pkgName=pkg_name_Response;
    }
    public ComData_USR_Response(byte[] data,int start) throws Exception {
        this();
        init(data,start);
    }


}
