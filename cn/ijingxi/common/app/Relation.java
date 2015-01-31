
package cn.ijingxi.common.app;

import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;

public class Relation extends jxORMobj
{
	public static void Init() throws Exception{	InitClass(Relation.class);}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(Relation.class);
	}
		
	@ORM(keyType=KeyType.PrimaryKey)
	public int ContainerID;
	
	@ORM(keyType=KeyType.PrimaryKey)
	public int TargetContainerID;
	
	@ORM
	public int RelationType;	
	
}

