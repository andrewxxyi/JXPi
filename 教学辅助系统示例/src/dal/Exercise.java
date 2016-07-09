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
 * 练习，一个试卷（Paper）由多个试题(Subject)组成，而学生每做一道题目，则相应的生成一个实际的练习。即学生做的结果
 *
 * Created by andrew on 16-6-21.
 */
public class Exercise extends ObjTag {

    //Exercise继承自ObjTag，在数据库中每一个Exercise对象的数据是由两部分组成的，一部分自然是在Exercise表
    //但另一部分则保存在ObjTag表中，这两者就是通过这两个表中都有的ID进行关联才能获得完整的Exercise对象的所有数据
    //而一个ORM数据类，如果是代表实体的则应只定义一个PrimaryKey
    @ORM(keyType = ORM.KeyType.PrimaryKey, Descr = "如果没有PrimaryKey，则无法update")
    public UUID ID;

    //为便于按我做了哪些题目进行查找，所以需要为PeopleID建立一个索引，某属性用Index进行了标记则系统在创建数据表时会自动
    //创建相应的索引，如果索引号相同，则会创建一个多列索引
    @ORM(Index = 1, Descr = "做题人，ObjID是哪道题")
    public UUID PeopleID;

    @ORM(Index = 2, Descr = "属于哪个试卷")
    public UUID PaperID;

    //不需要，可放到Descr
    //@ORM(Descr = "学生做的答案")
    //public String Answer;

    //ORM数据类定义自己需要的属性时，就使用自己需要的数据类型就可以了，系统会自动完成java语言中的类型和数据库类型之间的转换
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

    //在实现的时候，实际是将学生做题时的所有操作（学生如何选择答案的，包括时间点）都进行了捕获，
    //所以这几个属性其实都没有必要了，完全可以通过对学生选择答案的行为分析得出更为详细的判断结果
    @ORM(Descr = "该题是学生胡乱猜的吗？目前暂不考虑，但未来可以结合视频扑捉来判断学生面部表情等来进行判断")
    public Boolean Guess;

    //根据指定的领域（线程、网络等）和难度（(0，100】，100是最难）来选择题目
    public static Queue<jxORMobj> list(String Category, int difficulty) throws Exception {
        //可参考Mission中的说明
        SelectSql s = new SelectSql();
        s.AddTable("Exercise");
        s.AddContion("Exercise", "TagID", jxCompare.Equal, ObjTag.getTagID("测验练习"));
        s.AddContion("Exercise", "Category", jxCompare.Equal, Category);
        //将上下30范围的题目都挑选出来
        s.AddContion("Exercise", "TagOrder", jxCompare.GreateEqual, difficulty-15);
        s.AddContion("Exercise", "TagOrder", jxCompare.LessEqual, difficulty+15);
        s.OrderBy = "Time";
        return Exercise.Select(Exercise.class, s);
    }

    public static Exercise New(UUID paperID,UUID subjectID,UUID peopleID,String answer,Boolean mistake,int duration,Boolean multiSelect,int selectNumber,int delay) throws Exception {
        Exercise item = (Exercise) Exercise.Create(Exercise.class);
        //这里面的属性有些是在ObjTag中进行的定义，但不需要多关注
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
