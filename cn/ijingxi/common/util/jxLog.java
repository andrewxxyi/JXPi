
package cn.ijingxi.common.util;

import cn.ijingxi.common.app.jxSystem;
import cn.ijingxi.common.msg.jxMsg;
import cn.ijingxi.common.msg.jxMsgType;
import cn.ijingxi.common.orm.ORM;
import cn.ijingxi.common.orm.ORMID;
import cn.ijingxi.common.orm.ORMType;
import cn.ijingxi.common.orm.jxORMobj;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * 只在全局中有，
 * 
 * 统计分析在lanserver中每天晚上运行
 * 本机上只统计分析自己的
 * 
 * @author andrew
 *
 */
public class jxLog extends jxORMobj
{
	public static void Init() throws Exception{	InitClass(ORMType.jxLog.ordinal(),jxLog.class);}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(jxLog.class,null);
	}

	@ORM(Index=1)
	public UUID OwnerID;
	
	@ORM(Index=2)
	public int TypeID;
	@ORM(Index=2)
	public UUID ID;
	
	@ORM
	public String Name;
	
	@ORM
	public String Descr;

	@ORM(Index=3)
	public Date LogTime;
	
	@ORM(Descr="json格式的附加信息")
	public String Info;


	/**
	 * 尚未保存，最终设置完毕要注意保存
	 * @param OwnerID
	 * @param TypeID
	 * @param ID
	 * @param Name
	 * @param Descr
	 * @return
	 * @throws Exception
	 */
	public static jxLog Log(UUID OwnerID,int TypeID,UUID ID,String Name,String Descr) throws Exception
	{
		jxLog log=(jxLog) jxLog.Create(jxLog.class);
		log.OwnerID=OwnerID;
		log.TypeID=TypeID;
		log.ID=ID;
		log.Name=Name;
		log.Descr=Descr;
		log.LogTime=new Date();
		return log;
	}
	public void setInfo(String Purpose,Object value) throws Exception
	{
		setExtendValue("Info",Purpose,value);
	}
	
	public static jxMsg NewLogMsg(jxLog log,UUID Receiver) throws Exception
	{
		jxMsg msg=(jxMsg) jxMsg.Create(jxMsg.class);
		msg.Sender=jxSystem.System.ID;
		msg.setSenderID(ORMID.SystemID);
		msg.Receiver=Receiver;
		msg.setReceiverID(ORMID.SystemID);
		msg.MsgType=jxMsgType.Log;
		msg.setMsg(log.ToJSONString());
		return msg;
	}
	public static jxLog GetFromMsg(jxMsg msg) throws Exception
	{
		return (jxLog) Trans.TransFromJSONToJava(jxLog.class, msg.getMsg());
	}

	private static FileOutputStream logWriter=null;
	public static void setLogger(FileOutputStream logger) {
		logWriter=logger;
	}
	public static void Log(String tag,String msg) {
		if(logWriter!=null) {
			String str=(new Date()).toString()+"\t"+tag+"\t"+msg+"\n";
			try {
				byte [] bs = str.getBytes();
				logWriter.write(bs);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}