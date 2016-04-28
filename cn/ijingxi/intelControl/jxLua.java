package cn.ijingxi.intelControl;

import cn.ijingxi.orm.jxJson;
import cn.ijingxi.util.*;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
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
    public static final String LuaScript_InitEntry = "init";
    public static final String LuaScript_ClearEntry = "close";
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
        if (front != null)
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
    //一定要有name、type、ver，name不能重复，ver如果增加了就会将原配置删除，增加新的配置
    //type：Front，具备通信能力的前端设备
    //      lua，处理脚本，位于./conf/下中所定义的lua脚本
    //      该脚本必须定义两个函数：init是初始化函数入口，close是清理函数入口
    //function exec()
    //  local table={
    //        {name="USR1",type="Front",ver=1,startRead=true,ip="172.16.1.12",port=8899},
    //        {name="USR2",type="Front",ver=3,startRead=false,ip="172.16.1.13",port=8899},
    //        {name="USR2control",type="lua",ver=3,scriptFileName="USR2control.lua"},
    //  }
    //  return table
    //end
    //
    public static Map<String, IFrontConfigFunc> allConf = new HashMap<>();
    public static Map<String, luaGlobals> allDual = new HashMap<>();

    static class luaGlobals {
        Globals g = null;
        int ver = 0;
    }

    public static void addConf(String confFileName, IFrontConfigFunc dualConf) {
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
                        LuaValue t = (LuaValue) ecl.getValue();
                        Map<String, Object> map = jxLua.getKV(t);
                        String name = ((LuaString) map.get("name")).checkjstring();
                        int ver = ((LuaInteger) map.get("ver")).checkint();
                        String type = ((LuaString) map.get("type")).checkjstring();
                        switch (type) {
                            case "Front":
                                //实现了Front接口的通信设备
                                FrontCommunication front = jxLua.getFront(name);
                                if (front != null)
                                    if (front.ver < ver) {
                                        front.close();
                                        removeFront(name);
                                        //加入
                                    } else
                                        break;
                                jxLog.debug("add conf:" + cfn + ",name:" + name + ",ver:" + ver);
                                IFrontConfigFunc df = entry.getValue();
                                front = df.exec(map);
                                if (front != null)
                                    addFront(name, front);
                                break;
                            case "lua":
                                //lua控制脚本
                                luaGlobals lg = null;
                                if (!allDual.containsKey(name)) {
                                    lg = new luaGlobals();
                                    allDual.put(name, lg);
                                } else {
                                    lg = allDual.get(name);
                                    if (lg != null && lg.ver < ver)
                                        execFunc(lg.g, LuaScript_ClearEntry, null);
                                    else
                                        break;
                                }
                                lg.ver = ver;
                                lg.g = JsePlatform.standardGlobals();
                                String sf = ((LuaString) map.get("scriptFileName")).checkjstring();
                                jxLog.debug(String.format("%s ver:%d run:%s", name, ver, sf));
                                LuaValue chunk = lg.g.loadfile("./conf/" + sf);
                                chunk.call();
                                execFunc(lg.g, LuaScript_InitEntry, null);
                                break;
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

    public static void init() {
        jxTimer.DoPeriod(30, param -> checkConf(), null);
    }

    private static jxSparseTable<String, Integer, LuaValue> perionread = new jxSparseTable();

    //
    //lua脚本调addPeriodRead(devName,pin,cacllback)
    //当startRead启动后，轮询读取数据后调用informPinData_PeriodRead送入读取到的数据，
    //系统然后回调通知到lua脚本
    //
    static class addPeriodRead extends ThreeArgFunction {
        public LuaValue call(LuaValue devName, LuaValue pin, LuaValue cacllback) {
            String dv = devName.checkjstring();
            int p = pin.checkint();

            perionread.Add(dv, p, cacllback);
            return LuaValue.NIL;
        }
    }

    public static void informPinData_PeriodRead_SM(String devName, int pin, Integer value) {
        LuaValue cs = perionread.Search(devName, pin);
        if (cs != null && cs.isnil())
            luaStateMachine.happen(devName, pin, value.toString());
    }
    public static void informPinData_PeriodRead(String devName, int pin, float value) {
        LuaValue cs = perionread.Search(devName, pin);
        if (cs != null)
            cs.call(LuaValue.valueOf(pin), LuaValue.valueOf(value));
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
            return LuaValue.NIL;
        }
    }

    private static Map<String, String> lockMap = new HashMap<>();

    static class lock extends OneArgFunction {
        public LuaValue call(LuaValue lockName) {
            String js = lockName.checkjstring();
            lockMap.put(js, "lock");
            return LuaValue.NIL;
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
            return LuaValue.NIL;
        }
    }

    static class log extends OneArgFunction {
        public LuaValue call(LuaValue msg) {
            String m = msg.checkjstring();
            jxLog.debug(m);
            return LuaValue.NIL;
        }
    }
    static class error extends OneArgFunction {
        public LuaValue call(LuaValue msg) {
            String m = msg.checkjstring();
            jxLog.error(m);
            return LuaValue.NIL;
        }
    }
    static class warn extends OneArgFunction {
        public LuaValue call(LuaValue msg) {
            String m = msg.checkjstring();
            jxLog.warn(m);
            return LuaValue.NIL;
        }
    }

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue library = LuaValue.tableOf();
        library.set("log", new log());
        library.set("error", new error());
        library.set("warn", new warn());

        library.set("lock", new lock());
        library.set("checkLock", new checkLock());

        library.set("delay", new delay());
        library.set("addPeriodRead", new addPeriodRead());

        env.set("jxLua", library);
        return library;
    }

    private static LuaValue execFunc(Globals globals, String funcName, LuaValue[] params) {
        //jxLog.debug("jxLua exec starting");
        LuaValue fn = globals.get(funcName);
        LuaValue rs = null;
        int len = 0;
        if (params != null)
            len = params.length;
        switch (len) {
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
        //jxLog.debug("jxLua run starting");
        //utils.Check(params.length > 3, "只支持最多3个参数的调用");
        Globals globals = JsePlatform.standardGlobals();
        LuaValue chunk = globals.load(script);
        chunk.call();
        return exec(globals, params);
    }

    public static LuaValue runFile(String scriptFileName, LuaValue... params) throws Exception {
        //jxLog.debug("jxLua runFile:" + scriptFileName);
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
        luaScript s = new luaScript();
        s.global = JsePlatform.standardGlobals();
        LuaValue chunk = s.global.loadfile(scriptFileName);
        chunk.call();
        return s;
    }

    public static class luaScript {
        Globals global = null;
        Thread t = null;

        public LuaValue exec(String funcName, LuaValue... params) {
            return execFunc(global, funcName, params);
        }

        /**
         * 不干什么，只为了挂住
         */
        public void startTread() {
            t = new Thread(() -> {
                while (true)
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
            //jxLog.debug(String.format("key:%s,value:%s",k.checkjstring(),v.toString()));
            rs.put(k.checkjstring(), v);
        }
        return rs;
    }

    /**
     * 配置文件都放到了conf目录下
     *
     * @param confFileName
     * @return
     * @throws Exception
     */
    public static Map<String, Object> getConf(String confFileName) throws Exception {
        return getConf(confFileName, null);
    }

    public static Map<String, Object> getConf(String confFileName, String key) throws Exception {
        LuaValue luars = jxLua.runFile("./conf/" + confFileName);
        Map<String, Object> maplist = jxLua.getKV(luars);
        if (key == null)
            for (Map.Entry<String, Object> ecl : maplist.entrySet()) {
                jxLog.debug(ecl.getValue().toString());
                //避免某一个配置项有错误影响到其它配置项
                LuaValue t = (LuaValue) ecl.getValue();
                return jxLua.getKV(t);
            }
        else {
            LuaValue t = (LuaValue) maplist.get(key);
            if (t != null)
                return jxLua.getKV(t);
        }
        return null;
    }

    public static jxSparseTable<String, String, Object> getAllConf(String confFileName) throws Exception {
        jxSparseTable<String, String, Object> rs = new jxSparseTable();
        LuaValue luars = jxLua.runFile("./conf/" + confFileName);
        Map<String, Object> maplist = jxLua.getKV(luars);
        for (Map.Entry<String, Object> ecl : maplist.entrySet()) {
            jxLog.debug(ecl.getValue().toString());
            //避免某一个配置项有错误影响到其它配置项
            LuaValue t = (LuaValue) ecl.getValue();
            Map<String, Object> map = jxLua.getKV(t);
            for (Map.Entry<String, Object> ecl1 : map.entrySet())
                rs.Add(ecl.getKey(), ecl1.getKey(), ecl1.getValue());
        }
        return rs;
    }

}
