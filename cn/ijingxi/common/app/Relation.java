
package cn.ijingxi.common.app;

import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;

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
	public int ObjID;

	@ORM(keyType=KeyType.PrimaryKey)
	public int TargetTypeID;
	@ORM(keyType=KeyType.PrimaryKey)
	public int TargetID;
	
	@ORM
	public int RelationType;	
	
}

