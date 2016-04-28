package cn.ijingxi.util;

/**
 * Created by andrew on 15-10-2.
 */
public enum LogPriority implements IjxEnum {
    emerg,
    alert,
    crit,
    err,
    warning,
    notice,
    info,
    debug;


    @Override
    public Object TransToORMEnum(Integer param)
    {
        return LogPriority.values()[param];
    }

    @Override
    public String toChinese()
    {
        switch(this)
        {
            case emerg:
                return "系统不可用";
            case alert:
                return "必须立刻采取行动";
            case crit:
                return "关键事件";
            case err:
                return "错误事件";
            case warning:
                return "警告事件";
            case notice:
                return "普通但重要的事件";
            case info:
                return "有用的信息";
            case debug:
                return "调试信息";
        }
        return "None";
    }
}
