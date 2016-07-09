package cn.ijingxi.stub.general;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrew on 15-10-4.
 */
public class jxMatrix<TKey extends Comparable<TKey>, TValue>{
    //所有缓存对象的祖先点
    private node all=null;

    public jxMatrix(){
        all=new node();
        all.map=new HashMap<>();
    }

    public void put(Map<String,TKey> keys, TValue value) throws Exception {
        synchronized(this){
            int len=keys.size();
            int i=1;
            Map<String, Map<TKey, node>> list = all.map;
            for (Map.Entry<String, TKey> entry : keys.entrySet()) {
                String k = entry.getKey();
                TKey v = entry.getValue();
                Map<TKey, node> map = list.get(k);
                if(map==null){
                    map=new HashMap<>();
                    list.put(k,map);
                }
                node ooq = map.get(v);
                if(ooq==null){
                    ooq=new node();
                    map.put(v,ooq);
                }
                if(i==len) {
                    ooq.value = value;
                    return;
                }
                else{
                    //还没有结束，则沿着索引链继续查找
                    if(ooq.map==null)
                        ooq.map=new HashMap<>();
                    list=ooq.map;
                }
                i++;
            }
        }
    }
    public TValue get(Map<String,TKey> keys) throws Exception {
        synchronized(this){
            int len=keys.size();
            int i=1;
            Map<String, Map<TKey, node>> list = all.map;
            for (Map.Entry<String, TKey> entry : keys.entrySet()) {
                String k = entry.getKey();
                TKey v = entry.getValue();
                Map<TKey, node> map = list.get(k);
                if(map==null){
                    map=new HashMap<>();
                    list.put(k,map);
                }
                node ooq = map.get(v);
                if(ooq==null){
                    ooq=new node();
                    map.put(v,ooq);
                }
                if(i==len)
                    return ooq.value;
                else{
                    //还没有结束，则沿着索引链继续查找
                    if(ooq.map==null)
                        ooq.map=new HashMap<>();
                    list=ooq.map;
                }
                i++;
            }
            return null;
        }
    }

    class node{
        Map<String,Map<TKey,node>> map=null;
        //为了避免和对象缓存冲突，不能直接缓存对象，只能缓存ID
        TValue value=null;
    }

}
