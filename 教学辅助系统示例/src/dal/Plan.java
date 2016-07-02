package dal;

import cn.ijingxi.app.ObjTag;
import cn.ijingxi.orm.ORMID;

import java.util.Date;
import java.util.UUID;

/**
 * Created by andrew on 15-9-19.
 */
public class Plan extends ObjTag {

    public static Plan New(Date date) throws Exception {
        Plan item = (Plan) Plan.Create(Plan.class);
        item.TagID = ObjTag.getTagID("课程计划");
        item.Category = "课程计划";
        item.Time = date;
        return item;
    }
    

    public static ORMID GetORMID(UUID ID) {
        return new ORMID(CommonObjTypeID.Plan, ID);
    }

    public static void Init() throws Exception {
        InitClass(CommonObjTypeID.Plan, Plan.class, "课程计划");
    }

    public static void CreateDB() throws Exception {
        if(!CreateTableInDB(Plan.class))return;
    }
}
