package cn.ijingxi.common.intelControl;

import java.util.Map;

/**
 * Created by andrew on 16-3-8.
 */
public interface IFrontConfigFunc {
    FrontCommunication exec(Map<String, Object> map);
}
