
package cn.ijingxi.common.app;

import java.util.*;

import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.Trans;

/**
 * 全局的，所有topspace共享
 * @author andrew
 *
 */
public class People extends jxORMobj 
{
	public People MainUser=null;
	public static ORMID GetORMID(UUID ID)
	{
		return new ORMID(ORMType.People.ordinal(),ID);
	}

	@Override
	protected void Init_Create() throws Exception
	{
		ID=UUID.randomUUID();
		CreateTime=new Date();
	}
	/**
	 * 要在Container之后执行
	 * @throws Exception
	 */
	public static void Init() throws Exception{	InitClass(ORMType.People.ordinal(),People.class);}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(People.class,null);
		People p=(People) jxORMobj.Create(People.class);
		//1号是手机主人，但如果某人在两台手机上都装了，则会出现冲突，需要加以解决
		p.ID=jxSystem.SystemID;
		p.Name="您的姓名，请在设置中加以修改";
		p.Insert(null);
	}
	//如果在两台手机上都安装了，则在同步时自动以CreateTime在前的进行覆盖
	@ORM(keyType=KeyType.PrimaryKey)
	public UUID ID;
	
	@ORM(Index=2)
	public String Name;		
	
	@ORM
	public String Descr;		
	
	@ORM(Index=3)
	public Date CreateTime;

	@ORM(Index=4)
	public String LoginName;

	@ORM(Index=5)
	public Date Birthday;
			
	@ORM
	public String Passwd;	
	
	@ORM(Descr="json格式的安全问题与答案：Question、Answer")
	public String Secure;
	
	@ORM(Descr="json格式信息保存字段，如联系方式，包括但不限于Mail、Tel、Mobile、Fax、Address、国家等等")
	public String Info;

	@ORM(Descr="girl:true")
	public Boolean Sex;
	
	@ORM
	public Boolean NoUsed;

	public void setConf(TopSpace ts,String Purpose,String value) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("tsid", Trans.TransToString(ts.ID));
		setExtendArrayValue("Info","TSConf",ks,Purpose,value);
	}

	public jxJson getConf(TopSpace ts) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("tsid", Trans.TransToString(ts.ID));
		List<jxJson> ls = getExtendArrayList("Info","TSConf",ks);
		if(ls.size()==1)
			return ls.get(0);
		return null;
	}
	public String getConf(TopSpace ts,String Purpose) throws Exception
	{
		jxJson nod=getConf(ts);
		if(nod!=null)
			return (String) nod.getSubObjectValue(Purpose);
		return null;
	}

	
}