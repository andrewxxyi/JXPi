package cn.ijingxi.intelControl;

import cn.ijingxi.util.jxLog;
import cn.ijingxi.util.jxSparseTable;
import cn.ijingxi.util.jxStateMachine;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrew on 16-3-4.
 */
public class luaStateMachine extends TwoArgFunction {
    //lua脚本调用入口，即每个控制脚本都必须实现这个函数，系统也会调用这个函数
    public static final String LuaScript_CallEntry = "exec";
    //lua脚本存放位置
    public static final String LuaScriptRoot = "./luaFunction/";

    protected static Map<String, FrontCommunication> allFront = new HashMap<>();

    public static boolean checkFront(String devName) throws Exception {
        return allFront.containsKey(devName);
    }

    private static jxSparseTable<String,Integer,jxStateMachine<String,String>> sms=new jxSparseTable<>();
    private static jxSparseTable<String,Integer,jxStateMachine.realSM> realsm=new jxSparseTable<>();
    /**
     * params[0]：设备名
     * params[1]：inputPin
     * params[2]：事件前状态
     * params[3]：事件
     * params[4]：事件后状态
     * params[5]：callbackLuaFunc
     *
     */
    static class addTrans extends VarArgFunction {
        public LuaValue call(LuaValue... params) {
            String devName = params[0].checkjstring();
            int pin = params[1].checkint();
            String cs = params[2].checkjstring();
            String e = params[3].checkjstring();
            String ns = params[4].checkjstring();
            addSMTrans(devName,pin,cs,e,ns,params[5]);
            return LuaValue.NIL;
        }
    }
    static class setInitState extends ThreeArgFunction {
        public LuaValue call(LuaValue devName,LuaValue pin,LuaValue state) {
            String dv=devName.checkjstring();
            int p=pin.checkint();
            String s=state.checkjstring();
            jxStateMachine sm=sms.Search(dv,p);
            jxStateMachine.realSM r=sm.newRealSM(null,s);
            realsm.Add(dv,p,r);
            return null;
        }
    }
    static class happen extends ThreeArgFunction {
        public LuaValue call(LuaValue devName, LuaValue pin, LuaValue event) {
            String dv = devName.checkjstring();
            int p = pin.checkint();
            String e = event.checkjstring();
            happen(dv,p,e);
            return null;
        }
    }
    static void happen(String devName,int pin,String event){
        jxStateMachine<String, String> sm = sms.Search(devName, pin);
        if (sm != null) {
            synchronized (sm) {
                jxStateMachine.realSM r = realsm.Search(devName, pin);
                try {
                    r.happen(event, null);
                } catch (Exception e1) {
                    jxLog.error(e1);
                }
            }
        }
    }
    static class clear extends TwoArgFunction {
        public LuaValue call(LuaValue devName, LuaValue pin) {
            String dv = devName.checkjstring();
            int p = pin.checkint();
            sms.Delete(dv,p);
            realsm.Delete(dv,p);
            return null;
        }
    }


    /**
     * 0-1时调用cacllback
     */
    static class up extends ThreeArgFunction {
        public LuaValue call(LuaValue devName, LuaValue pin, LuaValue cacllback) {
            String dv = devName.checkjstring();
            int p = pin.checkint();
            addSMTrans(dv,p,"down","1","up",cacllback);
            //仅用于将状态复位
            addSMTrans(dv,p,"up","0","down",LuaValue.NIL);
            return LuaValue.NIL;
        }
    }

    /**
     * 1-0时触发调用cacllback
     */
    static class down extends ThreeArgFunction {
        public LuaValue call(LuaValue devName, LuaValue pin, LuaValue cacllback) {
            String dv = devName.checkjstring();
            int p = pin.checkint();
            addSMTrans(dv,p,"down","1","up",LuaValue.NIL);
            addSMTrans(dv,p,"up","0","down",cacllback);
            return LuaValue.NIL;
        }
    }

    /**
     * 0-1时调用upcacllback，1-0时触发调用downcacllback
     */
    static class updown extends VarArgFunction {
        public LuaValue call(LuaValue devName, LuaValue pin, LuaValue upcacllback, LuaValue downcacllback) {
            String dv = devName.checkjstring();
            int p = pin.checkint();
            addSMTrans(dv,p,"down","1","up",upcacllback);
            //仅用于将状态复位
            addSMTrans(dv,p,"up","0","down",downcacllback);
            return LuaValue.NIL;
        }
    }

    private static void addSMTrans(String devName, int pin, String currentState,String event,String newState,LuaValue cacllback){
        jxStateMachine<String, String> sm = sms.Search(devName, pin);
        if (sm == null) {
            sm = new jxStateMachine();
            sms.Add(devName, pin, sm);
        }
        sm.AddTrans(currentState, event, newState, (p1,p2) -> {
            if (!cacllback.isnil())
                cacllback.call();
        });
    }

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue library = LuaValue.tableOf();
        library.set("addTrans", new addTrans());
        library.set("setInitState", new setInitState());
        library.set("happen", new happen());
        library.set("clear", new clear());

        library.set("down", new down());
        library.set("up", new up());

        env.set("luaStateMachine", library);
        return library;
    }

}
