package cn.ijingxi.common.app;

import cn.ijingxi.common.orm.DB;
import cn.ijingxi.common.orm.ORMType;
import cn.ijingxi.common.orm.SelectSql;
import cn.ijingxi.common.orm.jxORMobj;
import cn.ijingxi.common.util.Trans;
import cn.ijingxi.common.util.jxCompare;
import groovyx.gpars.dataflow.Select;

import java.util.*;

/**针对同一对象的不同版本,真实映射的其实是一个Tag和一组关系
 * Created by andrew on 15-9-19.
 */
public class Group extends ObjTag {

    private Map<Integer,Relation> members=new Hashtable<Integer,Relation>();

    public static void Init() throws Exception
    {
        InitClass(ORMType.Group.ordinal(),Group.class);
    }
    @Override
    protected void Init_Create(DB db) throws Exception
    {
        super.Init_Create(db);
        TagID=getTagID("组");
    }
    @Override
    protected void myInit(DB db) throws Exception{
        Queue<jxORMobj> list = Relation.listTarget(db, null, ORMType.Group.ordinal(), ID, RelationType.OneToMulti);
        for(jxORMobj o:list){
            Relation r=(Relation)o;
            int order=Trans.TransToInteger(r.getExtendValue("Info", "order"));
            members.put(order,r);
        }
    }

    public void setClsType(Class<?> cls) throws Exception {
        TagOrder=jxORMobj.getTypeID(cls);
    }
    public void setCategory(String category) throws Exception {
        Category=category;
    }

    /**
     * 添加一个版本，至于这个版本的创建时间、创建者之类的信息全部由目标对象的TO来保存
     * 版本组为简化order的跟踪，并不对其中的各个版本提供删除功能，版本的删除一是可以通过对关系的Noused，二是目标对象的Noused
     * @param cls
     * @param id
     * @return
     * @throws Exception
     */
    public void AddMember(Class<?> cls,UUID id) throws Exception {
        int tid=jxORMobj.getTypeID(cls);
        if(tid!=TagOrder)
            throw new Exception(String.format("AddVer类型不匹配：需要s%，送入s%", jxORMobj.getClassName(TagOrder), cls.getName()));
        Relation r=(Relation)Relation.Create(cls);
        r.RelType=RelationType.OneToMulti;
        r.ObjTypeID= ORMType.Group.ordinal();
        r.ObjID=ID;
        r.TargetTypeID=TagOrder;
        r.TargetID=id;
        int order=members.size()+1;
        r.setExtendValue("Info","order",order);
        r.Insert(null);
        members.put(order, r);
    }

    public jxORMobj getMember(int order) throws Exception {
        Relation r = members.get(order);
        if(r!=null)
            return GetByID(TagOrder,r.TargetID,null);
        return null;
    }
    public jxORMobj getLastMember() throws Exception {
        int order=members.size()+1;
        return getMember(order);
    }

    /**
     * 按降序排列，即最新的版本在最前面
     * @return
     */
    public Queue<jxORMobj> list() throws Exception {
        Queue<jxORMobj> l=new LinkedList<jxORMobj>();
        for(Integer i:members.keySet()){
            l.add(getMember(i));
        }
        return l;
    }


}
