
package cn.ijingxi.app;

import cn.ijingxi.orm.*;
import cn.ijingxi.orm.ORM.KeyType;

import java.util.Date;
import java.util.UUID;

public class Container extends jxORMobj
{	
	//圈子

	@Override
	protected void Init_Create(DB db) throws Exception
	{
		   ID=UUID.randomUUID();
			CreateTime=new Date();
	}

	public static void Init() throws Exception{
		InitClass(ORMType.Container.ordinal(),Container.class,"容器");
	}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(Container.class);
	}
	
	public static ORMID GetORMID(UUID ID)
	{
		return new ORMID(ORMType.Container.ordinal(),ID);
	}
		
	@ORM(keyType=KeyType.PrimaryKey)
	public UUID ID;
	
	@ORM(Index=1,Encrypted=true)
	public String Name;		
	
	@ORM(Descr="说明信息",Encrypted=true)
	public String Descr;
	
	@ORM(Index=5,Descr="用于两者之间的同步")
	public Date CreateTime;

	public TopSpace myTopSpace=null;
	
}