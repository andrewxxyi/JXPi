
package cn.ijingxi.common.orm;

import java.sql.*;
import java.util.Date;
import java.util.UUID;

import cn.ijingxi.common.util.Trans;
import cn.ijingxi.common.util.utils;

public class JdbcSqlite implements DB
{
	private String DBName=null;
	private Connection conn=null;
	
	public void SetURL(String url){}
	public void SetDBName(String dbname){DBName=dbname;}
	public void SetUserID(String uid){}
	public void SetPasswd(String pwd){}
	
	public Integer GetGeneratedKey(Connection conn,String IDName) throws SQLException
	{
		String sql = "Select last_insert_rowid()";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		Integer id = rs.getInt(1);
		rs.close();
		return id;
	}
	public Connection GetConnection() throws ClassNotFoundException, SQLException
	{
		if(conn==null)
		{
			Class.forName("org.sqlite.JDBC");
			conn=DriverManager.getConnection("jdbc:sqlite:"+DBName+".db");
			conn.setAutoCommit(false);
		}
		return conn;
	}
	public void ReleaseConnection(Connection conn) throws SQLException
	{
		conn.commit();
	}
	@Override
	public Object TransValueFromJavaToDB(Object value) 
	{
		if(value==null)return null;
		Class<?> cls=value.getClass();
		//先判断是否是枚举值
		if(utils.JudgeIsEnum(cls))
			return ((Enum<?>)value).ordinal();
		
		String n=utils.GetClassName(cls);
		switch(n)
		{
		case "Date":
			return ((Date)value).getTime()/1000;
		case "boolean":
		case "Boolean":
			return (boolean)value ? 1 :0; 
		case "UUID":
			return Trans.TransToByteArray((UUID) value);
		}
		return value;
	}

	@Override
	public Object TransValueFromDBToJava(FieldAttr fa,Object value)
	{
		if(fa!=null&&fa.IsEnum)
			return Trans.TransTojxEunm(fa.FieldType, value);
		String n=utils.GetClassName(fa.FieldType);
		return TransValueFromDBToJava(n,value);
	}
	Object TransValueFromDBToJava(String DestTypeName,Object value) 
	{
		if(value==null)return null;
		switch(DestTypeName)
		{
		case "byte":
		case "Byte":
			return Byte.parseByte(String.valueOf(value));
		case "short":
		case "Short":
			return Short.parseShort(String.valueOf(value));
		case "int":
		case "Integer":
			return (Integer)value;
		case "long":
		case "Long":
			return Long.parseLong(String.valueOf(value));
		case "float":
		case "Float":
			return Float.parseFloat(String.valueOf(value));
		case "double":	
		case "Double":	
			return Double.parseDouble(String.valueOf(value));
		case "String":	
			return (String) value;
		case "[B":	
			return (byte[]) value;
		case "Date":
			return  new Date(Long.parseLong(String.valueOf(value))*1000);
		case "boolean":
		case "Boolean":
			return (Integer)value !=0;
		case "UUID":
			return Trans.TransToUUID((byte[]) value,0);
		}
		return value;
	}
	@Override
	public String TransDataTypeFromJavaToDB(Class<?> cls) 
	{
		if(utils.JudgeIsEnum(cls))
			return "INTEGER";
		String cn=utils.GetClassName(cls);
		switch(cn)
		{
		case "int":
		case "Integer":
		case "byte":
		case "Byte":
		case "short":
		case "Short":
		case "long":
		case "Long":			
		case "boolean":			
		case "Boolean":
		case "Date":	
			return "INTEGER";
		case "float":
		case "Float":
		case "double":	
		case "Double":	
			return "REAL";
		case "String":
		case "char":
			return "TEXT";
		case "[B":	
		case "UUID":	
			return "BLOB";
		}
		return null;
	}
	@Override
	public String GetDBGeneratedSQL() 
	{
		return " autoincrement";
	}

	
}