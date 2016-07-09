package dal;

import cn.ijingxi.app.ObjTag;
import cn.ijingxi.orm.ORMID;
import cn.ijingxi.orm.ORMType;

import java.util.Date;
import java.util.UUID;

/**
 * 参考下Mission和Exercise的说明
 *
 * Created by andrew on 15-9-19.
 */
public class Schedule extends ObjTag {

    public static Schedule New(UUID peopleID, Date date) throws Exception {
        Schedule item = (Schedule) Schedule.Create(Schedule.class);
        item.ObjTypeID = ORMType.People.ordinal();
        item.ObjID = peopleID;
        item.TagID = ObjTag.getTagID("日程");
        item.Category = "日程";
        item.Time = date;
        return item;
    }


    public static ORMID GetORMID(UUID ID) {
        return new ORMID(CommonObjTypeID.Schedule, ID);
    }

    public static void Init() throws Exception {
        InitClass(CommonObjTypeID.Schedule, Schedule.class, "日程");
    }

    public static void CreateDB() throws Exception {
        if (!CreateTableInDB(Schedule.class)) return;
    }
}
