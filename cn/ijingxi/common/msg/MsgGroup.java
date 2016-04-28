package cn.ijingxi.common.msg;

import cn.ijingxi.app.Group;
import cn.ijingxi.orm.ORMID;
import cn.ijingxi.orm.ORMType;

import java.util.UUID;

/**
 * Created by andrew on 15-9-5.
 */
public class MsgGroup extends Group {

    public static ORMID GetORMID(UUID ID)
    {
        return new ORMID(ORMType.MsgGroup.ordinal(),ID);
    }
    public static void Init() throws Exception{
        InitClass(ORMType.MsgGroup.ordinal(),Message.class,"消息组");
    }


}
