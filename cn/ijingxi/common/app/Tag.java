
package cn.ijingxi.common.app;

import java.util.HashMap;
import java.util.Map;

import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.jxCompare;

/**
 * 全局的，所有topspace共享
 * @author andrew
 *
 */
public class Tag extends jxORMobj
{		
	//所有的tag其id是按每两个字节分类的
	public static ORMID GetORMID(Integer ID)
	{
		return new ORMID(ORMType.Tag.ordinal(),ID);
	}
	
	public static void Init() throws Exception
	{	
		InitClass(ORMType.Tag.ordinal(),Tag.class);
	}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(Tag.class,null);
		Init_AddTag();
	}
	
	//非自动生成！！
	@ORM(keyType=KeyType.PrimaryKey)
	public long ID;

	@ORM(Index=1,Encrypted=true)
	public String Name;

	@ORM(Descr="json格式的附加信息",Encrypted=true)
	public String Addition;
	
	private static Map<String,Long> TagCache=new HashMap<String,Long>();
	public static Long GetTagID(People Caller,String TagName) throws Exception
	{
		long id=TagCache.get(TagName);
		if(id>0)return id;
		SelectSql s=new SelectSql();
		s.AddTable("Tag",Caller.CurrentTopSpace);
		s.AddContion("Tag", "Name", jxCompare.Equal, TagName);
		Tag tag=(Tag) Get(ObjTag.class,s,Caller.CurrentTopSpace);
		if(tag!=null)
			TagCache.put(TagName, tag.ID);
		return tag.ID;
	}
	
	public static Tag NewTag(int ID,String Name) throws Exception
	{
		Tag tag = (Tag) New(Tag.class);
		tag.ID=ID;
		tag.Name=Name;
		return tag;
	}
	
	private static void Init_AddTag() throws InstantiationException, IllegalAccessException, Exception
	{
		NewTag(1,"").Insert(null);
		
		
		
	}


	
	
	
}