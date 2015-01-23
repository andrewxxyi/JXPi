package cn.ijingxi.common.orm;

import java.sql.*;

public interface DB
{
	public void SetURL(String url);
	public void SetDBName(String dbname);
	public void SetUserID(String uid);
	public void SetPasswd(String pwd);
	
	public Integer GetGeneratedKey(Connection conn,String IDName) throws SQLException;
	public Connection GetConnection() throws ClassNotFoundException, SQLException;
	public void ReleaseConnection(Connection conn) throws SQLException;
	//用于对象值与数据库之间的转换
	public Object TransValueFromJavaToDB(Object value);
	public Object TransValueFromDBToJava(Class<?> cls, Object value);


	//
	//下面用于生成数据表
	//
	//用于数据类型的转换
	//public String TransDataTypeFromJavaToDB(String TypeName);
	//用于自增长列的定义
	public String GetDBGeneratedSQL();
	public String TransDataTypeFromJavaToDB(Class<?> cls);}