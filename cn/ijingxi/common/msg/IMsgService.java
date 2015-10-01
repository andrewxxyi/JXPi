package cn.ijingxi.common.msg;

import java.util.UUID;

/**
 * Created by andrew on 15-9-4.
 */
public interface IMsgService {
    void Register(UUID ReceiverID);
    void UnRegister(UUID ReceiverID);
    void SendMsg(Message msg);
    Boolean SyncSendMsg(Message msg);
    Message ReceiveMsg();

}
