package cn.ijingxi.intelControl;

import org.luaj.vm2.LuaValue;

import java.util.Map;

/**
 * Created by andrew on 16-3-8.
 */
public interface IFrontConfigFunc {
    FrontCommunication exec(Map<String, LuaValue> map);
}
