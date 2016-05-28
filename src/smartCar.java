import cn.ijingxi.ServerCommon.httpServer.jxHttpRes;
import cn.ijingxi.ServerCommon.httpServer.jxHttpServer;
import cn.ijingxi.app.ObjTag;
import cn.ijingxi.app.People;
import cn.ijingxi.data.JdbcSqlite.JdbcSqlite;
import cn.ijingxi.live.Medium;
import cn.ijingxi.orm.JdbcUtils;
import cn.ijingxi.pi.wheel;
import cn.ijingxi.util.jxLog;
import cn.ijingxi.util.utils;


/**
 * Created by andrew on 16-3-27.
 */
public class smartCar {

    public static void main(String args[]) throws Exception {

        if(ObjTag.SystemID==null){
            //尚未初始化
            jxLog.logger.debug("Init");
            JdbcSqlite db=null;
            try {
                db = new JdbcSqlite("main");
                JdbcUtils.SetDB(db);

                utils.Init();
                if(ObjTag.System==null)
                {
                    //系统尚未建库
                    jxLog.logger.debug("CreateDBTable");
                    utils.CreateDBTable();
                    People p=People.createPeople("admin","管理员");
                    p.Insert();
                }


                jxHttpRes.InitResClass(Medium.class);

                jxLog.logger.debug("wheel init");
                wheel.init();

                jxHttpServer server1=new jxHttpServer(10000, "./web_videojs/", null);
                server1.start();
                jxHttpServer server2=new jxHttpServer(10002, "./web_html5/", null);
                server2.start();

            } catch (Exception e) {
                jxLog.error(e);
            }

        }
    }
}
