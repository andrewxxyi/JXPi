package cn.ijingxi.common.intelControl;

import cn.ijingxi.common.app.ObjTag;
import cn.ijingxi.common.orm.DB;
import cn.ijingxi.common.orm.ORMType;
import cn.ijingxi.common.orm.SelectSql;
import cn.ijingxi.common.orm.jxORMobj;
import cn.ijingxi.common.util.jxCompare;
import cn.ijingxi.common.util.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Category:用于设备自身的ID
 * TagOrder:用于设备类型
 *
 * Created by andrew on 16-3-5.
 */
public class FrontDevice extends ObjTag {

    public static void Init() throws Exception {
        InitClass(ORMType.FrontDevice.ordinal(), FrontDevice.class, "智能前端配置");
    }

    @Override
    protected void Init_Create(DB db) throws Exception {
        super.Init_Create(db);
        TagID = getTagID("智能前端配置");
    }


    private static Map<String, Integer> allDevType = new HashMap<>();
    private static Map<Integer, String> allDevType_IK = new HashMap<>();

    public static void addDevType(String devType, int typeID) throws Exception {
        synchronized (allDevType) {
            utils.Check(allDevType.containsKey(devType), "设备类型已存在:" + devType);
            utils.Check(allDevType_IK.containsKey(typeID), "设备类型已存在:" + typeID);
            allDevType.put(devType, typeID);
            allDevType_IK.put(typeID, devType);
        }
    }

    public static FrontDevice create(String devName, String devType) throws Exception {

        FrontDevice fd = get(devName);
        utils.Check(fd != null, "设备已存在:" + devName);
        utils.Check(!allDevType.containsKey(devType), "设备类型不存在:" + devType);
        fd = (FrontDevice) FrontDevice.Create(FrontDevice.class);
        fd.TagOrder = allDevType.get(devType);
        fd.Name = devName;
        return fd;

    }

    public static FrontDevice get(String devName) throws Exception {
        SelectSql s = new SelectSql();
        s.AddTable("ObjTag");
        s.AddContion("ObjTag", "TagID", jxCompare.Equal, getTagID("智能前端配置"));
        if (devName != null)
            s.AddContion("ObjTag", "Name", jxCompare.Equal, devName);
        return (FrontDevice) FrontDevice.Get(FrontDevice.class, s);
    }

    public static Queue<jxORMobj> search(String devName,String devType) throws Exception {

        utils.Check(devName == null || devName == "", "需要给出有效的设备名");
        SelectSql s = new SelectSql();
        s.AddTable("ObjTag");
        s.AddContion("ObjTag", "TagID", jxCompare.Equal, getTagID("智能前端配置"));
        if (devName != null)
            s.AddContion("ObjTag", "Name", jxCompare.Like, devName);
        if (devType != null)
            s.AddContion("ObjTag", "TagOrder", jxCompare.Equal, allDevType.get(devType));
        return FrontDevice.Select(FrontDevice.class, s);
    }

}
