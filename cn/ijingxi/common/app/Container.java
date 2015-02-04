
package cn.ijingxi.common.app;

import java.util.Date;

import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;

public class Container extends jxORMobj
{	
	//圈子
		
	protected Container()
	{
		super();
		CreateTime=new Date();
	}
	public static void Init() throws Exception{	InitClass(ORMType.Container.ordinal(),Container.class);}
	public static void CreateDB(TopSpace ts) throws Exception
	{
		CreateTableInDB(Container.class,ts);
	}
	
	public static ORMID GetORMID(Integer ID)
	{
		return new ORMID(ORMType.Container.ordinal(),ID);
	}
		
	@ORM(keyType=KeyType.AutoSystemGenerated)
	public int ID;

	@ORM(Index=1,Encrypted=true)
	public String Name;		
	
	@ORM(Descr="说明信息",Encrypted=true)
	public String Descr;
	
	@ORM(Index=5,Descr="用于两者之间的同步")
	public Date CreateTime;

	public TopSpace myTopSpace=null;
	
	public ObjTag Tag(long TagID) throws Exception
	{
		ObjTag ot=(ObjTag) ObjTag.New(ObjTag.class);
		ot.ObjTypeID=getTypeID();
		ot.ObjID=ID;
		ot.TagID=TagID;
		ot.TagTime=new Date();
		return ot;
	}
	
}