package cn.ijingxi.intelControl;

import cn.ijingxi.orm.jxJson;

/**
 * Created by andrew on 16-3-4.
 */
public class FrontCommunication {

    public String devName=null;
    public String devType=null;
    public int ver=0;
    private jxJson info=null;

    public FrontCommunication(String devName,String devType,int ver){
        this.devName=devName;
        this.devType=devType;
        this.ver=ver;
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
