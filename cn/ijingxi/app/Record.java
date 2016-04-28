package cn.ijingxi.app;

import cn.ijingxi.orm.SelectSql;
import cn.ijingxi.orm.jxORMobj;
import cn.ijingxi.util.jxCompare;

import java.util.Queue;
import java.util.UUID;

/**
 * Created by andrew on 15-9-20.
 */
public class Record extends ObjTag {

    public static Record New(int typeID,UUID objID,String Category,String Descr) throws Exception {
        Record item = (Record)Record.Create(Record.class);
        item.ObjTypeID=typeID;
        item.ObjID=objID;
        item.TagID=ObjTag.getTagID("记录");
        item.Category=Category;
        item.Descr=Descr;
        return item;
    }

    public void setInfo(String Purpose,Object value) throws Exception {
        setExtendValue("Info", Purpose, value);
    }

    public Queue<jxORMobj> List(UUID objID) throws Exception {
        SelectSql s=new SelectSql();
        s.AddTable("ObjTag");
        s.AddContion("ObjTag", "ObjID", jxCompare.Equal, objID);
        return Select(Record.class,s);
    }
    public Queue<jxORMobj> List(UUID objID,String Category) throws Exception {
        SelectSql s=new SelectSql();
        s.AddTable("ObjTag");
        s.AddContion("ObjTag", "ObjID", jxCompare.Equal, objID);
        s.AddContion("ObjTag", "Category", jxCompare.Equal, Category);
        return Select(Record.class,s);
    }



}
