
package cn.ijingxi.common.app;

import java.util.Date;
import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;

public class Tag extends jxORMobj
{		
	public static ORMID GetORMID(Integer ID)
	{
		return new ORMID(GetTypeID("Tag"),ID);
	}
	
	public static void Init() throws Exception
	{	
		InitClass(Tag.class);
	}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(Tag.class);
		Init_AddTag();
	}
	
	//非自动生成！！
	@ORM(keyType=KeyType.PrimaryKey)
	public int ID;

	@ORM(Index=1,Encrypted=true)
	public String Name;		

	@ORM(Index=2,Descr="用于两者之间的同步")
	public Date CreateTime;		

	@ORM(Descr="json格式的附加信息",Encrypted=true)
	public String Addition;
	
	public static Tag NewTag(int ID,String Name) throws Exception
	{
		Tag tag = (Tag) New(Tag.class);
		tag.ID=ID;
		tag.Name=Name;
		return tag;
	}
	
	private static void Init_AddTag() throws InstantiationException, IllegalAccessException, Exception
	{
		NewTag(1,"").Insert();
		
		
		
	}


	
	
	
}