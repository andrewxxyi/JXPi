
package cn.ijingxi.common.util;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import cn.ijingxi.common.Process.InstanceState;


public class utils
{
	public static String DateFormat="yyyy-MM-dd HH:mm:ss";
	public static SimpleDateFormat DateFormatter=new SimpleDateFormat(DateFormat);
	public static String TransDateToString(Date ct)
	{
		 return DateFormatter.format(ct);
	}
	public static Date TransToDate(String str) throws ParseException
	{
		 return DateFormatter.parse(str);
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
	
	public static String TransToSTR(UUID id)
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
	
	public static Integer TransToInteger(String str)
	{
		return Integer.parseInt(str);
	}
	
	public static int TransByteArrayToInt(byte[] bs,int start)
	{
		return (int) ((((bs[start + 3] & 0xff) << 24)  
            | ((bs[start + 2] & 0xff) << 16)  
            | ((bs[start + 1] & 0xff) << 8)
            | (bs[start + 0] & 0xff))); 
	}
	public static long TransByteArrayToLong(byte[] bs,int start)
	{
		return (long) (
			((bs[start + 7] & 0xff) << 56)  
	        | ((bs[start + 6] & 0xff) << 48)  
	        | ((bs[start + 5] & 0xff) << 40)
	        | ((bs[start + 4] & 0xff) << 32)
			|	((bs[start + 3] & 0xff) << 24)  
            | ((bs[start + 2] & 0xff) << 16)  
            | ((bs[start + 1] & 0xff) << 8)
            | (bs[start + 0] & 0xff)
            ); 
	}
	/**
	 * 将num放到从start起的bs中，连续4个字节
	 * @param bs
	 * @param start
	 * @param num
	 */
	public static void TransIntToByteArray(byte[] bs,int start,int num)
	{
		// 最低位 
		bs[start + 0] = (byte) (num & 0xff);
		bs[start + 1] = (byte) ((num >> 8) & 0xff);
		bs[start + 2] = (byte) ((num >> 16) & 0xff);
		// 最高位,无符号右移。 
		bs[start + 3] = (byte) (num >>> 24);
	}
	public static void TransLongToByteArray(byte[] bs,int start,long num)
	{
		// 最低位 
		bs[start + 0] = (byte) (num & 0xff);
		bs[start + 1] = (byte) ((num >> 8) & 0xff);
		bs[start + 2] = (byte) ((num >> 16) & 0xff);
		bs[start + 3] = (byte) ((num >> 24) & 0xff);
		bs[start + 4] = (byte) ((num >> 32) & 0xff);
		bs[start + 5] = (byte) ((num >> 40) & 0xff);
		bs[start + 6] = (byte) ((num >> 48) & 0xff);
		// 最高位,无符号右移。 
		bs[start + 7] = (byte) (num >>> 56);
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
		String[] ss = ClassName.split(".");
		if(ss==null)
			return ClassName;
		else
			return ss[ss.length-1];
	}
	public static boolean JudgeIsEnum(Class<?> cls)
	{
		Class<?> p=cls.getSuperclass();
		return p!=null&&p.getName().compareTo("Enum")==0;
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
		Field f = obj.getClass().getDeclaredField(FieldName);
		return f.get(obj);
	}
	public static void setFiledValue(Object obj,String FieldName,Object value) throws Exception
	{
		Field f = obj.getClass().getDeclaredField(FieldName);
		f.set(obj, value);
	}
	
	public static void P(String msg)
	{
		System.out.println(msg);
	}
	
	public static void Check (boolean con,String msg) throws Exception
	{
		if(con)
			throw new Exception(msg);
	}
	
	
}