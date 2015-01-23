
package cn.ijingxi.common.orm;


public class JdbcUtils
{
	private static DB myDB=null;
	public static DB GetDB()
	{
		return myDB;
	}
	public static void SetDB(DB db)
	{
		myDB=db;
	}
	
	
}