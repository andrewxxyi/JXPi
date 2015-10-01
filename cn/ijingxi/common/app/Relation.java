
package cn.ijingxi.common.app;

import cn.ijingxi.common.Process.InstanceState;
import cn.ijingxi.common.Process.jxTask;
import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.jxCompare;

import java.util.Queue;
import java.util.UUID;

public class Relation extends jxORMobj
{
	public static void Init() throws Exception{	InitClass(ORMType.Relation.ordinal(),Relation.class);}
	public static void CreateDB(TopSpace ts) throws Exception
	{
		CreateTableInDB(Relation.class,ts);
	}

	@ORM(keyType=KeyType.PrimaryKey)
	public int ObjTypeID;
	@ORM(keyType=KeyType.PrimaryKey)
	public UUID ObjID;

	@ORM(keyType=KeyType.PrimaryKey)
	public int TargetTypeID;
	@ORM(keyType=KeyType.PrimaryKey)
	public UUID TargetID;
	
	@ORM
	public RelationType RelType;

	@ORM
	public Boolean NoUsed;

	@ORM(Descr="根据需要可以是字符串或json格式的附加信息")
	public String Info;
	
	public static void AddRela(TopSpace ts,int ObjTypeID,UUID ObjID,int TargetTypeID,UUID TargetID,RelationType RelType) throws Exception
	{
		Relation rl=(Relation) Relation.Create(Relation.class);
		rl.ObjTypeID=ObjTypeID;
		rl.ObjID=ObjID;
		rl.TargetTypeID=TargetTypeID;
		rl.TargetID=TargetID;
		rl.RelType=RelType;
		rl.Insert(ts);
	}
	public static void AddRela(DB db,TopSpace ts,int ObjTypeID,UUID ObjID,int TargetTypeID,UUID TargetID,RelationType RelType) throws Exception
	{
		Relation rl=(Relation) Relation.Create(Relation.class);
		rl.ObjTypeID=ObjTypeID;
		rl.ObjID=ObjID;
		rl.TargetTypeID=TargetTypeID;
		rl.TargetID=TargetID;
		rl.RelType=RelType;
		rl.Insert(db,ts);
	}

	public static Queue<jxORMobj> listTarget(DB db,TopSpace ts,int ObjTypeID,UUID ObjID,RelationType RelTyp) throws Exception {
		SelectSql s=new SelectSql();
		s.AddTable("Relation", ts);
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ObjTypeID);
		s.AddContion("Relation", "ObjID", jxCompare.Equal,ObjID);
		if(RelTyp!=RelationType.None)
			s.AddContion("Relation", "RelType", jxCompare.Equal, RelTyp);
		return Select(db,Relation.class,s,ts);
	}
	public static Queue<jxORMobj> listObj(DB db,TopSpace ts,int TargetTypeID,UUID TargetID,RelationType RelTyp) throws Exception {
		SelectSql s=new SelectSql();
		s.AddTable("Relation", ts);
		s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, TargetTypeID);
		s.AddContion("Relation", "TargetID", jxCompare.Equal,TargetID);
		if(RelTyp!=RelationType.None)
			s.AddContion("Relation", "RelType", jxCompare.Equal, RelTyp);
		return Select(db,Relation.class,s,ts);
	}



}

