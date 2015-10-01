package cn.ijingxi.common.msg;

import cn.ijingxi.common.app.TopSpace;
import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.util.IjxEnum;

import java.util.Date;
import java.util.UUID;

/**
 * 使用前需调用setMsgService设置消息服务
 * 然后对需要进行消息处理的类，调用jxORMobj.setDualMsg来设置消息处理函数
 * Created by andrew on 15-9-4.
 */
public class Message extends jxORMobj{

    public enum MsgType implements IjxEnum
    {
        //事件
        Event,
        //消息
        Info;

        @Override
        public Object TransToORMEnum(Integer param)
        {
            return MsgType.values()[param];
        }

        @Override
        public String toChinese()
        {
            switch(this)
            {
                case Event:
                    return "事件";
                case Info:
                    return "消息";
            }
            return "";
        }
    }
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

    public static void Init() throws Exception{	InitClass(ORMType.Message.ordinal(),Message.class);}
    public static void CreateDB(TopSpace ts) throws Exception
    {
        CreateTableInDB(Message.class,ts);
    }

    private static IMsgService msgService = null;
    public static void setMsgService(IMsgService service){
        msgService=service;
    }
    public static IMsgService getMsgService(){
        return msgService;
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
    public MsgType MessageType;

    @ORM(Index=3)
    public Date CreateTime;

    @ORM(Descr="提供给消息的使用者，提供诸如会话、排序等")
    public int MsgID;
    @ORM(Descr="用于消息的进一步分类，如事件中标识事件号等等")
    public int MsgUsedID;

    @ORM(Descr="json格式的附加信息")
    public String Info;

    public void send(){
        if(msgService!=null)
            msgService.SendMsg(this);
    }
}
