package dal;

import cn.ijingxi.app.ObjTag;
import cn.ijingxi.orm.ORMID;
import cn.ijingxi.orm.SelectSql;
import cn.ijingxi.orm.jxJson;
import cn.ijingxi.orm.jxORMobj;
import cn.ijingxi.util.Trans;
import cn.ijingxi.util.jxCompare;

import java.util.*;

/**
 * ORM数据类的定义--活动
 * 其继承自ObjTag，主要是为了简化数据库
 *
 * 活动就是一个模块的具体内容的记录，一天的教学计划包括了5个模块都是要做什么练习的要求，这是对一个班的；
 * 而每个人根据自己班级的教学计划录入自己在每个模块都具体是如何执行的
 *
 * Created by andrew on 15-9-19.
 */
public class Mission extends ObjTag {

    //每天安排了5个教学模块，这些属于业务层面的考虑
    public static final String mk1 = "基础练习";
    public static final String mk2 = "问题灭除";
    public static final String mk3 = "项目实训";
    public static final String mk4 = "文体活动";
    public static final String mk5 = "讲座";

    /**
     * 创建一个活动
     * 活动的父亲可以是计划也可以是记录
     *
     * @param parentID
     * @return
     * @throws Exception
     */
    public static Mission New(int ObjTypeID, UUID parentID, Date from, Date to) throws Exception {
        //ORM数据类的创建必须用自己继承来的Create函数（在ORM最基础的jxORMobj中定义）
        Mission item = (Mission) Mission.Create(Mission.class);
        //下面这些属性都是在ObjTag中定义的，作为子类直接用就可以了
        item.ObjTypeID = ObjTypeID;
        item.ObjID = parentID;
        item.TagID = ObjTag.getTagID("活动");
        item.Category = "活动";
        item.TagState = MissionState.Waiting.ordinal();
        item.Time = from;
        item.ToTime = to;
        return item;
    }

    public String getState() throws Exception {
        //自定义的枚举按这种方式就可以获得自定义的中文意思
        //TagState是相应的一个整数，这里是将其转换为相对应的中文含义
        return ((MissionState) Trans.TransTojxEunm(MissionState.class, TagState)).toChinese();
    }

    //在录入执行情况时可以通过手机拍照然后上传到服务器，但目前还没有实现
    public void addFile(String fileName) throws Exception {
        synchronized (this) {
            jxJson json = jxJson.GetObjectNode("sub");
            json.AddValue("fn", fileName);
            addExtendArraySubNode("Info", json);
            TagOrder++;
        }
    }

    public void delFile(String fileName) throws Exception {
        synchronized (this) {
            Map<String, String> Keys = new HashMap<>();
            Keys.put("fn", fileName);
            int num = delExtendArraySubNode("Info", Keys);
            TagOrder = TagOrder - num;
        }
    }

    public String[] listFile() throws Exception {
        List<jxJson> list = getExtendArrayList("info", null);
        String[] rs = new String[list.size()];
        int i = 0;
        for (jxJson json : list)
            rs[i++] = (String) json.getSubObjectValue("fn");
        return rs;
    }

    //注意：函数在修改后没有保存到数据库中，需要在业务代码中自己做一个保存动作
    public void setState(MissionState state) throws Exception {
        TagState = state.ordinal();
    }


    public static Queue<jxORMobj> listMission(UUID id) throws Exception {
        //对于ORM数据类，如何从数据库中查询数据
        //由于java不能向.net那样开放兰姆达表达式的解析，所以就无法象.net中那样的用兰姆达表达式实现对数据库的访问，而必须以这种方案
        //进行数据库的访问，
        //.net中用兰姆达表达式对数据库的访问最大的好处是可以让编译器在编译期间就完成数据类型的检查，而现在的这种方式只能推迟到
        //运行时，如果有问题就太晚了
        SelectSql s = new SelectSql();
        //指出想从哪些表中查询数据
        s.AddTable("ObjTag");
        //指定查询条件：计划或是当天的工作汇报
        s.AddContion("ObjTag", "ObjID", jxCompare.Equal, id);
        //指定用于排序的属性
        s.OrderBy = "Time";
        //读数据库并将查询到的数据转换为相应的ORM数据对象
        return Mission.Select(Mission.class, s);
    }

    public static Queue<jxORMobj> listMission(int tagid,Date d) throws Exception {
        SelectSql s = new SelectSql();
        s.AddTable("ObjTag");
        s.AddContion("ObjTag", "TagID", jxCompare.Equal, tagid);
        s.AddContion("ObjTag", "Time", jxCompare.Equal, d);
        return Mission.Select(Mission.class, s);
    }

    public static Mission getMission(int tagid,Date day,int order) throws Exception {
        SelectSql s = new SelectSql();
        s.AddTable("ObjTag");
        s.AddContion("ObjTag", "TagID", jxCompare.Equal, tagid);
        s.AddContion("ObjTag", "Time", jxCompare.Equal, day);
        s.AddContion("ObjTag", "TagOrder", jxCompare.Equal, order);
        //s.OrderBy = "Time";
        Queue<jxORMobj> list = Mission.Select(Mission.class, s);
        if(list!=null&&list.size()==1)
            return (Mission) list.poll();
        return null;
    }


    //下面的这三个静态函数是必须，没特殊情况照抄即可
    public static ORMID GetORMID(UUID ID) {
        return new ORMID(CommonObjTypeID.Mission, ID);
    }

    public static void Init() throws Exception {
        InitClass(CommonObjTypeID.Mission, Mission.class, "活动");
    }

    public static void CreateDB() throws Exception {
        //首先试图在没有创建Mission数据表时就自动创建Mission数据表
        if(!CreateTableInDB(Mission.class))return;
        //然后可以创建一些在第一次创建数据表是就需要创建的数据，如
        //Mission m=(Mission) Mission.Create(Mission.class);
        //m.Name="test";
        //m.Insert();
    }
}
