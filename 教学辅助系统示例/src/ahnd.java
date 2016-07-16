import bll.*;
import cn.ijingxi.Rest.httpServer.jxHttpREST;
import cn.ijingxi.Rest.httpServer.jxHttpServer;
import cn.ijingxi.app.ObjTag;
import cn.ijingxi.data.JdbcSqlite.JdbcSqlite;
import cn.ijingxi.orm.JdbcUtils;
import cn.ijingxi.ui.system;
import cn.ijingxi.util.jxLog;
import cn.ijingxi.util.utils;
import org.luaj.vm2.LuaValue;

import static cn.ijingxi.app.ObjTag.getSystemLuaConf;

/**
 * 系统入口
 *
 * Created by andrew on 16-5-24.
 */
public class ahnd {

	//主函数，负责完成系统初始化工作
    public static void main(String args[]) throws Exception {

        if (ObjTag.SystemID == null) {
            //尚未初始化
            jxLog.logger.debug("Init");
            JdbcSqlite db = null;
            try {
                //设置数据库为当前目录下的sqlite数据库：main.db
                db = new JdbcSqlite("main");
                JdbcUtils.SetDB(db);

                //首先进行cn.ijingxi.jar包的初始化
                utils.Init();
                //然后进行自己的初始化，初始化顺序不能颠倒
                common.Init();

                //创建数据表
                //目前的实现方式是系统会自动检测数据库中的ORM数据类所对应的数据表是否已创建
                //如果已创建则忽略，如果未创建则自动创建，因此当有新的ORM数据类定义时，再次启动时相应的数据表即自动创建
                utils.CreateDBTable();
                //创建顺序也不能颠倒，必须先建cn.ijingxi.jar包中定义的数据表，然后才能建自己的
                common.CreateDBTable();

                //读取./conf/system.lua中的配置项WebServerPort，该配置项定义了系统web服务所在的TCP端口
                LuaValue v = getSystemLuaConf("WebServerPort");
                utils.checkAssert(!v.isnil(),"WebServerPort未指定，程序结束");
                int wport=v.checkint();
                utils.checkAssert(wport>0,"WebServerPort应大于0，程序结束");

                //如果系统提供了REST接口，则每一个REST资源类都必须先进行初始化
                jxHttpREST.InitResClass(system.class);

                jxHttpREST.InitResClass(Person.class);
                jxHttpREST.InitResClass(plan.class);
                jxHttpREST.InitResClass(schedule.class);
                jxHttpREST.InitResClass(question.class);
                jxHttpREST.InitResClass(team.class);

                jxHttpREST.InitResClass(task.class);
                jxHttpREST.InitResClass(coding.class);
                jxHttpREST.InitResClass(testing.class);
                jxHttpREST.InitResClass(subject.class);

                //以配置的端口号启动web服务，并指定web服务的根目录为./web_manager/
                jxHttpServer server3 = new jxHttpServer(wport, "./web_manager/", null);
                server3.start();

            } catch (Exception e) {
                jxLog.error(e);
            }

        }
    }

}
