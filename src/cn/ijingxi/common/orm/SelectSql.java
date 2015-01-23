package cn.ijingxi.common.orm;

import java.util.ArrayList;
import cn.ijingxi.common.util.LinkNode;
import cn.ijingxi.common.util.jxCompare;
import cn.ijingxi.common.util.jxLink;
import cn.ijingxi.common.util.utils;

/**
 * 多表联合查询条件，目前还不支持or连接
 * @author andrew
 *
 */
public class SelectSql
{
	jxLink<String,Integer> Tables=new jxLink<String,Integer>();
	//目前假定，由and组合的各组or链
	jxLink<Integer,contionLink> con=new jxLink<Integer,contionLink>();
	public ArrayList<Object> params=new ArrayList<Object>();
	
	public void AddTable(String ClassName)
	{
		ORMClassAttr a=jxORMobj.getClassAttr(ClassName);
		if(a.DBTableName!=null&&!Tables.Exist(a.DBTableName))
		{
			Tables.addByRise(a.DBTableName, Tables.getCount()+1);
			ORMClassAttr p=jxORMobj.getSuperClassAttr(ClassName);
			if(p!=null)
			{
				AddTable(p.DBTableName);
				//如果存在继承则必须只能有一个主键进行链接
				AddContion(a.DBTableName,a.PrimaryKeys.get(0),p.DBTableName,p.PrimaryKeys.get(0));
			}
		}
	}
	public void AddContion(String ClassName,String ColName,jxCompare cp,Object value)
	{
		AddContion(0,ClassName,ColName,cp,value);
	}
	public void AddContion(Integer LinkID,String ClassName,String ColName,jxCompare cp,Object value)
	{
		String cn=jxORMobj.GetClassName(ClassName,ColName);		
		selectContion sc=new selectContion(cn,ColName,cp,jxORMobj.Encrypte(ClassName, ColName, value));
		AddContion(LinkID,sc);
	}
	public void AddContion(Integer LinkID,selectContion sc)
	{
		contionLink cl=con.search(LinkID);
		if(cl==null)
		{
			cl=new contionLink();
			cl.isOr=true;
			con.addByRise(LinkID, cl);
		}
		cl.conList.add(sc);
	}
	public void AddContion(String ClassName,String ColName,String OtherClassName,String OtherColName)
	{
		AddContion(0,ClassName,ColName,OtherClassName,OtherColName);
	}
	public void AddContion(Integer LinkID,String ClassName,String ColName,String OtherClassName,String OtherColName)
	{
		String cn=jxORMobj.GetClassName(ClassName,ColName);
		String ocn=jxORMobj.GetClassName(OtherClassName,OtherColName);
		selectContion sc=new selectContion(cn,ColName,ocn,OtherColName);
		AddContion(LinkID,sc);
	}

	/**
	 * 查找某个对象，要注意可能有继承
	 * @return
	 * @throws Exception 
	 */
	public String GetSql(String ClassName) throws Exception
	{
		String select=null;
		ORMClassAttr a=jxORMobj.getClassAttr(ClassName);
		if(a.DBTableName!=null)
			select=GetFullName(a.DBTableName,"*");
		ORMClassAttr p=jxORMobj.getSuperClassAttr(ClassName);
		while(p!=null)
		{
			if(p.DBTableName!=null)
				select=utils.StringAdd(select, ",",GetFullName(p.DBTableName,"*"));
			p=jxORMobj.getSuperClassAttr(p.ClsName);
		}
		return "Select "+select+" From "+GetSql_From()+" Where "+GetSql_Where();		
	}
	String GetSql_Where() throws Exception
	{
		String w=null;
		for(LinkNode<Integer,contionLink> node : con)
		{
			if(w==null)
				w=node.getValue().GetSql();
			else
				w+=" And "+node.getValue().GetSql();
		}
		return w;
	}
	String GetSql_From() throws Exception
	{
		String f=null;
		for(LinkNode<String,Integer> node:Tables)
		{
			if(f==null)
				f=node.getKey()+" AS "+GetTableAlias(node.getKey());
			else
				f+=","+node.getKey()+" AS "+GetTableAlias(node.getKey());
		}
		return f;
	}
	
	
	
	String GetTableAlias(String TableName) throws Exception
	{
		Integer t=Tables.search(TableName);
		if(t==0)
			throw new Exception("表名不存在："+TableName);
		return "t"+t;
	}
	String GetFullName(String TableName,String ColName) throws Exception
	{		
		return GetTableAlias(TableName)+"."+ColName;
	}
	

class selectContion
{
	String TableName=null;
	String ColName=null;
	jxCompare cp=jxCompare.Equal;
	boolean cpValue=true;
	Object value=null;
	String OtherTableName=null;
	String OtherColName=null;
	selectContion(String TableName,String ColName,jxCompare cp,Object value)
	{
		this.TableName=TableName;
		this.ColName=ColName;
		this.cp=cp;
		this.value=value;
	}
//只能用于两表之间的链接
selectContion(String TableName,String ColName,String OtherTableName,String OtherColName)
{
	this.TableName=TableName;
	this.ColName=ColName;
	this.cp=jxCompare.Equal;
	this.cpValue=false;
	this.OtherTableName=OtherTableName;
	this.OtherColName=OtherColName;
}

	String GetSql() throws Exception
	{
		String sc=GetFullName(TableName,ColName);
		if(sc==null)
			return null;
		sc+=utils.TransCompareToString(cp);
		if(cpValue)
		{
			sc +=" ?";
			params.add(value);
		}
		else
		{
			String sco=GetFullName(OtherTableName,OtherColName);
			if(sco==null)
				return null;
			sc += sco;
		}
		return sc;
	}
}

class contionLink
{
	ArrayList<selectContion> conList=new ArrayList<selectContion>();
	boolean isOr=false;
	String GetSql() throws Exception
	{
		String sc=null;
		String op=isOr?" Or ":" And ";
		for(selectContion c:conList)			
				sc=utils.StringAdd(sc, op, c.GetSql());		
		return "("+sc+")";
	}
}

}

