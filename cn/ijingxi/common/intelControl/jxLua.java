package cn.ijingxi.common.intelControl;

import cn.ijingxi.common.orm.jxJson;
import cn.ijingxi.common.util.IDo;
import cn.ijingxi.common.util.jxLog;
import cn.ijingxi.common.util.jxTimer;
import cn.ijingxi.common.util.utils;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrew on 16-3-4.
 */
public class jxLua extends TwoArgFunction {
    //lua脚本调用入口，即每个控制脚本都必须实现这个函数，系统也会调用这个函数
    public static final String LuaScript_CallEntry = "exec";
    //lua脚本存放位置
    public static final String LuaScriptRoot = "./luaFunction/";

    protected static Map<String, FrontCommunication> allFront = new HashMap<>();

    public static boolean checkFront(String devName) throws Exception {
        return allFront.containsKey(devName);
    }
    public static FrontCommunication getFront(String devName) throws Exception {
        return allFront.get(devName);
    }

    public static void addFront(String devName, FrontCommunication front) throws Exception {
        utils.Check(allFront.containsKey(devName), "设备已存在:" + devName);
        if (front != null)
            allFront.put(devName, front);
    }
    public static void removeFront(String devName) throws Exception {
        FrontCommunication front = allFront.remove(devName);
        if(front!=null)
            front.close();
    }


    public static jxJson send(String devName, jxJson json) throws Exception {
        FrontCommunication front = allFront.get(devName);
        if (front != null)
            return front.send(json);
        throw new Exception("前端设备尚未接入：" + devName);
    }

    public static Object send(String devName, Object data) throws Exception {
        FrontCommunication front = allFront.get(devName);
        if (front != null)
            return front.send(data);
        throw new Exception("前端设备尚未接入：" + devName);
    }

    public static byte[] send(String devName, byte[] data) throws Exception {
        FrontCommunication front = allFront.get(devName);
        if (front != null)
            return front.send(data);
        throw new Exception("前端设备尚未接入：" + devName);
    }

    //
    //定时轮询检查前端设备配置是否变化，如有变化则进行调整
    //
    //配置文件格式：
    //一定要有name和ver，name不能重复，ver如果增加了就会将原配置删除，增加新的配置
    //function exec()
    //  local table={
    //        {name="USR1",ver=1,startRead=true,ip="172.16.1.12",port=8899},
    //        {name="USR2",ver=3,startRead=false,ip="172.16.1.13",port=8899}
    //  }
    //  return table
    //end
    //
    public static Map<String,IFrontConfigFunc> allConf=new HashMap<>();
    public static void addConf(String confFileName,IFrontConfigFunc dualConf){
        allConf.put(confFileName, dualConf);
    }
    private static void checkConf() throws Exception {
        for (Map.Entry<String, IFrontConfigFunc> entry : allConf.entrySet()) {
            String cfn = entry.getKey();
            LuaValue luars = jxLua.runFile(cfn);
            try {
                //避免某一个配置文件有错误影响到其它配置文件
                Map<String, Object> maplist = jxLua.getKV(luars);
                for (Map.Entry<String, Object> ecl : maplist.entrySet()) {
                    try {
                        //避免某一个配置项有错误影响到其它配置项
                        LuaValue t = (LuaValue) entry.getValue();
                        Map<String, Object> map = jxLua.getKV(t);
                        String name = ((LuaString) map.get("name")).checkjstring();
                        int ver = ((LuaInteger) map.get("ver")).checkint();
                        if (!jxLua.checkFront(name)) {
                            jxLog.debug("lua checkConf:" + cfn + ",name:" + name + ",ver:" + ver);
                            IFrontConfigFunc df = entry.getValue();
                            FrontCommunication front=df.exec(map);
                            addFront(name,front);
                        } else {
                            FrontCommunication front = jxLua.getFront(name);
                            if (front != null && ver > front.ver) {
                                front.close();
                                removeFront(name);
                                //加入
                                IFrontConfigFunc df = entry.getValue();
                                front=df.exec(map);
                                addFront(name,front);
                            }
                        }
                    } catch (Exception e1) {
                        jxLog.error(e1);
                    }
                }
            } catch (Exception e) {
                jxLog.error(e);
            }
        }
    }

    public static void init(){
        jxTimer.DoPeriod(30,param -> checkConf(),null);
    }




    //
    //功能函数
    //
    static class delay extends OneArgFunction {
        public LuaValue call(LuaValue millis) {
            int js = millis.checkint();
            try {
                Thread.sleep(js);
            } catch (InterruptedException e) {
                jxLog.error(e);
            }
            return null;
        }
    }

    private static Map<String, String> lockMap = new HashMap<>();

    static class lock extends OneArgFunction {
        public LuaValue call(LuaValue lockName) {
            String js = lockName.checkjstring();
            lockMap.put(js, "lock");
            return null;
        }
    }

    static class checkLock extends OneArgFunction {
        public LuaValue call(LuaValue lockName) {
            String js = lockName.checkjstring();
            jxLog.debug("checkLock:" + js);
            if (js != null && lockMap.containsKey(js)) {
                String rs = lockMap.get(js);
                jxLog.debug("lockName:" + rs);
                if (rs != null)
                    return LuaValue.valueOf(true);
            }
            return null;
        }
    }

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue library = LuaValue.tableOf();
        library.set("lock", new lock());
        library.set("checkLock", new checkLock());

        library.set("delay", new delay());

        env.set("jxLua", library);
        return library;
    }

    private static LuaValue execFunc(Globals globals,String funcName, LuaValue[] params) {
        jxLog.debug("jxLua exec starting");
        LuaValue fn = globals.get(funcName);
        LuaValue rs = null;
        switch (params.length) {
            case 0:
                rs = fn.call();
                break;
            case 1:
                rs = fn.call(params[0]);
                break;
            case 2:
                rs = fn.call(params[0], params[1]);
                break;
            case 3:
                rs = fn.call(params[0], params[1], params[2]);
                break;
            default:
                Varargs rsa = fn.invoke(params);
                if (rsa != null)
                    return rsa.arg(1);
                break;
        }
        jxLog.debug("jxLua exec end");
        return rs;
    }
    private static LuaValue exec(Globals globals, LuaValue[] params) {
        return execFunc(globals, LuaScript_CallEntry, params);
    }

    public static LuaValue run(String script, LuaValue... params) throws Exception {
        jxLog.debug("jxLua run starting");
        //utils.Check(params.length > 3, "只支持最多3个参数的调用");
        Globals globals = JsePlatform.standardGlobals();
        LuaValue chunk = globals.load(script);
        chunk.call();
        return exec(globals, params);
    }

    public static LuaValue runFile(String scriptFileName, LuaValue... params) throws Exception {
        jxLog.debug("jxLua runFile:" + scriptFileName);
        //utils.Check(params.length > 3, "只支持最多3个参数的调用");
        Globals globals = JsePlatform.standardGlobals();
        LuaValue chunk = globals.loadfile(scriptFileName);
        chunk.call();
        return exec(globals, params);
    }

    public static void run_Async(String script, IDo getResult, LuaValue... params) throws Exception {
        jxLog.debug("jxLua run_Async starting");
        jxTimer.asyncRun(param -> {
            Globals globals = JsePlatform.standardGlobals();
            LuaValue chunk = globals.load(script);
            chunk.call();
            LuaValue rs = exec(globals, params);
            if (getResult != null)
                getResult.Do(rs);
        }, null);
    }

    public static void runFile_Async(String script, IDo getResult, LuaValue... params) throws Exception {
        jxLog.debug("jxLua runFile_Async starting");
        jxTimer.asyncRun(param -> {
            Globals globals = JsePlatform.standardGlobals();
            LuaValue chunk = globals.loadfile(script);
            chunk.call();
            LuaValue rs = exec(globals, params);
            if (getResult != null)
                getResult.Do(rs);
        }, null);
    }

    public static luaScript loadFile(String scriptFileName, LuaValue... params) throws Exception {
        jxLog.debug("jxLua loadFile:" + scriptFileName);
        luaScript s=new luaScript();
        s.global= JsePlatform.standardGlobals();
        LuaValue chunk = s.global.loadfile(scriptFileName);
        chunk.call();
        return s;
    }
    public static class luaScript{
        Globals global=null;
        Thread t=null;
        public LuaValue exec (String funcName, LuaValue... params){
            return execFunc(global, funcName, params);
        }

        /**
         * 不干什么，只为了挂住
         */
        public void startTread(){
            t=new Thread(()->{
                while(true)
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                    }
            });
            t.start();
        }
    }
    public static Map<String, Object> getKV(LuaValue table) {
        Map<String, Object> rs = new HashMap<>();
        LuaValue k = LuaValue.NIL;
        while (true) {
            Varargs n = table.next(k);
            if ((k = n.arg1()).isnil())
                break;
            LuaValue v = n.arg(2);
            rs.put(k.checkjstring(), v);
        }
        return rs;
    }

}
