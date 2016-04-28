package cn.ijingxi.system;

import cn.ijingxi.util.IDo;
import cn.ijingxi.util.IDo2;
import cn.ijingxi.util.jxTimer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrew on 15-12-6.
 */
public class jxAutoDeleteMap {
    private int period=0;
    private IDo2 dual=null;

    private Object lock=new Object();

    private Map<String,dmObj> map=new HashMap<>();

    /**
     *
     * @param Period_Second 以秒为单位的存活时间，超过自动删除
     * @param TimeOver Do(Object param1, Object param2)为超时送入（key，value）；
     */
    public jxAutoDeleteMap(int Period_Second, IDo2 TimeOver){
        period=Period_Second;
        dual=TimeOver;
    }

    public void put(String key,Object value){
        synchronized (lock) {
            dmObj o = map.remove(key);
            if (o != null)
                o.timer.cancel();
            else {
                o = new dmObj();
                o.key = key;
            }
            o.value=value;
            o.timer = jxTimer.DoAfter(period, new IDo() {
                @Override
                public void Do(Object param) throws Exception {
                    dmObj po = (dmObj) param;
                    map.remove(po.key);
                    if (dual != null)
                        dual.Do(po.key, po.value);
                }
            }, o);
            map.put(key, o);
        }
    }

    public Object remove(String key){
        synchronized (lock) {
            dmObj o = map.remove(key);
            if (o != null){
                o.timer.cancel();
                return o.value;
            }
            return null;
        }
    }

    /**
     * 刷新一下，时间重置
     * @param key
     */
    public void retick(String key){
        synchronized (lock) {
            dmObj o = map.get(key);
            if (o != null)
                o.timer.reTick();
        }
    }

    public Object get(String key){
        synchronized (lock) {
            dmObj o = map.get(key);
            if (o != null){
                o.timer.reTick();
                return o.value;
            }
            return null;
        }
    }


    private class dmObj{
        String key;
        Object value;
        jxTimer timer;
    }
}
