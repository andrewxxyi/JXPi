package cn.ijingxi.common.orm;

import java.util.*;

/**
 *
 * 只针对基本不怎么经常变化，但需要频繁读取的对象，如权限检查
 *
 * 对jxORMobj对象进行多列索引型的统一操作
 *
 * Created by andrew on 15-9-27.
 */
public class jxORMTable {

    //只对第一个列建立索引，key1为属性名，key2为键值
    private Map<String,Map<Object,Queue<jxORMobj>>> indexs=new HashMap<>();
    //所有缓存对象
    private Queue<jxORMobj> allObj=new LinkedList<>();
    public void setData(Queue<jxORMobj> data) {
        allObj = data;
        indexs=new HashMap<>();
    }

    public Queue<jxORMobj> searchObj(Map<String,Object> keys) throws Exception {
        synchronized(this){
            boolean first=true;
            Queue<jxORMobj> list=null;
            for (Map.Entry<String, Object> entry : keys.entrySet()) {
                String k=entry.getKey();
                Object v=entry.getValue();
                if(first){
                    Map<Object,Queue<jxORMobj>> ix=indexs.get(k);
                    if(ix==null){
                        createIndex(k);
                        ix=indexs.get(k);
                    }
                    list=ix.get(v);
                    first=false;
                }
                else {
                    if(list!=null) {
                        Queue<jxORMobj> nl=new LinkedList<>();
                        for (jxORMobj obj : list) {
                            Object ov = jxORMobj.getFiledValue(obj, k);
                            if (v != null && ov != null && v.equals(ov))
                                nl.offer(obj);
                            else if (v == null && ov == null)
                                nl.offer(obj);
                        }
                        list=nl;
                    }
                    else
                        return null;
                }
            }
            return list;
        }
    }

    private void createIndex(String colName) throws Exception {
        synchronized (this){
            if(allObj==null)return;
            Map<Object,Queue<jxORMobj>> ix=indexs.get(colName);
            if(ix!=null)return;
            ix=new HashMap<>();
            indexs.put(colName,ix);
            for(jxORMobj o:allObj){
                Object v = jxORMobj.getFiledValue(o, colName);
                Queue<jxORMobj> q = ix.get(v);
                if(q==null){
                    q=new LinkedList<>();
                    ix.put(v,q);
                }
                q.offer(o);
            }
        }
    }


}
