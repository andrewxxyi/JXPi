package dal;

import cn.ijingxi.app.ObjTag;
import cn.ijingxi.orm.ORMID;
import cn.ijingxi.orm.ORMType;
import cn.ijingxi.orm.SelectSql;
import cn.ijingxi.orm.jxORMobj;
import cn.ijingxi.util.jxCompare;

import java.util.Queue;
import java.util.UUID;

/**
 * 参考下Mission和Exercise的说明
 *
 * 学生在学习过程中暴露出来的问题
 *
 * Created by andrew on 15-9-19.
 */
public class Question extends ObjTag {

    public static Question New(UUID peopleID) throws Exception {
        Question item = (Question) Question.Create(Question.class);
        item.ObjTypeID= ORMType.People.ordinal();
        item.ObjID=peopleID;
        item.TagID = ObjTag.getTagID("问题");
        item.Category = "问题";
        item.TagState=QuestionState.Waiting.ordinal();
        return item;
    }

    public static Queue<jxORMobj> listQuestion(UUID peopleID,QuestionState state) throws Exception {
        SelectSql s = new SelectSql();
        s.AddTable("ObjTag");
        s.AddContion("ObjTag", "TagID", jxCompare.Equal, ObjTag.getTagID("问题"));
        if (peopleID != null)
            s.AddContion("ObjTag", "ObjID", jxCompare.Equal, peopleID);
        if(state!=QuestionState.None)
            s.AddContion("ObjTag", "TagState", jxCompare.Equal, state);
        return Question.Select(Question.class, s);
    }


    public static ORMID GetORMID(UUID ID) {
        return new ORMID(CommonObjTypeID.Question, ID);
    }

    public static void Init() throws Exception {
        InitClass(CommonObjTypeID.Question, Question.class, "问题");
    }

    public static void CreateDB() throws Exception {
        if(!CreateTableInDB(Question.class))return;
    }
}
