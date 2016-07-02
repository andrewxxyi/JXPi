package dal;

import cn.ijingxi.util.IjxEnum;

public enum MissionState implements IjxEnum {

    None,
    Waiting,
    Doing,
    Over,
    Pause,
    Cancel;


    @Override
    public Object TransToORMEnum(Integer param) {
        return MissionState.values()[param];
    }

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
