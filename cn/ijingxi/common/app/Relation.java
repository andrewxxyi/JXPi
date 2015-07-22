
package cn.ijingxi.common.app;

import cn.ijingxi.common.orm.DB;
import cn.ijingxi.common.orm.ORM;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.orm.ORMType;
import cn.ijingxi.common.orm.jxORMobj;

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
	
	@ORM(Descr="根据需要可以是字符串或json格式的附加信息")
	public String Extra;	
	
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
}

