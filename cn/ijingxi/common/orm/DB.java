package cn.ijingxi.common.orm;

import java.sql.*;

public interface DB
{
	public void SetURL(String url);
	public void SetDBName(String dbname);
	public void SetUserID(String uid);
	public void SetPasswd(String pwd);
	
	public Integer GetGeneratedKey(String IDName) throws SQLException;
	public void Release() throws SQLException;
	//Connection最好只用于内部，用户只用Release
	public Connection GetConnection();
	public void ReleaseConnection(Connection conn) throws SQLException;
	//用于对象值与数据库之间的转换
	public Object TransValueFromJavaToDB(Object value);
	public Object TransValueFromDBToJava(FieldAttr fa, Object value);


	//
	//下面用于生成数据表
	//
	//用于数据类型的转换
	//public String TransDataTypeFromJavaToDB(String TypeName);
	//用于自增长列的定义
	public String GetDBGeneratedSQL();
	public String TransDataTypeFromJavaToDB(Class<?> cls);
	
}