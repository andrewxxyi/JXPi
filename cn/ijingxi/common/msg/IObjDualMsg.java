package cn.ijingxi.common.msg;

import cn.ijingxi.common.orm.jxORMobj;

/**
 * Created by andrew on 15-9-4.
 */
public interface IObjDualMsg {
    /**
     *一般来说，消息处理应组成链条，分别处理需要处理的消息，其它消息丢给链条后继的处理
     * @param obj
     * @param msg
     * @return 返回true代表已处理
     */
    boolean Dual(jxORMobj obj,Message msg);

    /**
     * 对需要接收的信息进行注册
     * @param obj
     */
    void Register(jxORMobj obj);
}
