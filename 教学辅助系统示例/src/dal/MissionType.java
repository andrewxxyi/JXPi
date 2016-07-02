package dal;

import cn.ijingxi.util.IjxEnum;

public enum  MissionType  implements IjxEnum {

    None,
    Grounding,
    Question,
    Forum,
    RPjTraining,
    Movement,
    Rest,
    Leave,
    Coding,
    Testing;


    @Override
    public Object TransToORMEnum(Integer param) {
        return MissionType.values()[param];
    }

    @Override
    public String toChinese() {
        switch (this) {
            case Grounding:
                return "基础训练";
            case Question:
                return "问题消灭";
            case Forum:
                return "讲座";
            case RPjTraining:
                return "项目实训";
            case Movement:
                return "运动";
            case Rest:
                return "休息";
            case Leave:
                return "请假";
            case Coding:
                return "敲代码";
            case Testing:
                return "测验";
        }
        return "None";
    }
}
