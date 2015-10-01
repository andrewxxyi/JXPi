
package cn.ijingxi.common.util;

import cn.ijingxi.common.Process.*;
import cn.ijingxi.common.app.*;
import cn.ijingxi.common.msg.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class utils
{
	public static void Init() throws Exception
	{
		TopSpace.Init();
		Container.Init();
		jxMsg.Init();
		jxProcess.Init();
		PI.Init();
		WorkNode.Init();		
		jxSystem.Init();
		People.Init();
		PeopleInTs.Init();
		Role.Init();
		Relation.Init();
		ObjTag.Init();
		jxLog.Init();
		jxTask.Init();
		Group.Init();

		Message.Init();
		MsgGroup.Init();

		Trans.AddEunmType(InstanceState.Doing);
		Trans.AddEunmType(InstanceEvent.Touch);
		Trans.AddEunmType(jxMsgType.Text);
		Trans.AddEunmType(Result.Accept);
		Trans.AddEunmType(jxCompare.Equal);
		Trans.AddEunmType(jxMsgState.Posted);
		Trans.AddEunmType(jxOP.Equal);
		Trans.AddEunmType(Right.Read);
		Trans.AddEunmType(TSEvent.Sync);
		Trans.AddEunmType(NodeType.Task);

		Trans.AddEunmType(Message.MsgType.Event);


		MsgAgent.Start();
		
		//udpMsg.Init();
	}
	public static void CreateDBTable() throws Exception
	{
		jxMsg.CreateDB();
		jxSystem.CreateDB();
		People.CreateDB();
		TopSpace.CreateDB();
		//ts外也可能会使用到关系和tag
		Relation.CreateDB(null);
		ObjTag.CreateDB(null);
		//保存自己的信息及全局通讯录
		jxLog.CreateDB();

		Message.CreateDB(null);
		MsgGroup.CreateDB(null);
	}	
	private static Map<String,Object> objCache=new HashMap<String,Object>();
	public static void putCache(String key,Object obj)
	{
		objCache.put(key, obj);
	}
	public static Object getCache(String key)
	{
		return objCache.get(key);
	}

	

	
    private static String regSuffix = "(\\.[^\\.]+)$";
    private static Pattern RegSuffix = Pattern.compile(regSuffix);
    /**
     * 获取文件名的后缀
     * @param filename
     * @return
     */
	public static String getSuffix(String filename)
	{			
	    Matcher m = RegSuffix.matcher(filename);
	    while(m.find())
	    	return m.group(0);
	    return null;
	}
	
	public static Integer daysBetween(Date f,Date t)
	{
		Calendar cf=Calendar.getInstance();
		cf.setTime(f);
		Calendar cfd = utils.GetDate(cf);
		Calendar ct=Calendar.getInstance();
		ct.setTime(t);
		Calendar ctd = utils.GetDate(ct);
		return Trans.TransToInteger((ctd.getTimeInMillis()-cfd.getTimeInMillis())/1000*3600*24);
	}
	public static Integer secondsBetween(Date f,Date t)
	{
		Calendar cf=Calendar.getInstance();
		cf.setTime(f);
		Calendar ct=Calendar.getInstance();
		ct.setTime(t);
		return Trans.TransToInteger((ct.getTimeInMillis()-cf.getTimeInMillis())/1000);
	}
	public static Calendar GetDate(Calendar t)
	{
		Calendar c=Calendar.getInstance();
		c.set(Calendar.YEAR, t.get(Calendar.YEAR));
		c.set(Calendar.MONTH, t.get(Calendar.MONTH));
		c.set(Calendar.DAY_OF_MONTH, t.get(Calendar.DAY_OF_MONTH));
		return c;				
	}

	public static Date addSecond(Date dt,int seconds)
	{
		Calendar c=Calendar.getInstance();
		c.setTime(dt);
		c.add(Calendar.SECOND, seconds);
		return c.getTime();
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
		c.set(year, month - 1, day);
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

	private static ILog p=null;
	public static void setLogger(ILog logger)
	{
		p=logger;
	}
	public static void P(String tag,String msg)
	{
		if(p!=null)
			p.Log(tag, msg);
		else
			System.out.println(tag+"："+msg);
	}
	public static void LogException(String tag,Exception e)
	{
		String es=e.getLocalizedMessage()+e.getMessage()+"\n";
		StackTraceElement[] ss = e.getStackTrace();
		for(StackTraceElement s:ss)
			es+="\n"+s.toString();
		if(p!=null)
			p.Log(tag, es);
		else
			System.out.println(tag+"："+es);
	}
	
	public static void Check (boolean con,String msg) throws Exception
	{
		if(con)
			throw new Exception(msg);
	}
	
	public static void writeToFile(String filename,InputStream inStream) throws IOException
	{
		utils.P("writeToFile",filename);
		FileOutputStream fs=null;
		try {
			File f = new File(filename);
			if (f.exists()) {
				utils.P("file exist,deleted:",filename);
				f.delete();
			}
			fs = new FileOutputStream(filename);
			byte[] buffer = new byte[1024];
	        int readnum=inStream.read(buffer);
	        while ( readnum >0 ) {
				fs.write(buffer, 0, readnum);
				readnum=inStream.read(buffer);
			}
		} catch (IOException e) {
			throw e;
		} finally{
	        try {
	        	if(fs!=null)
	        	{
					fs.flush();
			        fs.close();
	        	}
		        inStream.close(); 			
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void unzipFile(String targetPath, InputStream zipFileStream) {

		try {
			ZipInputStream zis = new ZipInputStream(zipFileStream);
			ZipEntry entry = null;
			P("unzipFile","开始解压到:" + targetPath + "...");
			while ((entry = zis.getNextEntry()) != null) {
				String zipPath = entry.getName();
				try {
					if (entry.isDirectory()) {
						File zipFolder = new File(targetPath + File.separator
								+ zipPath);
						if (!zipFolder.exists()) {
							zipFolder.mkdirs();
						}
					} else {
						File file = new File(targetPath + File.separator
								+ zipPath);
						if (!file.exists()) {
							File pathDir = file.getParentFile();
							pathDir.mkdirs();
							file.createNewFile();
						}

						FileOutputStream fos = new FileOutputStream(file);
						int bread;
						while ((bread = zis.read()) != -1) {
							fos.write(bread);
						}
						fos.close();

					}
					P("unzipFile", "成功解压:" + zipPath);

				} catch (Exception e) {
					P("unzipFile", "解压" + zipPath + "失败");
					continue;
				}
			}
			zis.close();
			zipFileStream.close();
			System.out.println("解压结束");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void checkFolderExists(String path) {
		checkFolderExists(path,true);
	}
	public static void checkFolderExists(String path,boolean pathisdir) {
		File file = new File(path);
		if (!file.exists()) {
			utils.P("checkFolderExists", path);
			String[] ds=path.split("/");
			int len=ds.length;
			if(len==0)return;
			String dir="";
			int dirlen=pathisdir?len:len-1;
			for(int i=1;i<dirlen;i++) {
				String sd=ds[i];
				if (sd != null && sd != "") {
					dir += "/" + sd;
					utils.P("Dir", dir);
					file = new File(dir);
					if (!file.exists()) {
						utils.P("mkdir", dir);
						file.mkdirs();
					}
				}
			}
		}
	}
}