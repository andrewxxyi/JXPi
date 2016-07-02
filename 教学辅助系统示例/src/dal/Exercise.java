package dal;

import cn.ijingxi.app.ObjTag;
import cn.ijingxi.orm.ORM;
import cn.ijingxi.orm.ORMID;
import cn.ijingxi.orm.SelectSql;
import cn.ijingxi.orm.jxORMobj;
import cn.ijingxi.util.jxCompare;

import java.util.Queue;
import java.util.UUID;

/**
 * Created by andrew on 16-6-21.
 */
public class Exercise extends ObjTag {

    @ORM(keyType = ORM.KeyType.PrimaryKey, Descr = "如果没有PrimaryKey，则无法update")
    public UUID ID;

    @ORM(Index = 1, Descr = "做题人，ObjID是哪道题")
    public UUID PeopleID;

    @ORM(Index = 2, Descr = "属于哪个试卷")
    public UUID PaperID;

    //不需要，可放到Descr
    //@ORM(Descr = "学生做的答案")
    //public String Answer;

    @ORM(Descr = "该题做对了吗，true是错误")
    public Boolean Mistake;

    @ORM(Descr = "做本题所花费的时间，秒为单位，从显示出来到学生按下确定按钮")
    public int Duration;

    @ORM(Descr = "学生是否反复进行了选择，即学生就算做对也是比较犹疑的")
    public Boolean MultiSelect;

    @ORM(Descr = "选择的次数")
    public int SelectNumber;

    @ORM(Descr = "最后一次选择答案到点击确定按钮之间的时间")
    public int Delay;

    @ORM(Descr = "该题是学生胡乱猜的吗？目前暂不考虑，但未来可以结合视频扑捉来判断学生面部表情等来进行判断")
    public Boolean Guess;

    public static Queue<jxORMobj> list(String Category, int difficulty) throws Exception {
        SelectSql s = new SelectSql();
        s.AddTable("Exercise");
        s.AddContion("Exercise", "TagID", jxCompare.Equal, ObjTag.getTagID("测验练习"));
        s.AddContion("Exercise", "Category", jxCompare.Equal, Category);
        s.AddContion("Exercise", "TagOrder", jxCompare.GreateEqual, difficulty-15);
        s.AddContion("Exercise", "TagOrder", jxCompare.LessEqual, difficulty+15);
        s.OrderBy = "Time";
        return Exercise.Select(Exercise.class, s);
    }

    public static Exercise New(UUID paperID,UUID subjectID,UUID peopleID,String answer,Boolean mistake,int duration,Boolean multiSelect,int selectNumber,int delay) throws Exception {
        Exercise item = (Exercise) Exercise.Create(Exercise.class);
        item.TagID = ObjTag.getTagID("测验练习");
        item.ObjID=subjectID;
        item.PaperID=paperID;
        item.PeopleID=peopleID;
        item.Descr=answer;
        item.Mistake = mistake;
        item.Duration = duration;
        item.MultiSelect = multiSelect;
        item.SelectNumber = selectNumber;
        item.Delay = delay;
        return item;
    }

    public static ORMID GetORMID(UUID ID) {
        return new ORMID(CommonObjTypeID.Exercise, ID);
    }

    public static void Init() throws Exception {
        InitClass(CommonObjTypeID.Exercise, Exercise.class, "测验练习");
    }

    public static void CreateDB() throws Exception {
        if(!CreateTableInDB(Exercise.class))return;
    }
}
