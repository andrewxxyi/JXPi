package cn.ijingxi.ServerCommon.authServer;

import cn.ijingxi.ServerCommon.httpServer.RES;
import cn.ijingxi.ServerCommon.httpServer.jxHttpData;
import cn.ijingxi.common.app.People;
import cn.ijingxi.common.orm.jxJson;
import cn.ijingxi.common.system.jxAutoDeleteMap;

import java.util.Map;
import java.util.Random;

/**
 * Created by andrew on 15-12-7.
 */
public class AuthServer {

    private Object lock=new Object();
    //保留一小时
    private jxAutoDeleteMap  admap=new jxAutoDeleteMap(60*60,null);

    @RES
    public jxHttpData checkUserPasswd(Map<String,Object> ps, jxJson Param)
    {
        try{
            synchronized (lock){
                jxHttpData rs=new jxHttpData(200,"处理完毕");
                String name=(String) Param.GetSubValue("Name");
                String passwd=(String) Param.GetSubValue("Passwd");
                People p=  People.getPeopleByName(name);
                if(p!=null)
                    if(p.checkPasswd(passwd)){
                        rs.setResult(true);
                        Random r=new Random();
                        Long ac=r.nextLong();
                        rs.addValue("AuthCode",ac.toString());
                        rs.addValue("Name",name);
                        rs.addValue("ID",p.ID);
                        admap.put(ac.toString(),rs);
                    }
                    else
                        rs.setResult(false);
                else
                    rs.setResult(false);
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

    /**
     * 还未具体实现
     * @param ps
     * @param Param
     * @return
     */
    @RES
    public jxHttpData checkUserRight(Map<String,Object> ps, jxJson Param)
    {
        try{
            synchronized (lock){
                jxHttpData rs=new jxHttpData(200,"处理完毕");
                String name=(String) Param.GetSubValue("Name");
                String passwd=(String) Param.GetSubValue("Passwd");
                People p=  People.getPeopleByName(name);
                if(p!=null)
                    if(p.checkPasswd(passwd)){
                        rs.setResult(true);
                        Random r=new Random();
                        Long ac=r.nextLong();
                        rs.addValue("AuthCode",ac.toString());
                        rs.addValue("Name",name);
                        rs.addValue("ID",p.ID);
                        admap.put(ac.toString(),rs);
                    }
                    else
                        rs.setResult(false);
                else
                    rs.setResult(false);
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
