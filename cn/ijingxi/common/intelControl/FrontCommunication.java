package cn.ijingxi.common.intelControl;

import cn.ijingxi.common.orm.jxJson;

/**
 * Created by andrew on 16-3-4.
 */
public class FrontCommunication {

    private String devName=null;
    private String devType=null;
    public int ver=0;
    private jxJson info=null;
    public FrontCommunication(String devName,String devType){
        this.devName=devName;
        this.devType=devType;
        info=jxJson.GetObjectNode("Front");
    }
    public void setAttribute(String name,Object value) throws Exception {
        info.setSubObjectValue(name,value);
    }
    public Object getAttribute(String name) throws Exception {
        return info.getSubObjectValue(name);
    }




    public jxJson send(jxJson json) throws Exception{return null;}
    public Object send(Object data) throws Exception{return null;}
    public byte[] send(byte[] data) throws Exception{return null;}

    public void close(){}

}
