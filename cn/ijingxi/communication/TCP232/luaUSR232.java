package cn.ijingxi.communication.TCP232;

import cn.ijingxi.intelControl.jxLua;
import cn.ijingxi.util.jxLog;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

/**
 * Created by andrew on 16-3-5.
 */
public class luaUSR232 extends TwoArgFunction {

    static class send extends TwoArgFunction {
        public LuaValue call(LuaValue devName,LuaValue value) {
            try {
                String cmd=value.checkjstring();
                byte b=0x40;
                if("OpenHeat".compareTo(cmd)==0)
                    b=0x41;
                else if("OpenCool".compareTo(cmd)==0)
                    b=0x42;
                Object rs = jxLua.send(devName.checkjstring(), b);
                return LuaValue.valueOf(((byte[])rs)[0]);
            } catch (Exception e) {
                jxLog.error(e);
            }
            return LuaValue.NIL;
        }
    }


    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue library = LuaValue.tableOf();
        library.set( "send", new send() );

        env.set( "luaUSR232", library );
        return library;
    }
}
