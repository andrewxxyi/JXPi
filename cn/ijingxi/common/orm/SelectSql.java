package cn.ijingxi.common.orm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import cn.ijingxi.common.app.TopSpace;
import cn.ijingxi.common.util.LinkNode;
import cn.ijingxi.common.util.Trans;
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
	int Limit=0;
	int Offset=0;
	jxLink<String,String> clsToDBTablename=new jxLink<String,String>();
	jxLink<String,Integer> Tables=new jxLink<String,Integer>();
	//目前假定，由or组合的各组and链
	jxLink<Integer,contionLink> con=new jxLink<Integer,contionLink>();
	public Queue<Object> params=new LinkedList<Object>();
	/**
	 * 用的是类名
	 * @param ClassName
	 */
	public void AddTable(String ClassName,TopSpace ts)
	{
		ORMClassAttr a=jxORMobj.getClassAttr(ClassName);
		if(a.getDBTableName(ts)!=null&&!Tables.Exist(a.getDBTableName(ts)))
		{
			clsToDBTablename.addByRise(ClassName, a.getDBTableName(ts));
			Tables.addByRise(a.getDBTableName(ts), Tables.getCount()+1);
			ORMClassAttr p=jxORMobj.getClassAttr(a.SuperClassName);
			if(p!=null)
			{
				AddTable(p.ClsName,ts);
				//如果存在继承则必须只能有一个主键进行链接
				selectContion sc=new selectContion(a.getDBTableName(ts),a.PrimaryKeys.get(0),p.getDBTableName(ts),p.PrimaryKeys.get(0));
				AddContion(0,sc);
			}
		}
	}
	public void AddContion(String ClassName,String ColName,jxCompare cp,Object value) throws Exception
	{
		AddContion(0,ClassName,ColName,cp,value);
	}
	public void AddContion(Integer LinkID,String ClassName,String ColName,jxCompare cp,Object value) throws Exception
	{
		String cn=jxORMobj.GetClassName(ClassName,ColName);
		DB db=JdbcUtils.GetDB();
		selectContion sc=new selectContion(cn,ColName,cp,db.TransValueFromJavaToDB(jxORMobj.Encrypte(ClassName, ColName, value)));
		AddContion(LinkID,sc);
	}
	public void AddContion(Integer LinkID,selectContion sc)
	{
		contionLink cl=con.search(LinkID);
		if(cl==null)
		{
			cl=new contionLink();
			con.addByRise(LinkID, cl);
		}
		cl.conList.add(sc);
	}
	public void AddContion(String ClassName,String ColName,String OtherClassName,String OtherColName)
	{
		AddContion(0,ClassName,ColName,OtherClassName,OtherColName);
	}
	/**
	 * 在有继承的情况下，会自动寻找到属性所在的表进行条件关联
	 * @param LinkID
	 * @param ClassName
	 * @param ColName
	 * @param OtherClassName
	 * @param OtherColName
	 */
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
	public String GetSql(String ClassName,TopSpace ts) throws Exception
	{
		String select=null;
		ORMClassAttr a=jxORMobj.getClassAttr(ClassName);
		if(a.getDBTableName(ts)!=null)
			select=GetFullName(a.getDBTableName(ts),"*");
		ORMClassAttr p=jxORMobj.getSuperClassAttr(ClassName);
		while(p!=null)
		{
			if(p.getDBTableName(ts)!=null)
				select=utils.StringAdd(select, ",",GetFullName(p.getDBTableName(ts),"*"));
			p=jxORMobj.getSuperClassAttr(p.ClsName);
		}
		String sql="Select "+select+" From "+GetSql_From()+" Where "+GetSql_Where();		
		if(Limit>0)
		{
			sql+=" LIMIT "+Limit+" OFFSET "+Offset;
			Offset+=Limit;
		}
		return sql;
	}
	String GetSql_Where() throws Exception
	{
		String w=null;
		for(LinkNode<Integer,contionLink> node : con)
		{
			if(w==null)
				w=node.getValue().GetSql();
			else
				w+=" OR "+node.getValue().GetSql();
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
	
	
	
	String GetTableAlias(String ClassName) throws Exception
	{
		String dt=clsToDBTablename.search(ClassName);
		if(dt==null)return null;
		Integer t=Tables.search(dt);
		if(t==0)
			throw new Exception("表名不存在："+ClassName);
		return "t"+t;
	}
	String GetFullName(String TableName,String ColName) throws Exception
	{		
		return GetTableAlias(TableName)+"."+ColName;
	}
	

class selectContion
{
	String ClassName=null;
	String ColName=null;
	jxCompare cp=jxCompare.Equal;
	boolean cpValue=true;
	Object value=null;
	String OtherClassName=null;
	String OtherColName=null;
	
	selectContion(String ClassName,String ColName,jxCompare cp,Object value)
	{
		this.ClassName=ClassName;
		this.ColName=ColName;
		this.cp=cp;
		this.value=value;
	}
//只能用于两表之间的链接
selectContion(String ClassName,String ColName,String OtherClassName,String OtherColName)
{
	this.ClassName=ClassName;
	this.ColName=ColName;
	this.cp=jxCompare.Equal;
	this.cpValue=false;
	this.OtherClassName=OtherClassName;
	this.OtherColName=OtherColName;
}

	String GetSql() throws Exception
	{
		String sc=GetFullName(ClassName,ColName);
		if(sc==null)
			return null;
		sc+=Trans.TransCompareToString(cp);
		if(cpValue)
		{
			sc +=" ?";
			params.offer(value);
		}
		else
		{
			String sco=GetFullName(OtherClassName,OtherColName);
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

