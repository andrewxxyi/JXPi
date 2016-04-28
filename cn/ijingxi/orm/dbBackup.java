
package cn.ijingxi.orm;

import cn.ijingxi.app.ObjTag;
import cn.ijingxi.util.LinkNode;
import cn.ijingxi.util.Trans;
import cn.ijingxi.util.utils;

import java.io.*;
import java.util.*;

/**
 * 数据库备份，调用Backup_Start则增删改会自动进行进行备份
 * 
 * 目前所应采取的数据库同步方案是
 * 	1、主从方案，即从某一台手机开始所有数据都同步到这台手机上
 * 	2、如果有人请求同步数据，则主手机下载当前备份文件给从机
 * 	3、从机将除jxSystem表外的表都应清空，然后导入该文件，然后开启备份
 * 	4、从机添加数据都保存到备份文件中
 * 	5、从机导出增量备份文件
 * 	6、主机导入该增量文件，导入过程也应备份
 * 	7、主机将所以增量文件都同步完毕后再次导出分发给从机
 * 
 * @author andrew
 *
 */
public class dbBackup
{
	//哪些表不需被过滤
	private static Map<String,Integer> TableFilter=new HashMap<String,Integer>();	
	private static ByteArrayOutputStream backupBuf=null;
	private static ObjectOutputStream objos=null;
	private static FileOutputStream fos =null;
	
	//private static String bakPath=null;
	
	static
	{
		//addTableFilter(jxSystem.class);
	}
	
	public static void addTableFilter(Class<?> cls)
	{
		TableFilter.put(utils.GetClassName(cls), 1);
	}
	
	public static boolean needBackup(){return fos!=null;}
		
	/**
	 * 
	 * @param clsName
	 * @param sql
	 * @param param key是属性/列名，value是对象的原始值，而不能是可以直接保存到数据库中的值，因为该值是要保存到文件中，然后传递到另外的机器上，这时就无法知道该值的类型了
	 * @throws Exception
	 */
	public static void backup(String clsName,String sql,Map<String,Object> param) throws Exception
	{
		if(fos==null)return;
        synchronized (TableFilter)
        {		
			jxJson rs=jxJson.GetObjectNode(clsName);
			rs.setSubObjectValue("sql", sql);
			jxJson sub=jxJson.GetObjectNode("p");
			rs.AddSubObjNode(sub);
			for(String pn :param.keySet())
				sub.setSubObjectValue(pn, param.get(pn));
			if(objos!=null)
				objos.writeUTF(rs.TransToStringWithName());
			else
			{
				objos=new ObjectOutputStream(fos);
				objos.writeUTF(rs.TransToStringWithName());
				objos.flush();
				objos=null;
			}
        }
	}
	
	public static void StartBuf(){
		if(fos!=null)
	        synchronized (TableFilter)
	        {		
				backupBuf=new ByteArrayOutputStream();
				try {
					objos=new ObjectOutputStream(backupBuf);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	}
	public static void EndBuf()
	{
		if(fos!=null)
	        synchronized (TableFilter)
			{
				try {
					objos.flush();
					backupBuf.writeTo(fos);
					backupBuf.flush();
					backupBuf=null;
					objos=null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}

	public static void CancalBuf()
	{
		if(fos!=null)
	        synchronized (TableFilter)
			{
				backupBuf=null;
				objos=null;
			}
	}
	/**
	 * path是目录名，以"/"结尾
	 * @param Path
	 */
	public static void Backup_Start(String Path,boolean newfile)
	{
        synchronized (TableFilter)
        {
			//bakPath=Path;
			if(fos!=null)Backup_End();
			File file=null;
			if(!newfile)
			{
				File dir=new File(Path);  
				File[] fs=dir.listFiles();
				if(fs!=null)
					for(File f:fs)
					{
						if(file==null)
							file=f;
						else if(file.lastModified()>f.lastModified())
							file=f;
					}
			}
			if(newfile||file==null)
			{
				String bfname=Trans.TransToString(ObjTag.SystemID)+"_"+(new Date()).getTime();
				String fn=Path+bfname;
				file=new File(fn);  				
			}
			//判断文件是否存在  
	        if(!file.exists()){
	            try {  
	                file.createNewFile();  //创建文件  
	                fos=new FileOutputStream(file);
	                //fos.write("\n".getBytes());
	            } catch (IOException e) {  
	                // TODO Auto-generated catch block  
	                e.printStackTrace();  
	            }
	        }
        }
	}

	public static void Backup_End()
	{
        synchronized (TableFilter)
        {
        	if(fos==null)return;
        	try {
				fos.flush();
	        	fos.close();
	        	fos=null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
        
	/**
	 * 主机导入需要打开needBackup，而从机则应关闭needBackup
	 * @param db
	 * @param FileName
	 * @param needBackup 导入过程是否还需要备份
	 */
	public static void Recovery(DB db,String FileName,boolean needBackup)
	{
        synchronized (TableFilter)
        {
        	if(!needBackup)
        		Backup_End();
        	Map<String,Object> pl=null;
        	if(needBackup)
        		pl=new HashMap<String,Object>();
			File file=new File(FileName);
			if(file.exists())
	        synchronized (db)
	        {		
				try {
					ObjectInputStream oi=new ObjectInputStream(new FileInputStream(file));
					String str=oi.readUTF();
					while(str!=null)
					{
						jxJson temp=jxJson.JsonToObject(str);
						jxJson obj=temp.SubObjects().FirstValue();
						String clsname=obj.getName();
						if(TableFilter.containsKey(clsname))
							continue;					
						Queue<Object> ol=new LinkedList<Object>();
						jxJson p=obj.GetSubObject("p");
						for(LinkNode<String, jxJson> sub:p.SubObjects())
						{
							String fn=sub.getKey();
							FieldAttr fa = jxORMobj.getFieldAttr(clsname, fn);
							Object o=Trans.TransTo(fa.FieldType, sub.getValue());
							ol.add(db.TransValueFromJavaToDB(fa,o));
				        	if(needBackup)
				        		pl.put(fn, o);							
						}
						try
						{
							//由于都有主键，重复插入会导致错误，忽略则可
							String sql=(String) obj.GetSubValue("sql");
							jxORMobj.Exec(db,sql , ol);
				        	if(needBackup)
				        		backup(clsname,sql,pl);
						}catch (Exception e) {}
						str=oi.readUTF();
					}
					oi.close();				
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
        }
	}
        
}
	
		