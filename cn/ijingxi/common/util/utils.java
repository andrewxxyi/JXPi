
package cn.ijingxi.common.util;

import java.util.Calendar;
import java.util.Date;

import cn.ijingxi.common.Process.*;
import cn.ijingxi.common.app.*;


public class utils
{
	public static void Init() throws Exception
	{
		//初始化顺序不能发生改变，新增加的类只能在最后添加不能中间插入！！
		jxMsg.Init();
		jxProcess.Init();
		PI.Init();
		PN.Init();		
		Container.Init();
		jxSystem.Init();
		People.Init();
		Role.Init();
		Relation.Init();
		Tag.Init();
		jxLog.Init();

		Trans.AddEunmType(InstanceState.Doing);
		Trans.AddEunmType(InstanceEvent.Touch);
		Trans.AddEunmType(jxMsgType.Text);
		Trans.AddEunmType(Result.Accept);
		Trans.AddEunmType(jxCompare.Equal);
		Trans.AddEunmType(jxMsgState.Posted);
		Trans.AddEunmType(jxOP.Equal);
		
		udpMsg.Init();
	}
	public static void CreateDBTable() throws Exception
	{
		jxMsg.CreateDB();
		jxProcess.CreateDB();
		PI.CreateDB();
		PN.CreateDB();
		
		Container.CreateDB();
		//People在jxSystem中已完成了创建与初始化
		jxSystem.CreateDB();
		//People.CreateDB();
		Role.CreateDB();
		Relation.CreateDB();
		Tag.CreateDB();		
		jxLog.CreateDB();
	}
	
	public static Calendar GetDate(Calendar t)
	{
		Calendar c=Calendar.getInstance();
		c.set(Calendar.YEAR, t.get(Calendar.YEAR));
		c.set(Calendar.MONTH, t.get(Calendar.MONTH));
		c.set(Calendar.DAY_OF_MONTH, t.get(Calendar.DAY_OF_MONTH));
		return c;				
	}
	public static boolean CheckDate(int year,int month,int day)
	{
		try
		{
			Calendar c=Calendar.getInstance();
			c.setLenient(false);
	        c.set(Calendar.YEAR, year);
	        c.set(Calendar.MONTH, month);
	        c.set(Calendar.DATE, day);
	       // 如果日期错误,执行该语句,必定抛出异常.
	        c.get(Calendar.YEAR);
			return true;
	    } catch (IllegalArgumentException e) {
	        	return false;
	        	}
	}
	
	public static Calendar GetCalendar(int year,int month,int day)
	{
		Calendar c=Calendar.getInstance();
		c.set(year, month-1, day);
		return c;
	}
	public static Calendar GetDate()
	{
		Date d=new Date();
		Calendar c=Calendar.getInstance();
		c.setTime(d);
		return c;
	}
	
	public static String GetClassName(Class<?> cls)
	{
		return GetClassName(cls.getName());
	}
	public static String GetClassName(String ClassName)
	{
		String[] ss = ClassName.split("\\.");
		if(ss.length==0)
			return ClassName;
		else
			return ss[ss.length-1];
	}
	public static boolean JudgeIsEnum(Class<?> cls)
	{
		Class<?> p=cls.getSuperclass();
		if(p==null)return false;
		return GetClassName(p).compareTo("Enum")==0;
	}
	
	public static String StringAdd(String str,String split,String WantAdd)
	{
		if(str==null)
			return WantAdd;
		else
			return str+split+WantAdd;
	}

	public static void P(Object msg)
	{
		System.out.println(msg);
	}
	
	public static void Check (boolean con,String msg) throws Exception
	{
		if(con)
			throw new Exception(msg);
	}
	
	
}