
package cn.ijingxi.common.util;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import cn.ijingxi.common.Process.*;
import cn.ijingxi.common.app.*;
import cn.ijingxi.common.orm.jxORMobj;


public class utils
{
	public static void Init() throws Exception
	{
		jxMsg.Init();
		jxProcess.Init();
		ProcessInstance.Init();
		
		Container.Init();
		jxSystem.Init();
		People.Init();
		Relation.Init();
		Tag.Init();

		jxORMobj.AddEunmType(InstanceState.Doing);
		jxORMobj.AddEunmType(InstanceEvent.Touch);
		jxORMobj.AddEunmType(jxMsgType.Text);
	}
	public static void CreateDBTable() throws Exception
	{
		jxMsg.CreateDB();
		jxProcess.CreateDB();
		ProcessInstance.CreateDB();
		
		Container.CreateDB();
		//People在jxSystem中已完成了创建与初始化
		jxSystem.CreateDB();
		//People.CreateDB();
		Relation.CreateDB();
		Tag.CreateDB();		
	}
	
	public static String DateFormat="yyyy-MM-dd HH:mm:ss";
	public static SimpleDateFormat DateFormatter=new SimpleDateFormat(DateFormat);
	public static String TransToString(Date ct)
	{
		 return DateFormatter.format(ct);
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

	public static byte[] TransToByteArray(UUID id)
	{
		byte[] bs=new byte[16];
		TransToByteArray(bs,0,id);
		return bs;
	}
	public static void TransToByteArray(byte[] bs,int start,UUID id)
	{
		if(bs==null||start<0||bs.length<start+16)return;
		long lm=id.getMostSignificantBits();
		TransToByteArray(bs,start+8,lm);
		long ll=id.getLeastSignificantBits();
		TransToByteArray(bs,start,ll);
	}
	public static UUID TransToUUID(byte[] bs,int start)
	{
		if(bs==null||start<0||bs.length<start+16)return null;
		return new UUID(TransToLong(bs,start+8),TransToLong(bs,start));
	}
	public static String TransToString(UUID id)
	{
		String s = id.toString(); 
        //去掉“-”符号 
        return s.substring(0,8)+s.substring(9,13)+s.substring(14,18)+s.substring(19,23)+s.substring(24); 
	}
	public static UUID TransToUUID(String str)
	{
		String s=str.substring(0,8)+"-"+str.substring(8,12)+"-"+str.substring(12,16)+"-"+str.substring(16,20)+"-"+str.substring(20); 
		return UUID.fromString(s);
	}
	
	public static Object TransTo(Class<?> cls,Object value) throws ParseException
	{
		if(JudgeIsEnum(cls))
			return jxORMobj.TransTojxEunm(cls, value);
		String cn=GetClassName(cls);
		switch(cn)
		{
		case "byte":
		case "Byte":
			return TransToByte(value);
		case "short":
		case "Short":
			return TransToShort(value);
		case "long":
		case "Long":
			return TransToLong(value);
		case "float":
		case "Float":
			return TransToFloat(value);
		case "double":
		case "Double":
			return TransToDouble(value);
		case "boolean":
		case "Boolean":
			return TransToBoolean(value);
		case "Date":
			return TransToDate(value);
		}
		return value;
	}

	public static Boolean TransToBoolean(Object value)
	{
		return Boolean.parseBoolean(String.valueOf(value));
	}
	public static Double TransToDouble(Object value)
	{
		return Double.parseDouble(String.valueOf(value));
	}
	public static Float TransToFloat(Object value)
	{
		return Float.parseFloat(String.valueOf(value));
	}
	public static Long TransToLong(Object value)
	{
		return Long.parseLong(String.valueOf(value));
	}
	public static Integer TransToInteger(Object value)
	{
		return Integer.parseInt(String.valueOf(value));
	}
	public static Short TransToShort(Object value)
	{
		return Short.parseShort(String.valueOf(value));
	}
	public static Byte TransToByte(Object value)
	{
		return Byte.parseByte(String.valueOf(value));
	}
	public static Date TransToDate(Object str) throws ParseException
	{
		 return DateFormatter.parse(String.valueOf(str));
	}
	public static Integer TransToInteger(byte[] bs,int start)
	{
		Integer value= 0;
		//由高位到低位
		for (int i = 0; i < 4; i++)
		{
			int shift= (4 - 1 - i) * 8;
			//往高位游
			value +=(bs[start+i] & 0xFF) << shift;
		}
		return value;
	}
	public static long TransToLong(byte[] bs,int start)
	{
		long value= 0;
		//由高位到低位
		for (int i = 0; i < 8; i++)
		{
			int shift= (8 - 1 - i) * 8;
			//往高位游
			value +=((long)(bs[start+i] & 0xFF)) << shift;
		}
		return value;
	}
	/**
	 * 将num放到从start起的bs中，连续4个字节
	 * @param bs
	 * @param start
	 * @param num
	 */
	public static void TransToByteArray(byte[] bs,int start,int num)
	{
		bs[start+0] = (byte)((num >> 24) & 0xFF);
		bs[start+1] = (byte)((num >> 16) & 0xFF);
		bs[start+2] = (byte)((num >> 8) & 0xFF);
		bs[start+3] = (byte)(num & 0xFF);
	}
	public static byte[] TransToByteArray(int num)
	{
		byte[] bs=new byte[4];
		TransToByteArray(bs,0,num);
		return bs;
	}
	public static byte[] TransToByteArray(long num)
	{
		byte[] bs=new byte[8];
		TransToByteArray(bs,0,num);
		return bs;
	}
	public static void TransToByteArray(byte[] bs,int start,long num)
	{
		bs[start+0] = (byte)((num >> 56) & 0xFF);
		bs[start+1] = (byte)((num >> 48) & 0xFF);
		bs[start+2] = (byte)((num >> 40) & 0xFF);
		bs[start+3] = (byte)((num >> 32) & 0xFF);
		bs[start+4] = (byte)((num >> 24) & 0xFF);
		bs[start+5] = (byte)((num >> 16) & 0xFF);
		bs[start+6] = (byte)((num >> 8) & 0xFF);
		bs[start+7] = (byte)(num & 0xFF);
	}
	
	
	public static String TransCompareToString(jxCompare cp)
	{
		switch(cp)
		{
		case Equal:
			return "=";
		case NoEqual:
			return "<>";
		case Less:
			return "<";
		case LessEqual:
			return "<=";
		case Greate:
			return ">";
		case GreateEqual:
			return ">=";
		}
		return null;
	}
	
	public static InstanceState TransToInstanceState(String str)
	{
		switch(str)
		{
		case "Waiting":
			return InstanceState.Waiting;
		case "Doing":
			return InstanceState.Doing;
		case "Closed":
			return InstanceState.Closed;
		case "Canceled":
			return InstanceState.Canceled;
		case "Paused":
			return InstanceState.Paused;		
		}
		return InstanceState.NoActive;
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
	
	public static Object getFiledValue(Object obj,String FieldName) throws Exception
	{
		Field f=null;
		Class<?> cls=obj.getClass();
		while(cls!=null)
		{
			try{
				f = cls.getDeclaredField(FieldName);
			}
			catch(Exception e){
				f=null;
			}
			if(f!=null)
				break;
			cls=cls.getSuperclass();
		}
		if(f!=null)	
			return f.get(obj);
		return null;
	}
	public static void setFiledValue(Object obj,String FieldName,Object value) throws Exception
	{
		Field f=null;
		Class<?> cls=obj.getClass();
		while(cls!=null)
		{
			try{
				f = cls.getDeclaredField(FieldName);
			}
			catch(Exception e){
				f=null;
			}
			if(f!=null)
				break;
			cls=cls.getSuperclass();
		}
		if(f!=null)	
			f.set(obj, value);
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