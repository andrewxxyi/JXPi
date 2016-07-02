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
 * Created by andrew on 15-9-19.
 */
public class Mission extends ObjTag {

    public static final String mk1 = "基础练习";
    public static final String mk2 = "问题灭除";
    public static final String mk3 = "项目实训";
    public static final String mk4 = "文体活动";
    public static final String mk5 = "讲座";

    /**
     * 活动的父亲可以是计划也可以是记录
     *
     * @param parentID
     * @return
     * @throws Exception
     */
    public static Mission New(int ObjTypeID, UUID parentID, Date from, Date to) throws Exception {
        Mission item = (Mission) Mission.Create(Mission.class);
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
        return ((MissionState) Trans.TransTojxEunm(MissionState.class, TagState)).toChinese();
    }

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

    public void setState(MissionState state) throws Exception {
        TagState = state.ordinal();
    }


    public static Queue<jxORMobj> listMission(UUID id) throws Exception {
        SelectSql s = new SelectSql();
        s.AddTable("ObjTag");
        s.AddContion("ObjTag", "ObjID", jxCompare.Equal, id);
        s.OrderBy = "Time";
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


    public static ORMID GetORMID(UUID ID) {
        return new ORMID(CommonObjTypeID.Mission, ID);
    }

    public static void Init() throws Exception {
        InitClass(CommonObjTypeID.Mission, Mission.class, "活动");
    }

    public static void CreateDB() throws Exception {
        if(!CreateTableInDB(Mission.class))return;
    }
}
