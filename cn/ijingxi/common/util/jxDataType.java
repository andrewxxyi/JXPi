package cn.ijingxi.common.util;

/**
 * Created by andrew on 15-10-2.
 */
public enum  jxDataType implements IjxEnum
{
    None,
    Integer,
    Boolean,
    Float,
    String;

    @Override
    public Object TransToORMEnum(Integer param)
    {
        return jxDataType.values()[param];
    }

    @Override
    public String toChinese()
    {
        switch(this)
        {
            case Integer:
                return "整数";
            case Boolean:
                return "布尔";
            case Float:
                return "浮点数";
            case String:
                return "字符串";
        }
        return "None";
    }
}
