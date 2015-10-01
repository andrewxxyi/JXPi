package cn.ijingxi.common.msg;

import java.util.UUID;

/**
 * Created by andrew on 15-9-4.
 */
public interface IMsgDual {
    UUID getID();
    void DualMsg(Message msg);

}
