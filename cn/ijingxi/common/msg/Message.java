package cn.ijingxi.common.msg;

import cn.ijingxi.orm.*;

import java.util.Date;
import java.util.UUID;

/**
 * 使用前需设置消息服务
 * 然后对需要进行消息处理的类，调用jxORMobj.setDualMsg来设置消息处理函数
 * Created by andrew on 15-9-4.
 */
public class Message extends jxORMobj{

    public static final UUID MsgID_Auth=UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID MsgID_Log=UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static final UUID MsgID_Config=UUID.fromString("00000000-0000-0000-0000-000000000003");


    public static ORMID GetORMID(UUID ID)
    {
        return new ORMID(ORMType.Message.ordinal(),ID);
    }

    @Override
    protected void Init_Create(DB db) throws Exception
    {
        //ID= UUID.randomUUID();
        CreateTime=new Date();
    }

    public static void Init() throws Exception {
        InitClass(ORMType.Message.ordinal(), Message.class,"消息");
    }
    public static void CreateDB() throws Exception
    {
        CreateTableInDB(Message.class);
    }

    public static Message New(int SenderTypeID,UUID SenderID,int ReceiverTypeID,UUID ReceiverID,MessageType MsgType,String Msg) throws Exception {
        Message msg= (Message) Message.Create(Message.class);
        msg.SenderTypeID=SenderTypeID;
        msg.SenderID=SenderID;
        msg.ReceiverTypeID=ReceiverTypeID;
        msg.ReceiverID=ReceiverID;
        msg.MsgType = MsgType;
        msg.Msg=Msg;
        return msg;
    }

    @ORM
    public int SenderTypeID;
    @ORM(Index=1)
    public UUID SenderID;

    @ORM
    public int ReceiverTypeID;
    @ORM(Index=2)
    public UUID ReceiverID;

    @ORM
    public MessageType MsgType;

    @ORM
    public String Msg;

    @ORM(Index=3)
    public Date CreateTime;

    @ORM(Descr="提供给消息的使用者，提供诸如会话、排序等")
    public int MsgID;
    @ORM(Descr="用于消息的进一步分类，如事件中标识事件号等等")
    public int MsgUsedID;

    @ORM(Descr="json格式的附加信息")
    public String Info;

    public void send(){
        MsgAgent.send(this);
    }
}
