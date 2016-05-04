package cn.ijingxi.communication.NodeMCU;

import cn.ijingxi.intelControl.jxLua;
import cn.ijingxi.orm.jxJson;
import cn.ijingxi.util.Trans;
import cn.ijingxi.util.jxLog;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

/**
 * Created by andrew on 16-3-4.
 */
public class luaNodeMCU extends jxLua {


    static class cfgGPIO extends ThreeArgFunction {
        public LuaValue call(LuaValue devName,LuaValue pin,LuaValue mode) {
            jxJson json = jxJson.GetObjectNode("NodeMCU");
            try {
                json.AddValue("c", "c");
                json.AddValue("t", "g");
                json.AddValue("pin", pin.checkint());
                json.AddValue("m", pin.checkint());
                jxLua.send(devName.checkjstring(),json);
            } catch (Exception e) {
                jxLog.error(e);
            }
            return null;
        }
    }
    static class setGPIO extends ThreeArgFunction {
        public LuaValue call(LuaValue devName,LuaValue pin,LuaValue value) {
            jxJson json = jxJson.GetObjectNode("NodeMCU");
            try {
                json.AddValue("c", "s");
                json.AddValue("t", "g");
                json.AddValue("pin", pin.checkint());
                json.AddValue("a", value.checkint());
                jxLua.send(devName.checkjstring(),json);
                return LuaValue.valueOf(true);
            } catch (Exception e) {
                jxLog.error(e);
            }
            return null;
        }
    }
    static class getGPIO extends TwoArgFunction {
        public LuaValue call(LuaValue devName,LuaValue pin) {
            jxJson json = jxJson.GetObjectNode("NodeMCU");
            try {
                json.AddValue("c", "s");
                json.AddValue("t", "g");
                json.AddValue("pin", pin.checkint());
                jxJson rs = jxLua.send(devName.checkjstring(), json);                ;
                return LuaValue.valueOf(Trans.TransToInteger(rs.getSubObjectValue("d")));
            } catch (Exception e) {
                jxLog.error(e);
            }
            return null;
        }
    }

    static class cfgUart extends TwoArgFunction {
        public LuaValue call(LuaValue devName,LuaValue baud) {
            jxJson json = jxJson.GetObjectNode("NodeMCU");
            try {
                json.AddValue("c", "c");
                json.AddValue("t", "u");
                json.AddValue("b", baud.checkint());
                jxLua.send(devName.checkjstring(),json);
                return LuaValue.valueOf(true);
            } catch (Exception e) {
                jxLog.error(e);
            }
            return null;
        }
    }


    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue library = LuaValue.tableOf();
        library.set( "cfgGPIO", new cfgGPIO() );
        library.set( "cfgUart", new cfgUart() );

        library.set( "setGPIO", new setGPIO() );
        library.set( "getGPIO", new getGPIO() );

        env.set( "luaNodeMCU", library );
        return library;
    }

}
