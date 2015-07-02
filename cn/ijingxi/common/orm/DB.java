package cn.ijingxi.common.orm;

import java.sql.*;
import java.util.Queue;

import cn.ijingxi.common.util.jxCompare;
import cn.ijingxi.common.util.jxLink;

public interface DB
{
	public void SetURL(String url);
	public void SetDBName(String dbname);
	public void SetUserID(String uid);
	public void SetPasswd(String pwd);
	
	public Integer GetGeneratedKey(String IDName) throws SQLException;

	public void Trans_Begin();
	public void Trans_Commit();
	public void Trans_Cancel();
	
	
	public void Release() throws SQLException;
	//Connection最好只用于内部，用户只用Release
	//public Connection GetConnection();
	//public void ReleaseConnection(Connection conn) throws SQLException;
	//用于对象值与数据库之间的转换
	public Object TransValueFromJavaToDB(FieldAttr fa, Object value);
	public Object TransValueFromDBToJava(FieldAttr fa, Object value);
	//执行delete、update、insert命令
	public boolean Exec(String sql,Queue<Object> param) throws Exception;
	//执行select命令
	public Queue<jxLink<String, Object>> Search(Class<?> cls, String sql,Queue<Object> param) throws Exception;
	//执行select count命令
	public int GetRS_int(String sql,Queue<Object> param) throws Exception;

	//
	//下面用于生成数据表
	//
	//用于数据类型的转换
	//public String TransDataTypeFromJavaToDB(String TypeName);
	//用于自增长列的定义
	public String GetDBGeneratedSQL();
	public String TransDataTypeFromJavaToDB(Class<?> cls);
	public String TransCompareToString(jxCompare cp);
	
}