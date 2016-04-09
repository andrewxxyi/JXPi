package cn.ijingxi.ServerCommon.confServer;

import cn.ijingxi.ServerCommon.httpServer.RES;
import cn.ijingxi.ServerCommon.httpServer.jxHttpData;
import cn.ijingxi.common.orm.jxJson;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrew on 15-12-6.
 */
public class ConfServer {

    private Object lock=new Object();
    private Map<String,jxJson> services=new HashMap<>();

    @RES
    public jxHttpData registerService(Map<String,Object> ps, jxJson Param)
    {
        try{
            synchronized (lock){
                String name=(String) Param.GetSubValue("Name");
                services.remove(name);
                services.put(name,Param);
                jxHttpData rs=new jxHttpData(200,"处理完毕");
                rs.setResult(true);
                return rs;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            jxHttpData rs=new jxHttpData(503,"内部错误："+e.getMessage());
            return rs;
        }
    }

    @RES
    public jxHttpData getServiceInfo(Map<String,Object> ps, jxJson Param)
    {
        try{
            synchronized (lock){
                String name=(String) Param.GetSubValue("Name");
                jxJson info = services.get(name);
                jxHttpData rs=new jxHttpData(200,"处理完毕");
                rs.addJson(info);
                return rs;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            jxHttpData rs=new jxHttpData(503,"内部错误："+e.getMessage());
            return rs;
        }
    }

}
