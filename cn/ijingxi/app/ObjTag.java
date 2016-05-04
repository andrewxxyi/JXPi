
package cn.ijingxi.app;

import cn.ijingxi.intelControl.jxLua;
import cn.ijingxi.orm.*;
import cn.ijingxi.util.jxCompare;
import cn.ijingxi.util.utils;
import org.luaj.vm2.LuaValue;

import java.util.*;

public class ObjTag extends jxORMobj
{
	public static final int UserTagID=0x1000000;

	public static ObjTag System=null;
	public static UUID SystemID=null;

	private static Map<String,LuaValue> luaSystemConf=null;

	public static void Init() throws Exception{
		InitClass(ORMType.ObjTag.ordinal(),ObjTag.class,"标记");
		InitTagList(tags,1);
		System=getSystem();
		if(System!=null)
			SystemID=System.ID;
		luaSystemConf= jxLua.getConf("system.lua","system");
	}
	public static LuaValue getSystemLuaConf(String confName){
		if(luaSystemConf!=null)
			return luaSystemConf.get(confName);
		return LuaValue.NIL;
	}
	public static boolean checkSystemLogin() {
		LuaValue v = getSystemLuaConf("NoLogin");
		if (!v.isnil())
			return v.checkboolean();
		return false;
	}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(ObjTag.class);
		System=(ObjTag) jxORMobj.Create(ObjTag.class);
		System.TagID=getTagID("系统");
		System.Insert(null);
		SystemID=System.ID;
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
	public String Name;

	@ORM(Index=4)
	public String Category;

	@ORM
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

	@ORM
	public boolean Used;

	public static Queue<jxORMobj> ListTag(Class<?> cls,Map<String,Object> ks) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("ObjTag");
		for(Map.Entry<String, Object> entry : ks.entrySet())
			s.AddContion("ObjTag", entry.getKey(), jxCompare.Equal, entry.getValue());
		return Select(cls,s);
	}
	public static Queue<jxORMobj> ListTag(DB db,Class<?> cls,Map<String,Object> ks) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("ObjTag");
		for(Map.Entry<String, Object> entry : ks.entrySet())
			s.AddContion("ObjTag", entry.getKey(), jxCompare.Equal, entry.getValue());
		return Select(db,cls,s);
	}
	/**
	 * 列表TagID在[StartID,StartID&0xffffff00+0x100)区间内的所有tag
	 * @param ObjID
	 * @param StartID 是自StartID起直至下一个id段
	 * @return
	 * @throws Exception
	 */
	public static Queue<jxORMobj> ListTag(UUID ObjID,int StartID) throws Exception
	{
		int endid=StartID&0xffffff00+0x100;
		SelectSql s=new SelectSql();
		s.AddTable("ObjTag");
		if(ObjID!=null)
			s.AddContion("ObjTag", "ObjID", jxCompare.Equal, ObjID);
		s.AddContion("ObjTag", "TagID", jxCompare.GreateEqual, StartID);
		s.AddContion("ObjTag", "TagID", jxCompare.Less, endid);
		return Select(ObjTag.class,s);
	}

	private static String[] tags={"系统","记录","权限","组","文件","状态切换","说明","计划",
			"开始时间","结束时间",
			"序列号",
			"智能前端配置"
	};

	private static ObjTag getSystem(){
		Map<String, Object> map = getMapParam("TagID", getTagID("系统"));
		Queue<jxORMobj> list = null;
		try {
			list = ListTag(ObjTag.class, map);
			if(list.size()==1)
				return (ObjTag) list.poll();
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return null;
	}

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

	private static Map<Integer,String> tagList=new HashMap<Integer,String>();
	private static Map<String,Integer> tagList_KS=new HashMap<String,Integer>();
	public static void InitTagList(String[] tags,int initTagID) throws Exception {
		utils.Check(tagList.containsKey(initTagID),"TagID已使用："+initTagID);
		int tid=initTagID;
		for(String s:tags){
			if(tagList_KS.containsKey(s))
				throw new Exception("Tag already define:"+s);
			tagList_KS.put(s,tid);
			tagList.put(tid,s);
			tid++;
		}
	}

	public static String getTagName(int TagID)
	{
		return tagList.get(TagID);
	}

	public static Integer getTagID(String Name)
	{
		return tagList_KS.get(Name);
	}

	
}

