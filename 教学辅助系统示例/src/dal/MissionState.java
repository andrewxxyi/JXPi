package dal;

import cn.ijingxi.util.IjxEnum;

//Mission当前的完成情况
public enum MissionState implements IjxEnum {

    None,
    Waiting,
    Doing,
    Over,
    Pause,
    Cancel;

    //一个属性列是可以定义为一个实现了IjxEnum接口的枚举的，而枚举类型在保存到数据库中时是用整数进行存取的
    //所以系统需要在从数据库中读出这个整数后，知道该如何将其转换为相应的枚举值
    @Override
    public Object TransToORMEnum(Integer param) {
        return MissionState.values()[param];
    }

    //显示给前端的中文
    @Override
    public String toChinese() {
        switch (this) {
            case Waiting:
                return "等待执行";
            case Doing:
                return "正在执行";
            case Over:
                return "执行完毕";
            case Pause:
                return "暂停";
            case Cancel:
                return "取消";
        }
        return "None";
    }
}
