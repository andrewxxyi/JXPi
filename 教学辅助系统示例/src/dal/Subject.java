package dal;

import cn.ijingxi.app.ObjTag;
import cn.ijingxi.orm.ORMID;
import cn.ijingxi.orm.SelectSql;
import cn.ijingxi.orm.jxORMobj;
import cn.ijingxi.util.jxCompare;

import java.util.Queue;
import java.util.UUID;

/**
 * 参考下Mission和Exercise的说明
 *
 * 题目不管单选多选、难度大小，分数是不重要的，可假定为一题一分
 *
 * 试卷和题目是一对多的情况，也就是一个试卷是由多个题目组成的
 *
 * Created by andrew on 16-6-21.
 */
public class Subject extends ObjTag {

    public static Queue<jxORMobj> list(String Category, float difficulty) throws Exception {
        SelectSql s = new SelectSql();
        s.AddTable("ObjTag");
        s.AddContion("ObjTag", "TagID", jxCompare.Equal, ObjTag.getTagID("测验题目"));
        s.AddContion("ObjTag", "Category", jxCompare.Equal, Category);
        s.AddContion("ObjTag", "Number", jxCompare.GreateEqual, difficulty - 15);
        s.AddContion("ObjTag", "Number", jxCompare.LessEqual, difficulty + 15);
        s.OrderBy = "Time";
        return Subject.Select(Subject.class, s);
    }

    public boolean getMultiSelect() throws Exception {
        return Boolean.parseBoolean(getExtendValue("Info", "multiSelect"));
    }

    public static Subject New(String Category, String Descr, int answerTotal, String answer, float difficulty, boolean multiSelect) throws Exception {
        Subject item = (Subject) Subject.Create(Subject.class);
        item.TagID = ObjTag.getTagID("测验题目");
        item.Category = Category;
        item.Number = difficulty;
        //题目内容
        item.Descr = Descr;
        //答案
        item.Name = answer;
        //需要罗列出来的答案数量，即需要显示出来的ABCD
        //item.setExtendValue("Info", "answerTotal", answerTotal);
        item.setExtendValue("Info", "answerTotal", 8);
        //单选还是多选
        item.setExtendValue("Info", "multiSelect", multiSelect);
        item.Insert();
        return item;
    }

    public static ORMID GetORMID(UUID ID) {
        return new ORMID(CommonObjTypeID.Subject, ID);
    }

    public static void Init() throws Exception {
        InitClass(CommonObjTypeID.Subject, Subject.class, "测验题目");
    }

    public static void CreateDB() throws Exception {
        if (!CreateTableInDB(Subject.class)) return;
    }
}
