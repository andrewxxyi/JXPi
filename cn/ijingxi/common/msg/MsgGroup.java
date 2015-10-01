package cn.ijingxi.common.msg;

import cn.ijingxi.common.app.People;
import cn.ijingxi.common.app.Relation;
import cn.ijingxi.common.app.RelationType;
import cn.ijingxi.common.app.TopSpace;
import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.util.jxCompare;

import java.util.Date;
import java.util.Queue;
import java.util.UUID;

/**
 * Created by andrew on 15-9-5.
 */
public class MsgGroup extends jxORMobj {

    public static ORMID GetORMID(UUID ID)
    {
        return new ORMID(ORMType.MsgGroup.ordinal(),ID);
    }

    @Override
    protected void Init_Create(DB db) throws Exception
    {
        ID= UUID.randomUUID();
        CreateTime=new Date();
    }

    public static void Init() throws Exception{	InitClass(ORMType.MsgGroup.ordinal(),Message.class);}
    public static void CreateDB(TopSpace ts) throws Exception
    {
        CreateTableInDB(Message.class,ts);
    }

    @ORM(keyType= ORM.KeyType.PrimaryKey)
    public UUID ID;

    @ORM(Index=1)
    public String Name;

    @ORM
    public String Descr;

    @ORM(Index=2)
    public Date CreateTime;

    @ORM
    public Boolean NoUsed;

    public static void AddMember(UUID MsgGroupID,UUID PeopleID) throws Exception {
        Relation rl=(Relation)Create(Relation.class);
        rl.ObjTypeID=ORMType.MsgGroup.ordinal();
        rl.ObjID=MsgGroupID;
        rl.TargetTypeID=ORMType.People.ordinal();
        rl.TargetID=PeopleID;
        rl.RelType=RelationType.Contain;
        rl.Insert(null);
    }

    /**
     * 列出某个MsgGroup中的所有成员
     * @param MsgGroupID
     * @return
     * @throws Exception
     */
    public static Queue<jxORMobj> ListMember(UUID MsgGroupID) throws Exception {
        SelectSql s=new SelectSql();
        s.AddTable("Relation",null);
        s.AddTable("People", null);
        s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.MsgGroup.ordinal());
        s.AddContion("Relation", "ObjID", jxCompare.Equal,MsgGroupID);
        s.AddContion("Relation", "RelType", jxCompare.Equal, RelationType.Contain);
        s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.People.ordinal());
        s.AddContion("Relation", "TargetID", "People", "ID");
        return Select(People.class,s,null);
    }

    /**
     * 列出某人所参与的所有MsgGroup
     * @param PeopleID
     * @return
     * @throws Exception
     */
    public static Queue<jxORMobj> ListMsgGroups(UUID PeopleID) throws Exception {
        SelectSql s=new SelectSql();
        s.AddTable("Relation",null);
        s.AddTable("MsgGroup", null);
        s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.MsgGroup.ordinal());
        s.AddContion("Relation", "ObjID", "MsgGroup", "ID");
        s.AddContion("Relation", "RelType", jxCompare.Equal, RelationType.Contain);
        s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.People.ordinal());
        s.AddContion("Relation", "TargetID", jxCompare.Equal,PeopleID);
        return Select(MsgGroup.class,s,null);
    }

}
