package cn.ijingxi.common.orm;

import cn.ijingxi.common.util.LinkNode;
import cn.ijingxi.common.util.jxCompare;
import cn.ijingxi.common.util.jxLink;
import cn.ijingxi.common.util.utils;

import java.util.*;

/**
 * 对jxORMobj对象进行多列索引型的统一操作
 *
 * Created by andrew on 15-9-27.
 */
public class jxORMTable {

    //容量为一百万
    private final int MaxCount=1000000000;

    private ORMClassAttr clsAttr=null;
    //是否需要缓存？一般只有系统中唯一一个组件时才可以进行缓存，而且缓存的管理比较复杂
    private boolean cache=false;
    public void setCache(boolean cache) throws Exception {
        utils.Check(cache&&!clsAttr.canCache,String.format("类型：%s 不支持缓存！",clsAttr.ClsName));
        this.cache=cache;
    }

    //所有缓存对象的祖先点
    private ListOrObject all=new ListOrObject();


    private jxLink<UUID,ListOrObject> cacheLink=new jxLink<UUID,ListOrObject>();

    public jxORMTable(String clsName){
        clsAttr=jxORMobj.getClassAttr(clsName);
        all.map=new HashMap<>();
    }
    public jxORMTable(Class<?> cls){
        clsAttr=jxORMobj.getClassAttr(cls);
        all.map=new HashMap<>();
    }
    /**
     * 根据索引值读取对象，兼容了<key1>、<key1,key2>
     * @param keys 以<列名：值>所组成的值对的索引值
     * @return 对象list
     * @throws Exception
     */
    public Queue<jxORMobj> searchObj(Map<String,Object> keys) throws Exception {
        DB db=JdbcUtils.GetDB();
        Queue<jxORMobj> map = searchObj(db, keys);
        db.Release();
        return map;
    }
    public Queue<jxORMobj> searchObj(DB db,Map<String,Object> keys) throws Exception {
        if(!cache)
            return getFromDB(db, keys);
        synchronized(all){
            int len=keys.size();
            int i=1;
            ListOrObject pn=all;
            Map<String, Map<Object, ListOrObject>> list = all.map;
            for (Map.Entry<String, Object> entry : keys.entrySet()) {
                String k = entry.getKey();
                Object v = entry.getValue();
                Map<Object, ListOrObject> map = list.get(k);
                if(map==null){
                    map=new HashMap<>();
                    list.put(k,map);
                }
                ListOrObject ooq = map.get(v);
                if(ooq==null){
                    ooq=new ListOrObject();
                    map.put(v,ooq);
                    ooq.parent=pn;
                }
                if(i==len){
                    Queue<jxORMobj> rs=null;
                    //最终的对象，可以兼容，<k1>,<k1,k2>两种情况
                    if(ooq.objects==null) {
                        //如果没有数据则试图从数据库中读取
                        rs = getFromDB(db, keys);
                        ooq.objects=new LinkedList<>();
                        for(jxORMobj o:rs)
                            ooq.objects.add(o.getID());
                        cacheLink.offer(null,ooq);
                        ooq.node=cacheLink.getLast();
                        ooq.totalNum=ooq.objects.size();
                        ListOrObject parent = ooq.parent;
                        while (parent!=null){
                            parent.totalNum+=ooq.totalNum;
                            parent=parent.parent;
                        }
                        if(all.totalNum>=MaxCount)
                            clear();
                    }
                    else{
                        rs=new LinkedList<>();
                        for(UUID id:ooq.objects)
                            rs.add(jxORMobj.GetByID(db,clsAttr.clsType,id,null));
                    }
                    cacheLink.moveToTail(ooq.node);
                    return rs;
                }
                else{
                    //还没有结束，则沿着索引链继续查找
                    if(ooq.map==null)
                        ooq.map=new HashMap<>();
                    list=ooq.map;
                    pn=ooq;
                }
                i++;
            }
            return null;
        }
    }

    public jxORMobj getObj(Map<String,Object> keys) throws Exception {
        Queue<jxORMobj> list = searchObj(keys);
        if(list!=null&&list.size()==1)
            return list.peek();
        return null;
    }
    public jxORMobj getObj(DB db,Map<String,Object> keys) throws Exception {
        Queue<jxORMobj> list = searchObj(db, keys);
        if(list!=null&&list.size()==1)
            return list.peek();
        return null;
    }

    /**
     * 执行清理任务
     */
    public void clear(){
        while (all.totalNum>=MaxCount)
            cloneOne();
    }
    private void cloneOne(){
        ListOrObject op = cacheLink.poll();
        ListOrObject p=op.parent;
        while (p!=null){
            p.totalNum-=op.totalNum;
            if(p.totalNum==0)
                p.map=null;
            p=p.parent;
        }
        op.totalNum=0;
        op.objects=null;
    }

    public Queue<jxORMobj> getFromDB(DB db,Map<String,Object> keys) throws Exception {
        SelectSql s=new SelectSql();
        s.AddTable(clsAttr.ClsName, null);
        for (Map.Entry<String, Object> entry : keys.entrySet()) {
            s.AddContion(clsAttr.ClsName, entry.getKey(), jxCompare.Equal, entry.getValue());
        }
        return jxORMobj.Select(db, clsAttr.clsType, s, cache, null);
    }



    class ListOrObject{
        int totalNum=0;
        Map<String,Map<Object,ListOrObject>> map=null;
        //为了避免和对象缓存冲突，不能直接缓存对象，只能缓存ID
        Queue<UUID> objects=null;
        ListOrObject parent=null;
        //只有对象才需要
        LinkNode<UUID,ListOrObject> node=null;
    }

}
