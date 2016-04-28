package cn.ijingxi.communication;

import cn.ijingxi.util.Trans;
import cn.ijingxi.util.jxLog;
import cn.ijingxi.util.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 通讯包格式，主要用于类似modbus之类的控制包的格式设置，目前只支持网络字节序
 * 不同协议的包直接继承ComData，使用：
 * 1、继承ComData
 * 2、在静态初始化函数中调用defPackgeFormat，对该包名的各字段进行定义
 * 3、重载computeCRC、computeLength、checkCRC
 * 4、调用setValue对需要设置的字段赋值
 * 5、调用getPacket获取生成的
 * 6、新建一个byte[] pkg的构造函数，对接收到的包进行解包，再调用checkCRC进行校验，然后就可以调用getValue获取值
 *
 *
 * Created by andrew on 15-11-23.
 */
public class ComData {
    private static final int packetDefaultBufSize=1024;
    //可以根据自己的实际情况来重新定义该属性
    public static int packetBufSize=packetDefaultBufSize;

    protected String pkgName=null;
    public ComData(String pkgName){
        this.pkgName=pkgName;
        //jxLog.logger.debug("ComData");
    }

    /**
     * 本构造函数只能处理最多一个变长字段的包
     *
     * 由于存在变长字段，而由于com通讯比较古老，所以一般都没有采用TLV格式，所以无法在不了解具体包格式的情况下进行统一处理，
     * 所以如果存在两个以上的变长字段则只能由各自的包定义类自行解析
     * @param pkg
     * @throws Exception
     */
    public ComData(String pkgName,byte[] pkg,int start) throws Exception {
        this(pkgName);
        init(pkg,start);
    }
    protected void init(byte[] pkg,int bufstart) throws Exception {
        //变长字段数
        int vlen=0;
        int sTlen=0;
        PackgeFormat pf=DataFormatter.get(pkgName);
        for(int i=1;i<=pf.itemArr.size();i++){
            String itemname=pf.itemArr.get(i);
            DataItem item=pf.items.get(itemname);
            if (item.byteCount==0){
                //变长字段
                vlen++;
                if(vlen>1)
                    //存在两个以上的变长字段，无法处理
                    throw new Exception("存在两个以上的变长字段，无法处理");
            }
            else
                sTlen+=item.byteCount;
        }
        if(sTlen>pkg.length){
            jxLog.logger.error("pkg:"+Trans.TransToHexString(pkg," "));
            throw new Exception("格式错误：送入的包长短于固定字段总长");
        }
        int vTlen=pkg.length-sTlen;
        int start=bufstart;
        for(int i=1;i<=pf.itemArr.size();i++){
            String itemname=pf.itemArr.get(i);
            DataItem item=pf.items.get(itemname);
            if (item.byteCount==0){
                byte[] arr=new byte[vTlen];
                System.arraycopy(pkg,start,arr,0,vTlen);
                setValue(itemname,arr);
                start+=vTlen;
            }
            else{
                byte[] arr=new byte[item.byteCount];
                System.arraycopy(pkg,start,arr,0,item.byteCount);
                setValue(itemname,arr);
                start+=item.byteCount;
            }
        }
    }

    public int getLengthStart() throws Exception {
        int len=0;
        PackgeFormat pf=DataFormatter.get(pkgName);
        if(pf.lengthFiled==null)return -1;
        for(int i=1;i<=pf.itemArr.size();i++){
            String itemname=pf.itemArr.get(i);
            if(itemname.compareTo(pf.lengthFiled)==0)
                return len;
            else{
                //如果有变长字段，则一定在length字段之后
                DataItem item=pf.items.get(itemname);
                utils.Check(item.byteCount==0,"变长字段在长度字段之前："+itemname);
                len+=item.byteCount;
            }
        }
        //设置了长度字段，但没有定义该字段
        return -1;
    }
    public int getFiledDefaultLength(String itemName){
        PackgeFormat pf=DataFormatter.get(pkgName);
        DataItem item=pf.items.get(itemName);
        if(item!=null)
            return item.byteCount;
        return -1;
    }
    protected static Map<String,PackgeFormat> DataFormatter=new HashMap<>();

    /**
     * 必须按顺序对包中各字段依次添加
     * @param pkgName 包名
     * @param itemName 字段名
     * @param byteCount 0为变长字段，如果大于0则是固定字段，固定字段要么为null，要么长度等于byteCount
     * @param defaultValue
     */
    public static void defPackgeFormat(String pkgName,String itemName,int byteCount,byte[] defaultValue) throws Exception {
        PackgeFormat pf=DataFormatter.get(pkgName);
        if(pf==null){
            pf=new PackgeFormat();
            DataFormatter.put(pkgName,pf);
        }
        pf.packgeTotalLength+=byteCount;
        DataItem dt=pf.items.get(itemName);
        if(dt==null){
            if(byteCount>0){
                //固定长度字段
                utils.Check(defaultValue!=null&&defaultValue.length!=byteCount,
                        "固定长度字段如果指定缺省值则其长度应等于byteCount");
            }
            dt=new DataItem();
            dt.itemName=itemName;
            dt.byteCount=byteCount;
            dt.defaultValue=defaultValue;
            pf.items.put(itemName,dt);
            pf.itemArr.put(pf.itemArr.size()+1,itemName);
        }
    }

    public static void setLengthFiled(String pkgName,String itemName){
        PackgeFormat pf=DataFormatter.get(pkgName);
        pf.packgeTotalLength=0;
        pf.lengthFiled=itemName;
    }

    protected boolean checkItemDef(String itemName){
        return DataFormatter.get(pkgName).items.containsKey(itemName);
    }

    //实际存储的数据
    private Map<String,byte[]> values=new HashMap<>();
    public int Length=0;
    public void setValue(String itemName,byte[] value) throws Exception {
        utils.Check(value==null,"值不能为空:"+itemName);
        utils.Check(!checkItemDef(itemName),"不包含值:"+itemName);
        Length=0;
        int dl=DataFormatter.get(pkgName).items.get(itemName).byteCount;
        utils.Check(dl!=0&&dl!=value.length,"值的字节长度不等于缺省值长度:"+itemName);
        values.put(itemName,value);
    }
    public byte[] getValue(String itemName) throws Exception {
        utils.Check(!checkItemDef(itemName),"不包含值:"+itemName);
        //设置值了就用设置的值
        if(values.containsKey(itemName))
            return values.get(itemName);
        //没设值则用缺省值
        DataItem item=DataFormatter.get(pkgName).items.get(itemName);
        if(item.defaultValue!=null)
            return item.defaultValue;
        else
        //缺省值也没设则看是否是变长字段，是变长字段则返回空，否则返回全0的固定字段
            if(item.byteCount==0)
                return null;
            else
                return new byte[item.byteCount];
    }

    public byte getValue(int index) throws Exception {
        utils.Check(index < 0, "值索引应大于等于0：" + index);
        int id = index;
        PackgeFormat pf = DataFormatter.get(pkgName);
        for (int i = 1; i <= pf.itemArr.size(); i++) {
            String itemname = pf.itemArr.get(i);
            byte[] arr = getValue(itemname);
            if (arr != null) {
                if (id < arr.length)
                    return arr[id];
                else
                    id -= arr.length;
            }
        }
        throw new Exception("值索引超出范围：" + index);
    }
    public int getPkgLength() throws Exception {
        //jxLog.logger.debug("call super::getPkgLength");
        PackgeFormat pf=DataFormatter.get(pkgName);
        for(int i=1;i<=pf.itemArr.size();i++){
            String itemname=pf.itemArr.get(i);
            byte[] arr=getValue(itemname);
            if(arr!=null)
                Length += arr.length;
        }
        return Length;
    }

    public byte[] getPacket() throws Exception {
        setLength();
        setCRC();
        PackgeFormat pf=DataFormatter.get(pkgName);
        byte[] rs=new byte[getPkgLength()];
        int start=0;
        for(int i=1;i<=pf.itemArr.size();i++){
            String itemname=pf.itemArr.get(i);
            byte[] arr=getValue(itemname);
            if(arr!=null) {
                System.arraycopy(arr, 0, rs, start, arr.length);
                start += arr.length;
            }
        }
        return rs;
    }

    protected boolean checkCRC() throws Exception {
        byte[] crc = getValue("CRC");
        byte[] bs = computeCRC();
        return utils.checkByteArr(crc, bs);
    }

    public void setCRC() throws Exception {
        byte[] bs = computeCRC();
        setValue("CRC", bs);
    }
    /**
     * 计算校验和
     * @return
     */
    protected byte[] computeCRC() throws Exception {return null; }

    //因为存在变长字段，而且一般的com数据包都含有包头、包尾、crc，而它们一般不记入包长，所以包长一般需单独计算
    protected void setLength(){ }

    /**
     * com通信一般是一发一收：cmd-respose，本函数主要用来接收到响应包后利用命令包进行校验
     * @param cmdpkg
     * @return
     */
    public boolean check(ComData cmdpkg){return true;}


    protected static class PackgeFormat {
        public Map<Integer, String> itemArr = new HashMap<>();
        public Map<String, DataItem> items = new HashMap<>();
        //如果不为0则为固定包长，length字段不一定存在
        public int packgeTotalLength = 0;
        //长度字段用于从字节流中恢复包时确定包长
        public String lengthFiled = null;
    }

    private static class DataItem{
        String itemName=null;
        //为0则代表可变长度
        int byteCount=0;
        byte[] defaultValue=null;
    }

    public String disp() throws Exception {
        String rs="";
        PackgeFormat pf=DataFormatter.get(pkgName);
        for(int i=1;i<=pf.itemArr.size();i++){
            String itemname=pf.itemArr.get(i);
            rs+=itemname+":";
            byte[] v=getValue(itemname);
            if(v!=null)
                rs+= Trans.TransToHexString(v," ");
            rs+="\n";
        }
        return rs;
    }

}
