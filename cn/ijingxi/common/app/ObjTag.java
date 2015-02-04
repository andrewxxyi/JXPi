
package cn.ijingxi.common.app;

import java.util.Date;
import java.util.Queue;

import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.jxCompare;

public class ObjTag extends jxORMobj
{
	public static void Init() throws Exception{	InitClass(ORMType.ObjTag.ordinal(),ObjTag.class);}
	public static void CreateDB(TopSpace ts) throws Exception
	{
		CreateTableInDB(ObjTag.class,ts);
	}

	@ORM(keyType=KeyType.AutoDBGenerated)
	public int ID;

	@ORM(Index=1)
	public int ObjTypeID;
	@ORM(Index=1)
	public int ObjID;

	@ORM(Index=1)
	public long TagID;
	
	@ORM
	public String Descr;
	
	@ORM(Descr="标记是可以带有状态的")
	public int TagState;
	
	@ORM(Index=2,Descr="标记时的时间")
	public Date TagTime;	

	@ORM
	public float Number;	

	@ORM(Index=3,Descr="时间点信息，如todo的发生时间，两个时间点Tag可以组成时间段")
	public Date Time;

	//@ORM(Index=3,Descr="时间段信息，和Time组合使用，Time是起点，如日程安排")
	//public Date ToTime;
	
	@ORM(Descr="json格式的附加信息",Encrypted=true)
	public String Addition;

	private static Long TagIDMask=(long) 0x10000;
	public Queue<jxORMobj> List(People Caller,int typeid,int id,Long TagID) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("ObjTag",Caller.CurrentTopSpace);
		s.AddContion("ObjTag", "ObjTypeID", jxCompare.Equal, typeid);
		s.AddContion("ObjTag", "ObjID", jxCompare.Equal, id);
		if(TagID%TagIDMask==0)
		{
			//如果最后两个字节都为0，则代表是某一个类，如费用，下面再分打车等等
			long start=TagID/TagIDMask;
			long end=start+TagIDMask;
			s.AddContion("ObjTag", "TagID", jxCompare.GreateEqual,start);
			s.AddContion("ObjTag", "TagID", jxCompare.Less,end);
		}
		else
			s.AddContion("ObjTag", "TagID", "Tag", "ID");
		return Select(ObjTag.class,s,Caller.CurrentTopSpace);
	}
	public Queue<jxORMobj> List(People Caller,int typeid,int id,String TagName) throws Exception
	{
		long tid=Tag.GetTagID(Caller, TagName);
		return List(Caller,typeid,id,tid);
	}
	
}

