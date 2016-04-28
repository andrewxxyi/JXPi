package cn.ijingxi.com.USRIO88;

import cn.ijingxi.communication.comTrans_CmdResponse;
import cn.ijingxi.intelControl.jxLua;
import cn.ijingxi.util.jxLog;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

/**
 * Created by andrew on 16-3-5.
 */
public class luaUSR extends TwoArgFunction {

    static class closeChannel extends TwoArgFunction {
        public LuaValue call(LuaValue devName,LuaValue channelID) {
            try {
                ComData_USR_Cmd com = new ComData_USR_Cmd();
                com.setCmd((byte) 0x01);
                com.setParam(new byte[]{channelID.tobyte()});
                comTrans_CmdResponse.syncResult rs =
                        (comTrans_CmdResponse.syncResult)jxLua.send(devName.checkjstring(), com);
                if(!rs.OK)
                    return LuaValue.valueOf((String) rs.error);
            } catch (Exception e) {
                jxLog.error(e);
                return LuaValue.valueOf("出现错误："+e.getMessage());
            }
            return LuaValue.NIL;
        }
    }
    static class openChannel extends TwoArgFunction {
        public LuaValue call(LuaValue devName,LuaValue channelID) {
            try {
                ComData_USR_Cmd com = new ComData_USR_Cmd();
                com.setCmd((byte) 0x02);
                com.setParam(new byte[]{channelID.tobyte()});
                comTrans_CmdResponse.syncResult rs =
                        (comTrans_CmdResponse.syncResult)jxLua.send(devName.checkjstring(), com);
                if(!rs.OK)
                    return LuaValue.valueOf((String) rs.error);
            } catch (Exception e) {
                jxLog.error(e);
                return LuaValue.valueOf("出现错误："+e.getMessage());
            }
            return null;
        }
    }



    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue library = LuaValue.tableOf();
        library.set( "closeChannel", new closeChannel() );
        library.set( "openChannel", new openChannel() );

        env.set( "luaUSR", library );
        return library;
    }
}
