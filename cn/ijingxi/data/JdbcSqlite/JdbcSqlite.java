
package cn.ijingxi.data.JdbcSqlite;

import cn.ijingxi.common.orm.DB;
import cn.ijingxi.common.orm.FieldAttr;
import cn.ijingxi.common.util.*;

import java.sql.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class JdbcSqlite implements DB
{
	protected String DBName=null;
	private Connection conn=null;
	private Object myOwner=null;
	
	public void SetURL(String url){}
	public void SetDBName(String dbname){}
	public void SetUserID(String uid){}
	public void SetPasswd(String pwd){}

	public JdbcSqlite(boolean NoUsedForsoncall)
	{
	}
	
	public JdbcSqlite(String dbname) throws Exception
	{
		DBName=dbname;
		if(conn==null)
		{
			Class.forName("org.sqlite.JDBC");
			conn=DriverManager.getConnection("jdbc:sqlite:"+DBName+".db");
			conn.setAutoCommit(false);
		}
	}
	
	@Override
	public void Release(Object obj) throws SQLException
	{
		synchronized (this){
			if(obj==myOwner){
				myOwner=null;
				conn.commit();
			}
		}
	}

	public Integer GetGeneratedKey(String IDName) throws SQLException
	{
		String sql = "Select last_insert_rowid()";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		Integer id = rs.getInt(1);
		rs.close();
		return id;
	}
	public Connection GetConnection()
	{
		return conn;
	}
	public void ReleaseConnection(Connection conn) throws SQLException
	{
		//conn.commit();
	}
	

	@Override
	public void Trans_Begin()
	{
		//dbBackup.StartBuf();
	}
	@Override
	public void Trans_Commit() 
	{
		try {
			conn.commit();
			//dbBackup.EndBuf();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	@Override
	public void Trans_Cancel() 
	{
		try {
			conn.rollback();
			//dbBackup.CancalBuf();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	@Override
	public void setOwner(Object obj) {
		myOwner = obj;
	}


	@Override
	public Object TransValueFromJavaToDB(FieldAttr fa,Object value) 
	{
		//先判断是否是枚举值
		if(utils.JudgeIsEnum(fa.FieldType))
		{
			if(value==null)return 0;
			return ((Enum<?>)value).ordinal();
		}
		String n= utils.GetClassName(fa.FieldType);
		switch(n)
		{
		case "Date":
			if(value==null)return null;
			return Trans.TransToInteger((Date)value);
		case "boolean":
		case "Boolean":
			if(value==null)return 0;
			return ((boolean)value) ? 1 :0; 
		case "UUID":
			if(value==null)return null;
			return Trans.TransToString((UUID) value);
		case "byte":
		case "Byte":
			if(value==null)return 0;
			return value;
		case "short":
		case "Short":
			if(value==null)return 0;
			return value;
		case "int":
		case "Integer":
			if(value==null)return 0;
			return value;
		case "long":
		case "Long":
			if(value==null)return 0L;
			return value;
		case "float":
		case "Float":
			if(value==null)return 0F;
			return value;
		case "double":	
		case "Double":	
			if(value==null)return 0D;
			return value;
		}
		return value;
	}
	

	@Override
	public String TransCompareToString(jxCompare cp)
	{
		switch(cp)
		{
		case Equal:
			return "=";
		case NoEqual:
			return "<>";
		case Less:
			return "<";
		case LessEqual:
			return "<=";
		case Greate:
			return ">";
		case GreateEqual:
			return ">=";
		case Like:
			return "like";
		}
		return null;
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
		switch(DestTypeName)
		{
		case "byte":
		case "Byte":
			if(value==null)return 0;
			return Byte.parseByte(String.valueOf(value));
		case "short":
		case "Short":
			if(value==null)return 0;
			return Short.parseShort(String.valueOf(value));
		case "int":
		case "Integer":
			if(value==null)return 0;
			return (Integer)value;
		case "long":
		case "Long":
			if(value==null)return 0L;
			return Long.parseLong(String.valueOf(value));
		case "float":
		case "Float":
			if(value==null)return 0F;
			return Float.parseFloat(String.valueOf(value));
		case "double":	
		case "Double":	
			if(value==null)return 0D;
			return Double.parseDouble(String.valueOf(value));
		case "String":	
			if(value==null)return null;
			return (String) value;
		case "[B":	
			if(value==null)return null;
			return (byte[]) value;
		case "Date":
			if(value==null)return 0;
			return Trans.TransToDate((Integer)value);
		case "boolean":
		case "Boolean":
			if(value==null)return 0;
			return (Integer)value !=0;
		case "UUID":
			if(value==null)return null;
			return Trans.TransToUUID((String) value);
			//return Trans.TransToUUID((byte[]) value,0);
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
			return "BLOB";
		case "UUID":	
			return "TEXT";
		}
		return null;
	}

	@Override
	public String GetDBGeneratedSQL() 
	{
		return " autoincrement";
	}
	@Override
	public boolean Exec(String sql, Queue<Object> param) throws SQLException {
		jxLog.logger.debug(sql);
		synchronized (this) {
			PreparedStatement ps = conn.prepareStatement(sql);
			if (param != null) {
				int len = param.size();
				for (int i = 1; i <= len; i++) {
					Object obj = param.poll();
					ps.setObject(i, obj);
				}
			}
			return ps.execute();
		}
	}
	@Override
	public Queue<jxLink<String, Object>> Search(Class<?> cls, String sql, Queue<Object> param) throws Exception {
		synchronized (this) {
			Queue<jxLink<String, Object>> rs = new LinkedList<jxLink<String, Object>>();

			jxLog.logger.debug("Search:"+sql);

			PreparedStatement stmt = conn.prepareStatement(sql);
			int len = param.size();
			for (int i = 1; i <= len; i++) {
				Object obj = param.poll();
				//utils.P("Search set param", i + ":" + obj);
				stmt.setObject(i, obj);
			}
			ResultSet result = stmt.executeQuery();
			ResultSetMetaData rsMetaData = result.getMetaData();
			int numberOfColumns = rsMetaData.getColumnCount();
			while (result.next()) {
				jxLink<String, Object> map = new jxLink<String, Object>();
				for (int i = 1; i <= numberOfColumns; i++) {
					Object v = result.getObject(i);
					String cn = rsMetaData.getColumnName(i);
					map.offer(cn, v);
				}
				rs.offer(map);
			}
			//utils.P("Search result size", rs.size() + "");
			return rs;
		}
	}
	@Override
	public int GetRS_int(String sql, Queue<Object> param) throws Exception 
	{
		synchronized (this) {
			PreparedStatement stmt = conn.prepareStatement(sql);
			int len = param.size();
			for (int i = 1; i <= len; i++)
				stmt.setObject(i, param.poll());
			ResultSet result = stmt.executeQuery();
			while (result.next()) {
				Object v = result.getObject(1);
				if (v != null)
					return Trans.TransToInteger(v);
				return 0;
			}
			return 0;
		}
	}

	
}