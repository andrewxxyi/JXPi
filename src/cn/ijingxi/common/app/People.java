
package cn.ijingxi.common.app;

import java.util.*;

import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.utils;

public class People extends Container
{
	/**
	 * 要在Container之后执行
	 * @throws Exception
	 */
	public static void Init() throws Exception{	InitClass(People.class);}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(People.class);
	}
	
	public People(){ContainerType=1;}
	
	@ORM(keyType=KeyType.PrimaryKey)
	public int ID;

	@ORM(Index=1)
	public String LoginName;

	@ORM(Index=2)
	public Date Birthday;
	
	@ORM(Index=3)
	public String UUIDSTR;
	public UUID getUUID()
	{
		if(UUIDSTR!=null)
			return utils.TransToUUID(UUIDSTR);
		return null;
	}
		
	@ORM
	public String Passwd;	
	
	@ORM(Descr="json格式的安全问题与答案：Question、Answer")
	public String Secure;
	
	@ORM(Descr="json格式的联系方式，包括但不限于Mail、Tel、Mobile、Fax、Address、国家等等")
	public String Contact;
		
	@ORM
	public Boolean NoUsed;
		
	
}