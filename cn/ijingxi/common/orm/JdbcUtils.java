
package cn.ijingxi.common.orm;


public class JdbcUtils
{
	private static DB myDB=null;
	public static DB GetDB(DB db,Object Owner)
	{
		if(db!=null)return db;
		myDB.setOwner(Owner);
		return myDB;
	}
	public static void SetDB(DB db)
	{
		myDB=db;
	}
	
	
}