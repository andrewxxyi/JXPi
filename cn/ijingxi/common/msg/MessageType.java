package cn.ijingxi.common.msg;

import cn.ijingxi.util.IjxEnum;

/**
 * Created by andrew on 15-10-5.
 */
public enum MessageType implements IjxEnum
{
    //事件
    Event,
    //消息
    Info;

    @Override
    public Object TransToORMEnum(Integer param)
    {
        return MessageType.values()[param];
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