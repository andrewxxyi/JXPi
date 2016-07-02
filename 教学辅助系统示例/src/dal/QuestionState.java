package dal;

import cn.ijingxi.util.IjxEnum;

public enum QuestionState implements IjxEnum {

    None,
    Waiting,
    Done;


    @Override
    public Object TransToORMEnum(Integer param) {
        return QuestionState.values()[param];
    }

    @Override
    public String toChinese() {
        switch (this) {
            case Waiting:
                return "等待解决";
            case Done:
                return "已经解决";
        }
        return "None";
    }
}
