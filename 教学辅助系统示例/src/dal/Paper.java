package dal;

import cn.ijingxi.app.ObjTag;
import cn.ijingxi.app.Relation;
import cn.ijingxi.app.RelationType;
import cn.ijingxi.orm.*;
import cn.ijingxi.util.Trans;
import cn.ijingxi.util.jxCompare;
import cn.ijingxi.util.utils;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

/**
 * 参考下Mission和Exercise的说明
 *
 * 试卷
 *
 * 试卷和题目是一对多的情况，也就是一个试卷是由多个题目组成的
 *
 * Created by andrew on 16-6-21.
 */
public class Paper extends ObjTag {

    @ORM(keyType = ORM.KeyType.PrimaryKey, Descr = "如果没有PrimaryKey，则无法update")
    public UUID ID;

    @ORM(Index = 1, Descr = "出卷人")
    public UUID IssueID;

    //为了支持个性化教学，可以是给一个班的卷子，但每个人做的题目是不一样的，因为在根据这个卷子挑选试题的时候
    //可以根据学生的程度个性化的选择不同的难度
    @ORM(Index = 2, Descr = "如果是copy的卷子，则为复制自的那个试卷")
    public UUID ParentID;

    public static Queue<jxORMobj> listPaper(Date day) throws Exception {
        SelectSql s = new SelectSql();
        s.AddTable("Paper");
        s.AddContion("Paper", "TagID", jxCompare.Equal, ObjTag.getTagID("试卷"));
        s.AddContion("Paper", "Time", jxCompare.Equal, day);
        return Paper.Select(Paper.class, s);
    }
    //一天可以做多个测验，选哪个
    public static Paper getPaper(Date day, int order) throws Exception {
        SelectSql s = new SelectSql();
        s.AddTable("Paper");
        s.AddContion("Paper", "TagID", jxCompare.Equal, ObjTag.getTagID("试卷"));
        s.AddContion("Paper", "Time", jxCompare.Equal, day);
        s.AddContion("Paper", "TagOrder", jxCompare.Equal, order);
        //s.OrderBy = "Time";
        Queue<jxORMobj> list = Paper.Select(Paper.class, s);
        if(list!=null&&list.size()==1)
            return (Paper) list.poll();
        return null;
    }
    /**
     * 组卷
     * 实际的出题过程，根据出题人的意图（领域和难度）从题库中挑选合适的题目
     */
    public Queue<jxORMobj> takeSubject() throws Exception {
        Queue<jxORMobj> rs = new LinkedList<>();
        int perCnum = 0;
        int tn = 0;
        //本试卷所包含的总题目数
        int totalSubjectNumber = Trans.TransToInteger(getExtendValue("Info", "totalSubjectNumber"));
        utils.checkAssert(totalSubjectNumber > 0, "题目数量应大于0");
        //根据所给出的领域和难度先从题库中挑出所有符合条件的题目
        //出题人可以选择多个覆盖领域（线程、网络等），用空格分隔开
        String[] cs = utils.StringSplit(Category, " ");
        //根据难度从每个领域选出来的题目可能很多，暂时保存一下
        Queue<Queue<jxORMobj>> al = new LinkedList<>();
        //每个领域需要出的平均的题目数量
        perCnum = totalSubjectNumber / cs.length;
        for (String c : cs) {
            Queue<jxORMobj> list = Subject.list(c, Number);
            //如果这个领域中的数量小于平均出题数，则全部选中
            if (list.size() <= perCnum) {
                for (jxORMobj obj : list)
                    rs.offer(obj);
            } else {
                //否则随机挑选题目
                for (int i = 0; i < perCnum; i++) {
                    jxORMobj o = utils.randomGet(list);
                    rs.offer(o);
                    list.remove(o);
                }
                //挑剩下的题目暂时保存到al中，等后面还要继续挑
                al.offer(list);
            }
        }
        //每领域挑perCnum有可能还差几题需要补足
        if (rs.size() < totalSubjectNumber) {
            //题目数量不够，需要补足
            int diff = totalSubjectNumber - rs.size();
            int len = al.size();
            Object[] arr = new Object[len];
            int i = 0;
            while (diff > 0) {
                //思路是从之前剩下的各领域中依次进行挑选
                Object obj = arr[i];
                if (obj == null) {
                    obj = al.poll();
                    arr[i] = obj;
                }
                Queue<jxORMobj> list = (Queue<jxORMobj>) obj;
                if (list.size() > 0) {
                    rs.offer(utils.randomGet(list));
                    diff--;
                    if (diff <= 0)
                        break;
                }
                i++;
                if (i >= len)
                    i = 0;
            }
        }
        //将选出的实际试题添加到试卷中
        for (jxORMobj obj:rs){
            Subject s=(Subject)obj;
            addSubject(s);
        }
        return rs;
    }

    //添加试题，其实就是创建一个一（试卷）对多（题目）的关系
    public void addSubject(Subject s) throws Exception {
        Relation.addRela(CommonObjTypeID.Paper,ID,CommonObjTypeID.Subject,s.ID, RelationType.OneToMulti);
    }

    //取得本试卷所包含的所有试卷的关系信息
    public Queue<jxORMobj> listSubject() throws Exception {
        return Relation.listTarget(null,CommonObjTypeID.Paper, ID, RelationType.OneToMulti);
    }

    public float getDifficulty(){return Number;}
    public int getTotalSubjectNumber() throws Exception {return Integer.parseInt(getExtendValue("Info","totalSubjectNumber"));}
    public boolean getIndividuation() throws Exception {return Boolean.parseBoolean(getExtendValue("Info","individuation"));}

    //这里创建的只是出题人的一个试卷要求，具体包括哪些试题，要通过组卷过程来完成
    public static Paper New(String name,UUID issueID, int objTypeid, UUID objid, boolean individuation, String Category, float difficulty,int totalSubjectNumber) throws Exception {
        utils.checkAssert(totalSubjectNumber > 0, "题目数量应对于0");
        utils.checkAssert(Category != null && Category.length()>0, "应给出有效的组卷领域");
        utils.checkAssert(difficulty > 00 && difficulty <= 100, "难度的有效范围是（0,100】:"+difficulty);
        Paper item = (Paper) Paper.Create(Paper.class);
        item.TagID = ObjTag.getTagID("试卷");
        item.Name=name;
        item.IssueID = issueID;
        //试卷是出给谁的：整个班的，还是自己的自测卷
        item.ObjTypeID = objTypeid;
        item.ObjID = objid;
        //这里的Category应是一串以空格分隔开来的
        item.Category = Category;
        //Number保存的是难度
        item.Number = difficulty;
        //如果是对一个班级出题，则系统会针对班级里的每个人出一套符合他的特点的试卷
        item.setExtendValue("Info", "individuation", individuation);
        //总题目数量
        item.setExtendValue("Info", "totalSubjectNumber", totalSubjectNumber);
        return item;
    }

    /**
     * 如果是针对班级出的个性化试卷，则在给班级中的每个人个性化的针对该生单独组卷
     *
     * @param studentID
     * @return
     * @throws Exception
     */
    public Paper copy(UUID studentID) throws Exception {
        utils.checkAssert(ObjTypeID == ORMType.Department.ordinal() && getIndividuation(), "只能是对一个班级的出的个性化试卷才需要进行复制");
        Paper item = (Paper) Paper.Create(Paper.class);
        item.TagID = TagID;
        item.ObjTypeID = ORMType.People.ordinal();
        item.ObjID = studentID;
        //从本试卷复制过去的子试卷其父id应设为我自己的id
        item.ParentID = ID;
        //这里的Category应是一串以空格分隔开来的
        item.Category = Category;
        item.Name=Name;
        item.Number = Number;
        item.Time=Time;
        //如果是对一个班级出题，则系统会针对班级里的每个人出一套符合他的特点的试卷
        item.TagOrder = 1;
        return item;
    }

    @Override
    protected void Init_Create(DB db) throws Exception {
        super.Init_Create(db);
        ID = super.ID;
    }

    public static ORMID GetORMID(UUID ID) {
        return new ORMID(CommonObjTypeID.Paper, ID);
    }

    public static void Init() throws Exception {
        InitClass(CommonObjTypeID.Paper, Paper.class, "测验试卷");
    }

    public static void CreateDB() throws Exception {
        if (!CreateTableInDB(Paper.class)) return;
    }
}
