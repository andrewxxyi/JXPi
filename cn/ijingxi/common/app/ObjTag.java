
package cn.ijingxi.common.app;

import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.util.Trans;
import cn.ijingxi.common.util.jxCompare;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class ObjTag extends jxORMobj
{
	public static void Init() throws Exception{	InitClass(ORMType.ObjTag.ordinal(),ObjTag.class);}
	public static void CreateDB(TopSpace ts) throws Exception
	{
		CreateTableInDB(ObjTag.class,ts);
	}

	@ORM
	public int ObjTypeID;
	@ORM(Index=1)
	public UUID ObjID;

	//@ORM(Index=2)
	//public String Name;
	
	@ORM(Index=3)
	public int TagID;
	
	@ORM
	public String Descr;
	
	@ORM(Descr="标记是可以带有状态的")
	public int TagState;
	
	@ORM(Index=4,Descr="标记时的时间")
	public Date TagTime;	

	@ORM
	public Float Number;	

	@ORM(Index=5,Descr="时间点信息，如todo的发生时间，两个时间点Tag可以组成时间段")
	public Date Time;

	//@ORM(Index=3,Descr="时间段信息，和Time组合使用，Time是起点，如日程安排")
	//public Date ToTime;
	
	@ORM(Descr="json格式的附加信息")
	public String Addition;
	
	
	
	
	public static Queue<jxORMobj> ListTag(TopSpace ts,UUID ObjID,String Name) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("ObjTag",ts);
		s.AddContion("ObjTag", "ObjID", jxCompare.Equal, ObjID);
		s.AddContion("ObjTag", "TagID", jxCompare.Equal, getTagID(Name));
		return Select(ObjTag.class,s,ts);
	}	
	public static ObjTag GetTag(TopSpace ts,UUID ObjID,String Name) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("ObjTag",ts);
		s.AddContion("ObjTag", "ObjID", jxCompare.Equal, ObjID);
		s.AddContion("ObjTag", "TagID", jxCompare.Equal, getTagID(Name));
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
	 * @param ObjTypeID
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
	public static ObjTag AddTag(TopSpace ts,int TagID,int ObjTypeID,UUID ObjID,Float TagValue,String Descr) throws Exception
	{
		ObjTag tag=(ObjTag) ObjTag.Create(ObjTag.class);
		//tag.Name=getTagName(TagID);
		tag.TagID=TagID;
		tag.ObjTypeID=ObjTypeID;
		tag.ObjID=ObjID;
		tag.Number=TagValue;
		tag.Descr=Descr;
		tag.TagTime=new Date();
		tag.Insert(ts);
		return tag;
	}
	public static ObjTag AddTag(DB db,TopSpace ts,int TagID,int ObjTypeID,UUID ObjID,Float TagValue,String Descr) throws Exception
	{
		ObjTag tag=(ObjTag) ObjTag.Create(ObjTag.class);
		//tag.Name=getTagName(TagID);
		tag.TagID=TagID;
		tag.ObjTypeID=ObjTypeID;
		tag.ObjID=ObjID;
		tag.Number=TagValue;
		tag.Descr=Descr;
		tag.TagTime=new Date();
		tag.Insert(db, ts);
		return tag;
	}
	public static ObjTag AddTag(TopSpace ts,int TagID,int ObjTypeID,UUID ObjID,Date time,String Descr) throws Exception
	{
		ObjTag tag=(ObjTag) ObjTag.Create(ObjTag.class);
		//tag.Name=getTagName(TagID);
		tag.TagID=TagID;
		tag.ObjTypeID=ObjTypeID;
		tag.ObjID=ObjID;
		tag.Time=time;
		tag.Descr=Descr;
		tag.TagTime=new Date();
		tag.Insert(ts);
		return tag;
	}
	public static ObjTag AddTag(DB db,TopSpace ts,int TagID,int ObjTypeID,UUID ObjID,Date time,String Descr) throws Exception
	{
		ObjTag tag=(ObjTag) ObjTag.Create(ObjTag.class);
		//tag.Name=getTagName(TagID);
		tag.TagID=TagID;
		tag.ObjTypeID=ObjTypeID;
		tag.ObjID=ObjID;
		tag.Time=time;
		tag.Descr=Descr;
		tag.TagTime=new Date();
		tag.Insert(db, ts);
		return tag;
	}
	

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
	
	

	public static void Log(TopSpace ts,int TypeID,UUID ID,int LogLevel,String Name,String Descr) throws Exception
	{
		ObjTag tag=(ObjTag) ObjTag.Create(ObjTag.class);
		tag.TagID=Tag_System_Log;
		//tag.Name=getTagName(Tag_System_Log);
		tag.ObjTypeID=TypeID;
		tag.ObjID=ID;
		tag.Number=Trans.TransToFloat(LogLevel);
		//tag.Name=Name;
		tag.Descr=Descr;
		tag.TagTime=new Date();
		tag.Insert(ts);
	}
	public static void Log(DB db,TopSpace ts,int TypeID,UUID ID,int LogLevel,String Name,String Descr) throws Exception
	{
		ObjTag tag=(ObjTag) ObjTag.Create(ObjTag.class);
		tag.TagID=Tag_System_Log;
		//tag.Name=getTagName(Tag_System_Log);
		tag.ObjTypeID=TypeID;
		tag.ObjID=ID;
		tag.Number=Trans.TransToFloat(LogLevel);
		//tag.Name=Name;
		tag.Descr=Descr;
		tag.TagTime=new Date();
		tag.Insert(db,ts);
	}
	
	//对ID的扩展
	private static Queue<ITag> tagExtFunc=new LinkedList<ITag>();
	public static void addExtra(ITag func)
	{
		tagExtFunc.add(func);
	}	
	
	public static String getTagName(int TagID)
	{
		for(ITag func :tagExtFunc)
		{
			String rs=func.getTagName(TagID);
			if(rs!=null)
				return rs;
		}
		switch(TagID)
		{
		case Tag_System:
			return "系统";
		case Tag_System_Log:
			return "记录";
		case Tag_System_StateChange:
			return "状态切换";
		case Tag_System_Plan:
			return "计划";
		case Tag_System_Remark:
			return "备注";
		case Tag_System_LastTime:
			return "最后期限";
			
			
			
		case Tag_Cost:
			return "费用";
		case Tag_Cost_Taxi:
			return "出租车费";
			

		case Tag_Budget:
			return "预算";
			
			
		}
		return null;
	}

	public static int getTagID(String Name)
	{
		for(ITag func :tagExtFunc)
		{
			int rs=func.getTagID(Name);
			if(rs!=0)
				return rs;
		}
		switch(Name)
		{
		case "系统":
			return Tag_System;
		case "记录":
			return Tag_System_Log;
		case "状态切换":
			return Tag_System_StateChange;
		case "计划":
			return Tag_System_Plan;
		case "备注":
			return Tag_System_Remark;
		case "最后期限":
			return Tag_System_LastTime;
			
			
		case "费用":
			return Tag_Cost;
		case "出租车费":
			return Tag_Cost_Taxi;
		case "预算":
			return Tag_Budget;

		}
		return 0;
	}

	
}

