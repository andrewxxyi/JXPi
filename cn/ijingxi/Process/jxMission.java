package cn.ijingxi.Process;

import cn.ijingxi.app.ObjTag;
import cn.ijingxi.app.People;
import cn.ijingxi.app.Relation;
import cn.ijingxi.app.RelationType;
import cn.ijingxi.orm.*;
import cn.ijingxi.util.jxCompare;

import java.util.Date;
import java.util.Queue;
import java.util.UUID;

/**
 * 任务，可以再次分解，可以是一个流程，也可以是一个简单的动作
 *
 * Created by andrew on 15-12-8.
 */
public class jxMission extends jxORMobj {


    @ORM(keyType= ORM.KeyType.PrimaryKey)
    public UUID ID;

    @ORM(Index=1)
    public String Name;

    @ORM(Descr="说明信息")
    public String Descr;

    @ORM(Index=2,Descr="用于两者之间的同步")
    public Date CreateTime;

    @ORM(Index=3,Descr="流程的当前状态")
    public InstanceState State;

    public void addMember(DB db,UUID peopleID,String peopleType) throws Exception {
        Relation rl=(Relation)Create(Relation.class);
        rl.ObjTypeID=ORMType.Mission.ordinal();
        rl.ObjID=ID;
        rl.TargetTypeID=ORMType.People.ordinal();
        rl.TargetID=peopleID;
        rl.Number=ObjTag.getTagID(peopleType);
        //多对多
        rl.RelType= RelationType.MultiToMulti;
        rl.Insert(db);
    }

    /**
     *
     * @param missionID
     * @param peopleType Relation的Number，用ObjTag进行转换
     * @param state
     * @return
     * @throws Exception
     */
    public static Queue<jxORMobj> listPeople(UUID missionID,String peopleType,InstanceState state) throws Exception {
        SelectSql s=new SelectSql();
        s.AddTable("jxMission");
        s.AddTable("People");
        s.AddTable("Relation");
        s.AddContion("Relation", "ObjID", jxCompare.Equal, missionID);
        if(peopleType!=null)
            s.AddContion("Relation", "Number", jxCompare.Equal, ObjTag.getTagID(peopleType));
        else{
            s.AddContion("Relation", "Number", jxCompare.GreateEqual, ObjTag.getTagID("创建者"));
            s.AddContion("Relation", "Number", jxCompare.LessEqual, ObjTag.getTagID("需了解者"));
        }
        if(state!=InstanceState.None)
            s.AddContion("jxMission", "State", jxCompare.Equal, state);

        s.AddContion("Relation","TargetID","People","ID");
        return People.Select(People.class,s);
    }
    public static Queue<jxORMobj> listMission(UUID peopleID,String peopleType,InstanceState state) throws Exception {
        SelectSql s=new SelectSql();
        s.AddTable("jxMission");
        s.AddTable("People");
        s.AddTable("Relation");
        s.AddContion("Relation", "TargetID", jxCompare.Equal, peopleID);
        if(peopleType!=null)
            s.AddContion("Relation", "Number", jxCompare.Equal, ObjTag.getTagID(peopleType));
        else{
            s.AddContion("Relation", "Number", jxCompare.GreateEqual, ObjTag.getTagID("创建者"));
            s.AddContion("Relation", "Number", jxCompare.LessEqual, ObjTag.getTagID("需了解者"));
        }
        if(state!=InstanceState.None)
            s.AddContion("jxMission", "State", jxCompare.Equal, state);

        s.AddContion("Relation","ObjID","jxMission","ID");
        return jxMission.Select(jxMission.class,s);
    }

    public static ORMID GetORMID(UUID ID)
    {
        return new ORMID(ORMType.Mission.ordinal(),ID);
    }

    @Override
    protected void Init_Create(DB db) throws Exception
    {
        ID=UUID.randomUUID();
        CreateTime=new Date();
        State=InstanceState.Doing;
    }

    public static void Init() throws Exception
    {
        InitClass(ORMType.Mission.ordinal(),jxMission.class,"任务");
        MissionTag.Init();
    }
    public static void CreateDB() throws Exception
    {
        CreateTableInDB(jxMission.class);
    }
}
