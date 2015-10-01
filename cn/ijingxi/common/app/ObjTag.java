
package cn.ijingxi.common.app;

import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.util.Trans;
import cn.ijingxi.common.util.jxCompare;

import java.util.*;

public class ObjTag extends jxORMobj
{
	public static void Init() throws Exception{
		InitClass(ORMType.ObjTag.ordinal(),ObjTag.class);
		InitTagList(tags);
	}
	public static void CreateDB(TopSpace ts) throws Exception
	{
		CreateTableInDB(ObjTag.class, ts);
	}
	@Override
	protected void Init_Create(DB db) throws Exception
	{
		ID= UUID.randomUUID();
		CreateTime=new Date();
	}

	@ORM(keyType= ORM.KeyType.PrimaryKey,Descr = "如果没有PrimaryKey，则无法update")
	public UUID ID;

	@ORM
	public int ObjTypeID;
	@ORM(Index=1)
	public UUID ObjID;
	
	@ORM(Index=2)
	public int TagID;

	@ORM(Descr="如果某一对象打了同样的多个tag则用此进行区别")
	public int TagOrder;

	@ORM(Index=3)
	public String Category;

	@ORM(Index=4)
	public String Descr;
	
	@ORM(Descr="标记是可以带有状态的")
	public int TagState;
	
	@ORM(Index=5,Descr="标记时的时间")
	public Date CreateTime;

	@ORM
	public Float Number;	

	@ORM(Index=6,Descr="时间点信息，如todo的发生时间，两个时间点Tag可以组成时间段")
	public Date Time;

	//@ORM(Index=3,Descr="时间段信息，和Time组合使用，Time是起点，如日程安排")
	//public Date ToTime;
	
	@ORM(Descr="json格式的附加信息")
	public String Info;


	/**
	 * 专用于ObjTag衍生出来的无额外数据表的类
	 * @param cls
	 * @param db
	 * @param ObjID
	 * @param Category
	 * @return
	 * @throws Exception
	 */
	public static Queue<jxORMobj> ListTagByCategory(Class<?> cls,DB db,UUID ObjID,String Category) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("ObjTag", null);
		if(ObjID!=null)
			s.AddContion("ObjTag", "ObjID", jxCompare.Equal, ObjID);
		s.AddContion("ObjTag", "Category", jxCompare.Equal, Category);
		return Select(db,cls,s,null);
	}
	public static Queue<jxORMobj> ListTagByCategory(Class<?> cls,UUID ObjID,String Category) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("ObjTag", null);
		if(ObjID!=null)
			s.AddContion("ObjTag", "ObjID", jxCompare.Equal, ObjID);
		s.AddContion("ObjTag", "Category", jxCompare.Equal, Category);
		return Select(cls,s,null);
	}
	public static Queue<jxORMobj> ListTag(TopSpace ts,UUID ObjID,String TagName) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("ObjTag", ts);
		s.AddContion("ObjTag", "ObjID", jxCompare.Equal, ObjID);
		if(TagName!=null && TagName!="")
			s.AddContion("ObjTag", "TagID", jxCompare.Equal, getTagID(TagName));
		return Select(ObjTag.class,s,ts);
	}	
	public static ObjTag GetTag(TopSpace ts,UUID ObjID,String TagName) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("ObjTag",ts);
		s.AddContion("ObjTag", "ObjID", jxCompare.Equal, ObjID);
		s.AddContion("ObjTag", "TagID", jxCompare.Equal, getTagID(TagName));
		Queue<jxORMobj> tl = Select(ObjTag.class,s,ts);
		for(jxORMobj obj:tl)
		{
			ObjTag tag=(ObjTag)obj;
			if(tag!=null)
				return tag;
		}
		return null;
	}	
	/**
	 * 列表TagID在[StartID,StartID&0xffffff00+0x100)区间内的所有tag
	 * @param ts
	 * @param ObjID
	 * @param StartID 是自StartID起直至下一个id段
	 * @return
	 * @throws Exception
	 */
	public static Queue<jxORMobj> ListTag(TopSpace ts,UUID ObjID,int StartID) throws Exception
	{
		int endid=StartID&0xffffff00+0x100;
		SelectSql s=new SelectSql();
		s.AddTable("ObjTag",ts);
		s.AddContion("ObjTag", "ObjID", jxCompare.Equal, ObjID);
		s.AddContion("ObjTag", "TagID", jxCompare.GreateEqual, StartID);
		s.AddContion("ObjTag", "TagID", jxCompare.Less, endid);
		return Select(ObjTag.class,s,ts);
	}
	public static ObjTag AddTag(TopSpace ts,int TagID,int ObjTypeID,int TagOrder,UUID ObjID,Float TagValue,String Category,String Descr) throws Exception{
		ObjTag tag=(ObjTag) ObjTag.Create(ObjTag.class);
		tag.TagID=TagID;
		tag.ObjTypeID=ObjTypeID;
		tag.TagOrder=TagOrder;
		tag.ObjID=ObjID;
		tag.Number=TagValue;
		tag.Category=Category;
		tag.Descr=Descr;
		tag.Insert(ts);
		return tag;
	}
	public static ObjTag AddTag(TopSpace ts,int TagID,int ObjTypeID,UUID ObjID,Float TagValue,String Category,String Descr) throws Exception
	{
		return AddTag(ts,TagID,ObjTypeID,0,ObjID,TagValue,Category,Descr);
	}
	public static ObjTag AddTag(DB db,TopSpace ts,int TagID,int ObjTypeID,int TagOrder,UUID ObjID,Float TagValue,String Category,String Descr) throws Exception{
		ObjTag tag=(ObjTag) ObjTag.Create(ObjTag.class);
		//tag.Name=getTagName(TagID);
		tag.TagID=TagID;
		tag.ObjTypeID=ObjTypeID;
		tag.ObjID=ObjID;
		tag.TagOrder=TagOrder;
		tag.Number=TagValue;
		tag.Category=Category;
		tag.Descr=Descr;
		tag.Insert(db, ts);
		return tag;
	}
	public static ObjTag AddTag(DB db,TopSpace ts,int TagID,int ObjTypeID,UUID ObjID,Float TagValue,String Category,String Descr) throws Exception
	{
		return AddTag(db,ts,TagID,ObjTypeID,0,ObjID,TagValue,Category,Descr);
	}
	public static ObjTag AddTag(TopSpace ts,int TagID,int ObjTypeID,int TagOrder,UUID ObjID,Date time,String Category,String Descr) throws Exception{
		ObjTag tag=(ObjTag) ObjTag.Create(ObjTag.class);
		//tag.Name=getTagName(TagID);
		tag.TagID=TagID;
		tag.ObjTypeID=ObjTypeID;
		tag.ObjID=ObjID;
		tag.TagOrder=TagOrder;
		tag.Time=time;
		tag.Category=Category;
		tag.Descr=Descr;
		tag.Insert(ts);
		return tag;
	}
	public static ObjTag AddTag(TopSpace ts,int TagID,int ObjTypeID,UUID ObjID,Date time,String Category,String Descr) throws Exception
	{
		return AddTag(ts,TagID,ObjTypeID,0,ObjID,time,Category,Descr);
	}
	public static ObjTag AddTag(DB db,TopSpace ts,int TagID,int ObjTypeID,int TagOrder,UUID ObjID,Date time,String Category,String Descr) throws Exception{
		ObjTag tag=(ObjTag) ObjTag.Create(ObjTag.class);
		//tag.Name=getTagName(TagID);
		tag.TagID=TagID;
		tag.ObjTypeID=ObjTypeID;
		tag.ObjID=ObjID;
		tag.TagOrder=TagOrder;
		tag.Time=time;
		tag.Category=Category;
		tag.Descr=Descr;
		tag.Insert(db, ts);
		return tag;
	}
	public static ObjTag AddTag(DB db,TopSpace ts,int TagID,int ObjTypeID,UUID ObjID,Date time,String Category,String Descr) throws Exception
	{
		return AddTag(db,ts,TagID,ObjTypeID,0,ObjID,time,Category,Descr);
	}

	private static String[] tags={"记录","组","状态切换","说明","计划","开始时间","结束时间"};

	/*
	//系统
	public static final int Tag_System=0x100;
	public static final int Tag_System_Log=Tag_System+1;
	//状态切换
	public static final int Tag_System_StateChange=Tag_System_Log+1;
	public static final int Tag_System_Plan=Tag_System_StateChange+1;
	public static final int Tag_System_Remark=Tag_System_Plan+1;
	public static final int Tag_System_LastTime=Tag_System_Remark+1;
	
	
	//费用
	public static final int Tag_Cost=0x1100;
	//出租车费
	public static final int Tag_Cost_Taxi=Tag_Cost+1;
	

	//预算
	public static final int Tag_Budget=0x2100;
	*/
	

	public static void Log(TopSpace ts,int TypeID,UUID ID,int LogLevel,String Name,String Descr) throws Exception
	{
		ObjTag tag=(ObjTag) ObjTag.Create(ObjTag.class);
		tag.TagID=getTagID("日志");
		//tag.Name=getTagName(Tag_System_Log);
		tag.ObjTypeID=TypeID;
		tag.ObjID=ID;
		tag.Number=Trans.TransToFloat(LogLevel);
		//tag.Name=Name;
		tag.Descr=Descr;
		tag.Insert(ts);
	}
	public static void Log(DB db,TopSpace ts,int TypeID,UUID ID,int LogLevel,String Name,String Descr) throws Exception
	{
		ObjTag tag=(ObjTag) ObjTag.Create(ObjTag.class);
		tag.TagID=getTagID("日志");
		//tag.Name=getTagName(Tag_System_Log);
		tag.ObjTypeID=TypeID;
		tag.ObjID=ID;
		tag.Number=Trans.TransToFloat(LogLevel);
		//tag.Name=Name;
		tag.Descr=Descr;
		tag.Insert(db, ts);
	}

	//每调用InitTagList一次则myTagID增加0x1000
	private static int myTagID=0;

	/**
	 * 要使用Tag，则应确保InitTagList调用顺序一致，不同模块定义的tag就可以不必担心tagid的冲突了
	 * @param tags
	 */
	public static void InitTagList(String[] tags) throws Exception {
		myTagID+=0x1000;
		InitTagList(tags,myTagID);
	}
	private static Map<Integer,String> tagList=new HashMap<Integer,String>();
	private static Map<String,Integer> tagList_KS=new HashMap<String,Integer>();
	public static void InitTagList(String[] tags,int initTagID) throws Exception {
		int i=0;
		for(String s:tags){
			if(tagList_KS.containsKey(s))
				throw new Exception("Tag already define:"+s);
			int tid=initTagID+i;
			tagList_KS.put(s,tid);
			tagList.put(tid,s);
		}
	}

	public static String getTagName(int TagID)
	{
		return tagList.get(TagID);
	}

	public static int getTagID(String Name)
	{
		return tagList_KS.get(Name);
	}

	
}

