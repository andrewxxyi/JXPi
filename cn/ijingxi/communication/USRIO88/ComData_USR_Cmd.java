package cn.ijingxi.communication.USRIO88;

import cn.ijingxi.communication.ComData;
import cn.ijingxi.util.Trans;
import cn.ijingxi.util.jxLog;

/**
 * 使用方法：
 * 1、new ComData_USR_Cmd()
 * 2、setCmd
 * 3、setParam
 * 4、getPacket
 *
 * Created by andrew on 15-11-23.
 */
public class ComData_USR_Cmd extends ComData {
    private static final String pkg_name="ComData_USR_Cmd";
    static {
        try {
            //utils.P("ComData_USR_IOT","static init");
            defPackgeFormat(pkg_name,"Head",2, new byte[]{0x55, (byte) 0xaa});
            defPackgeFormat(pkg_name,"Length",2, null);
            //保留，不要设置
            defPackgeFormat(pkg_name,"ID",1, null);
            defPackgeFormat(pkg_name,"Cmd",1, null);
            defPackgeFormat(pkg_name,"Param",0, null);
            defPackgeFormat(pkg_name,"CRC",1, null);
            setLengthFiled(pkg_name,"Length");
        } catch (Exception e) {
            jxLog.error(e);
        }
    }

    public ComData_USR_Cmd(){
        super(pkg_name);
        //jxLog.logger.debug("ComData_USR_IOT");
    }
    public ComData_USR_Cmd(byte[] data,int start) throws Exception {
        super(pkg_name,data,start);
        //jxLog.logger.debug("ComData_USR_IOT");
    }

    public void setCmd(byte cmd){
        try {
            setValue("Cmd",new byte[]{cmd});
        } catch (Exception e) {
            jxLog.error(e);
        }
    }
    public byte[] getCmd(){
        try {
            return getValue("Cmd");
        } catch (Exception e) {
            jxLog.error(e);
            return null;
        }
    }
    public void setParam(byte[] param){
        try {
            setValue("Param",param);
        } catch (Exception e) {
            jxLog.error(e);
        }
    }
    public byte[] getParam(){
        try {
            return getValue("Param");
        } catch (Exception e) {
            jxLog.error(e);
            return null;
        }
    }

    @Override
    protected byte[] computeCRC() throws Exception {
        int rs=0;
        byte[] v=getValue("Length");
        for(int i=0;i<v.length;i++)
            rs += v[i];
        v=getValue("ID");
        for(int i=0;i<v.length;i++)
            rs += v[i];
        v=getValue("Cmd");
        for(int i=0;i<v.length;i++)
            rs += v[i];
        v=getValue("Param");
        if(v!=null)
            for(int i=0;i<v.length;i++)
                rs += v[i];
        v= Trans.TransToByteArray(rs);

        byte[] bs=new byte[1];
        System.arraycopy(v, 3, bs, 0, 1);
        return bs;
    }


    @Override
    public int getPkgLength() throws Exception {
        //jxLog.logger.debug("call getPkgLength");
        //utils.P("getPkgLength","ComData_USR_IOT");
        try {
            byte[] p = getValue("Length");
            int len=Trans.TransToShort(p,0);
            return len+5;
        } catch (Exception e) {
            jxLog.error(e);
        }
        return 0;
    }

    @Override
    protected void setLength(){
        //utils.P("setLength","ComData_USR_IOT");
        byte[] p=null;
        try {
            p = getValue("Param");
        } catch (Exception e) {
            jxLog.error(e);
        }
        Short len=0;
        if(p==null)
            len=2;
        else
            len= (short) (p.length+2);
        try {
            setValue("Length", Trans.TransToByteArray(len));
        } catch (Exception e) {
            jxLog.error(e);
        }
    }

    @Override
    public boolean check(ComData cmdpkg){
        try {
            if(checkCRC()){
                byte[] arr=getValue("Head");
                if(arr[0]==0xaa&&arr[1]==0x55){
                    arr=getValue("Cmd");
                    if(arr[0]==cmdpkg.getValue("Cmd")[0]+0x80)
                        return true;
                }
            }
            return false;
        } catch (Exception e) {
            jxLog.error(e);
            return false;
        }
    }

    public void readFromInputStream(byte[] data,int bufstart) throws Exception {
        byte[] buf=null;
        PackgeFormat pf=DataFormatter.get(pkgName);
        if(pf.lengthFiled!=null){
            int start=bufstart+getLengthStart();
            //jxLog.logger.debug("start:"+start);
            int len=getFiledDefaultLength(pf.lengthFiled);
            //jxLog.logger.debug("len:"+len);
            int rl=start+len;
            //jxLog.logger.debug("rl:"+rl);
            //ins.read(data,bufstart+2,rl-2);

            byte[] lb=new byte[len];
            System.arraycopy(data,start,lb,0,len);
            setValue(pf.lengthFiled,lb);
            int tl=getPkgLength();
            //jxLog.logger.debug("tl:"+tl);

            //ins.read(data,rl,tl-rl);
            buf=new byte[tl];
            System.arraycopy(data,bufstart,buf,0,tl);
        }
        else{
            buf=new byte[pf.packgeTotalLength];
            //ins.read(data,bufstart+2,pf.packgeTotalLength-2);
            System.arraycopy(data,bufstart,buf,0,pf.packgeTotalLength);
        }
        //jxLog.logger.debug("input:"+ Trans.TransToHexString(buf," "));
        init(buf,0);
    }
}
