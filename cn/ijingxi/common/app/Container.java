
package cn.ijingxi.common.app;

import cn.ijingxi.common.orm.ORM;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.orm.ORMID;
import cn.ijingxi.common.orm.ORMType;
import cn.ijingxi.common.orm.jxORMobj;

import java.util.Date;
import java.util.UUID;

public class Container extends jxORMobj
{	
	//圈子

	@Override
	protected void Init_Create() throws Exception
	{
		   ID=UUID.randomUUID();
			CreateTime=new Date();
	}

	public static void Init() throws Exception{	InitClass(ORMType.Container.ordinal(),Container.class);}
	public static void CreateDB(TopSpace ts) throws Exception
	{
		CreateTableInDB(Container.class,ts);
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