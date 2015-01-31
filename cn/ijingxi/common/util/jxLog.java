
package cn.ijingxi.common.util;

import java.util.*;

import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;

public class jxLog extends jxORMobj
{
	protected jxLog() throws Exception {
		super();
	}

	public static void Init() throws Exception{	InitClass(jxLog.class);}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(jxLog.class);
	}
	
	@ORM(Index=1,Descr="相关人，或者是事件的触发者，或者是执行人，或者是目标人")
	public UUID CorrelationID;
	
	@ORM(Index=2)
	public int ContainerType;
	@ORM(Index=2)
	public int ContainerID;
	
	@ORM
	public String Descr;
	
	@ORM
	public int Event;

	@ORM(Index=3)
	public Date LogTime;
	
	@ORM(Descr="json格式的附加信息")
	public String Info;


	public static jxLog Log(UUID CorrelationID,int ContainerType,int ContainerID,int Event,String Descr) throws Exception
	{
		jxLog log=(jxLog) jxLog.New(jxLog.class);
		log.CorrelationID=CorrelationID;
		log.ContainerType=ContainerType;
		log.ContainerID=ContainerID;
		log.Event=Event;
		log.Descr=Descr;
		log.LogTime=new Date();
		log.Insert();
		return log;
	}
	public void AddInfo(String Purpose,Object value) throws Exception
	{
		setExtendValue("Info",Purpose,value);
	}
	
}