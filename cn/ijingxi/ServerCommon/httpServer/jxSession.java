package cn.ijingxi.ServerCommon.httpServer;

import cn.ijingxi.system.jxAutoDeleteMap;
import cn.ijingxi.util.Trans;
import cn.ijingxi.util.jxLog;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by andrew on 15-12-21.
 */
public class jxSession {

    private String id=null;
    public String getID(){return id;}

    private static jxAutoDeleteMap sessions=new jxAutoDeleteMap(60*15,null);

    private Map<String,Object> cache=new HashMap<>();

    public static jxSession search(String sessionID){
        return (jxSession) sessions.get(sessionID);
    }
    public static jxSession create(){
        jxSession session=new jxSession();
        session.id=Trans.TransToString(UUID.randomUUID());
        sessions.put(session.id,session);
        return session;
    }

    public Object get(String k){return cache.get(k);}
    public void set(String k,Object v){cache.put(k,v);}

    public static UUID getPeopleID(String sessionID){
        UUID pid=null;
        if(sessionID!=null){
            jxLog.logger.debug("sessionID:"+sessionID);
            jxSession session = jxSession.search(sessionID);
            jxLog.logger.debug("session:"+(session!=null?"存在":"不存在"));
            if(session!=null)
                pid= (UUID) session.get(jxHttpServer.SessionPeopleIDName);
        }
        return pid;
    }
    public void setPeopleID(UUID pid){
        set(jxHttpServer.SessionPeopleIDName,pid);
    }


}
