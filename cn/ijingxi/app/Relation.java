
package cn.ijingxi.app;

import cn.ijingxi.orm.*;
import cn.ijingxi.orm.ORM.KeyType;
import cn.ijingxi.util.jxCompare;

import java.util.Queue;
import java.util.UUID;

public class Relation extends jxORMobj
{
	public static void Init() throws Exception{
		InitClass(ORMType.Relation.ordinal(),Relation.class,"关系");
	}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(Relation.class);
	}

	@ORM(keyType=KeyType.PrimaryKey)
	public int ObjTypeID;
	@ORM(keyType=KeyType.PrimaryKey)
	public UUID ObjID;

	@ORM(keyType=KeyType.PrimaryKey)
	public int TargetTypeID;
	@ORM(keyType=KeyType.PrimaryKey)
	public UUID TargetID;
	
	@ORM(keyType=KeyType.PrimaryKey)
	public RelationType RelType;

	@ORM
	public int Number;

	@ORM
	public String Descr;

	@ORM
	public Boolean NoUsed;

	@ORM(Descr="根据需要可以是字符串或json格式的附加信息")
	public String Info;
	
	public static Relation addRela(int ObjTypeID,UUID ObjID,int TargetTypeID,UUID TargetID,RelationType RelType) throws Exception
	{
		Relation rl=(Relation) Relation.Create(Relation.class);
		rl.ObjTypeID=ObjTypeID;
		rl.ObjID=ObjID;
		rl.TargetTypeID=TargetTypeID;
		rl.TargetID=TargetID;
		rl.RelType=RelType;
		rl.Insert();
		return rl;
	}
	public static Relation addRela(DB db,int ObjTypeID,UUID ObjID,int TargetTypeID,UUID TargetID,RelationType RelType) throws Exception
	{
		Relation rl=(Relation) Relation.Create(Relation.class);
		rl.ObjTypeID=ObjTypeID;
		rl.ObjID=ObjID;
		rl.TargetTypeID=TargetTypeID;
		rl.TargetID=TargetID;
		rl.RelType=RelType;
		rl.Insert(db);
		return rl;
	}

	public static Queue<jxORMobj> listTarget(DB db,int ObjTypeID,UUID ObjID,RelationType RelTyp) throws Exception {
		SelectSql s=new SelectSql();
		s.AddTable("Relation");
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ObjTypeID);
		s.AddContion("Relation", "ObjID", jxCompare.Equal,ObjID);
		if(RelTyp!=RelationType.None)
			s.AddContion("Relation", "RelType", jxCompare.Equal, RelTyp);
		return Select(db,Relation.class,s);
	}
	public static Queue<jxORMobj> listObj(DB db,int TargetTypeID,UUID TargetID,RelationType RelTyp) throws Exception {
		SelectSql s=new SelectSql();
		s.AddTable("Relation");
		s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, TargetTypeID);
		s.AddContion("Relation", "TargetID", jxCompare.Equal,TargetID);
		if(RelTyp!=RelationType.None)
			s.AddContion("Relation", "RelType", jxCompare.Equal, RelTyp);
		return Select(db,Relation.class,s);
	}



}

