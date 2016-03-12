package cn.ijingxi.common.app;

import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.util.jxCompare;
import cn.ijingxi.common.util.jxLog;
import cn.ijingxi.common.util.jxSparseTable;
import cn.ijingxi.common.util.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

/**
 * 用户权限管理：
 * Res：Action--people/role
 *
 * Created by andrew on 15-12-20.
 */
public class Right extends ObjTag {

    private static jxSparseTable<String,String,ActiveRight.Policy> defaultRight=
            new jxSparseTable<>();

    private static Object lock=new Object();
    private static jxORMTable cache=null;
    public static void reset(){
        synchronized (lock){
            cache=new jxORMTable();
            SelectSql s=new SelectSql();
            s.AddTable("ObjTag");
            try {
                s.AddContion("ObjTag", "TagID", jxCompare.GreateEqual, getTagID("权限"));
                Queue<jxORMobj> data = Select(Right.class, s);
                cache.setData(data);
            } catch (Exception e) {
                jxLog.error(e);
            }
        }
    }

    public static void addDefaultPolicy(String res,String active,ActiveRight.Policy policy){
        defaultRight.Add(res,active,policy);
    }

    public static boolean checkPeople(UUID peopleID,String res,String active,jxJson param) throws Exception {
        boolean rs=check(peopleID,res,active,param);
        if(rs)return true;
        Queue<UUID> rl = People.listAllRoleID(null, peopleID);
        for(UUID id:rl){
            if(check(id,res,active,param))
                return true;
        }
        return false;
    }

    /**
     *
     * @param objID
     * @param res
     * @param active
     * @param param
     * @return
     * @throws Exception
     */
    public static boolean check(UUID objID,String res,String active,jxJson param) throws Exception {
        jxLog.logger.debug(active + ":" + active);
        utils.Check(res == null, "资源名不能为空");
        //active则代表对整个资源的权限检查
        if (objID != null) {
            if (objID.compareTo(ObjTag.SystemID) == 0)
                //管理员具有无限权力
                return true;

            //如login
            Queue<jxORMobj> list = list(objID, res, active);
            if (list != null && list.size() == 1) {
                //先检查确定权限
                Right r = (Right) list.poll();
                return r.check(param);
            }
            if (active != null) {
                //如果是具体权限没检查通过则试图检查该对象对整个资源的操作权限
                list = list(objID, res, null);
                if (list != null && list.size() == 1) {
                    Right r = (Right) list.poll();
                    return r.check(param);
                }
            }
        }
        ActiveRight.Policy p = defaultRight.Search(res, active);
        //该操作的默认权限
        if (p == ActiveRight.Policy.Accept)
            return true;
        if (p == ActiveRight.Policy.Manager) {
            People peo = (People) People.GetByID(People.class, objID);
            if (peo != null && peo.PeopleType == ObjTag.getTagID("管理员"))
                return true;
        }
        //该资源的默认权限
        p = defaultRight.Search(res, null);
        return p == ActiveRight.Policy.Accept;
    }

    public static Queue<jxORMobj> list(UUID objID,String res,String active) throws Exception {
        synchronized (lock) {
            Map<String, Object> map = new HashMap<>();
            if(objID!=null)
                map.put("ObjID", objID);
            map.put("Category", res);
            map.put("Descr", active);
            return cache.searchObj(map);
        }
    }

    public static Right New(int objType,UUID objID,String rightName,String res,String active,
                            ActiveRight.Policy policy) throws Exception {
        Queue<jxORMobj> list = list(objID, res, active);
        if (list.size() == 1)
            return (Right) list.poll();
        Right right = (Right) Right.Create(Right.class);
        right.ObjTypeID = objType;
        right.ObjID = objID;
        right.TagOrder=policy.ordinal();
        right.Category=res;
        right.Name=rightName;
        right.Descr=active;
        return right;
    }
    public void save(DB db) throws Exception {
        synchronized (lock) {
            Insert(db);
            reset();
        }
    }
    public void del(DB db) throws Exception {
        synchronized (lock) {
            Delete(db);
            reset();
        }
    }

    public boolean check(jxJson param){
        if(ActiveRight.Policy.Accept.ordinal()==TagOrder)
            try {
                jxJson json=getExtendJSON("Info");
                if(json!=null)
                    for(jxJson sub:json){
                        String k=sub.getName();
                        String iv = (String) sub.getValue();
                        String o= (String) param.GetSubValue(k);
                        if(iv!=null&&o!=null){
                            if(iv.compareTo(o)==0)
                                continue;
                        }
                        return false;
                    }
                return true;
            } catch (Exception e) {
                jxLog.error(e);
            }
        return false;
    }

    public String getRes(){return Category;}
    public String getAction(){return Descr;}

    public String dispRight() throws Exception {
        String rs=null;
        jxORMobj obj=jxORMobj.GetByID(ObjTypeID,ObjID);
        rs=jxORMobj.getTypeName(ObjTypeID)+":";
        rs= obj.getName();
        if(TagOrder==ActiveRight.Policy.Accept.ordinal())
            rs+=" 允许 执行 ";
        else if(TagOrder==ActiveRight.Policy.Refuse.ordinal())
            rs+=" 拒绝 执行 ";
        return rs+Category+":"+Name;
    }

    public static ORMID GetORMID(UUID ID)
    {
        return new ORMID(ORMType.Right.ordinal(),ID);
    }
    public static void Init() throws Exception{
        InitClass(ORMType.Right.ordinal(), Right.class,"权限");
        reset();
    }
    @Override
    protected void Init_Create(DB db) throws Exception
    {
        super.Init_Create(db);
        TagID=getTagID("权限");
    }

}
