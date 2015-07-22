package cn.ijingxi.common.orm;

import cn.ijingxi.common.app.TopSpace;
import cn.ijingxi.common.util.LinkNode;
import cn.ijingxi.common.util.jxCompare;
import cn.ijingxi.common.util.jxLink;
import cn.ijingxi.common.util.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 多表联合查询条件，目前还不支持or连接
 * @author andrew
 *
 */
public class SelectSql
{
	int Limit=0;
	int Offset=0;
	//jxLink<String,String> clsToDBTablename=new jxLink<String,String>();
	jxLink<String,Integer> Tables=new jxLink<String,Integer>();
	//目前假定，由or组合的各组and链
	jxLink<Integer,contionLink> con=new jxLink<Integer,contionLink>();
	public Queue<Object> params=new LinkedList<Object>();
	
	public void setLimit(int Limit){this.Limit=Limit;}
	public void setOffset(int Offset){this.Offset=Offset;}
	/**
	 * 用的是类名
	 * @param ClassName
	 */
	public void AddTable(String ClassName,TopSpace ts)
	{
		//ORMClassAttr a=jxORMobj.getClassAttr(ClassName);
		//String dbtable=a.getDBTableName(ts);
		//if(dbtable!=null&&!Tables.Exist(dbtable))
		if(!Tables.Exist(ClassName))
		{
			//clsToDBTablename.addByRise(ClassName, dbtable);
			ORMClassAttr a=jxORMobj.getClassAttr(ClassName);
			if(a==null)
			{
				utils.P(ClassName, "没有进行rom定义或初始化");
				return;
			}
			if(a.dbTableName!=null)
				Tables.addByRise(ClassName, Tables.getCount()+1);
			ORMClassAttr p=jxORMobj.getClassAttr(a.SuperClassName);
			if(p!=null)
			{
				AddTable(p.ClsName,ts);
				//如果存在继承则必须只能有一个主键进行链接
				//selectContion sc=new selectContion(dbtable,a.PrimaryKeys.get(0),p.getDBTableName(ts),p.PrimaryKeys.get(0));
				if(a.dbTableName!=null)
				{
					selectContion sc=new selectContion(ClassName,a.PrimaryKeys.get(0),p.ClsName,p.PrimaryKeys.get(0));
					AddContion(0,sc);
				}
			}
		}
	}
	public void AddContion(String ClassName,String ColName,jxCompare cp,Object value) throws Exception
	{
		AddContion(0,ClassName,ColName,cp,value);
	}
	public void AddContion(Integer LinkID,String ClassName,String ColName,jxCompare cp,Object value) throws Exception
	{
		//utils.P(ClassName, ColName);
		
		String cn=jxORMobj.GetClassName(ClassName,ColName);
		DB db=JdbcUtils.GetDB();
		ORMClassAttr a=jxORMobj.getClassAttr(cn);
		if(a==null)
			throw new Exception("ORM类在使用前必须先初始化："+cn);
		FieldAttr fa = a.Fields.get(ColName);
		selectContion sc=new selectContion(cn,ColName,cp,db.TransValueFromJavaToDB(fa,jxORMobj.Encrypte(ClassName, ColName, value)));
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
	public String GetSql(DB db,String ClassName,TopSpace ts) throws Exception
	{
		String select=null;
		select=GetFullName(ClassName,"*");
		ORMClassAttr p=jxORMobj.getSuperClassAttr(ClassName);
		while(p!=null)
		{
			if(p.dbTableName!=null)
				select=utils.StringAdd(select, ",",GetFullName(p.ClsName,"*"));
			p=jxORMobj.getSuperClassAttr(p.ClsName);
		}
		String sql="Select "+select+" From "+GetSql_From(ts)+GetSql_Where(db);		
		if(Limit>0)
		{
			sql+=" LIMIT "+Limit+" OFFSET "+Offset;
			Offset+=Limit;
		}
		return sql;
	}
	public String GetSql_Count(DB db,String ClassName,TopSpace ts) throws Exception
	{
		String sql="Select Count(*) From "+GetSql_From(ts)+GetSql_Where(db);		
		return sql;
	}
	/**
	 * 有可能会出现没有where子句的可能
	 * @param db
	 * @return
	 * @throws Exception
	 */
	String GetSql_Where(DB db) throws Exception
	{
		String w="";
		for(LinkNode<Integer,contionLink> node : con)
		{
			if(w.compareTo("")==0)
				w=" Where "+node.getValue().GetSql(db);
			else
				w+=" OR "+node.getValue().GetSql(db);
		}
		return w;
	}
	String GetSql_From(TopSpace ts) throws Exception
	{
		String f=null;
		for(LinkNode<String,Integer> node:Tables)
		{
			String clsname=node.getKey();
			ORMClassAttr a=jxORMobj.getClassAttr(clsname);
			String dtn=GetTableAlias(clsname);
			if(dtn!=null)
				if(f==null)
					f=a.getDBTableName(ts)+" AS "+dtn;
				else
					f+=","+a.getDBTableName(ts)+" AS "+dtn;
		}
		return f;
	}

	String GetTableAlias(String ClassName) throws Exception
	{
		if(ClassName==null)return null;
		Integer t=Tables.search(ClassName);
		if(t==null)
			return null;
		return "t"+t;
	}
	String GetFullName(String ClassName,String ColName) throws Exception
	{
		String alias=null;
		if(ColName.compareTo("*")==0)
			alias=GetTableAlias(ClassName);
		else
		{
			//如果属性位于父类中，则引用父类
			String cn=jxORMobj.GetClassName(ClassName,ColName);
			alias=GetTableAlias(cn);
		}
		if(alias!=null)
			return alias+"."+ColName;
		return null;
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
	
		String GetSql(DB db) throws Exception
		{
			String sc=GetFullName(ClassName,ColName);
			if(sc==null)
				return null;
			sc+=db.TransCompareToString(cp);
			if(cpValue)
			{
				sc +=" ?";
				//ORMClassAttr a=jxORMobj.getClassAttr(ClassName);
				//if(a==null)
				//	throw new Exception("ORM类在使用前必须先初始化："+ClassName);
				//FieldAttr fa = a.Fields.get(ColName);
				//params.offer(db.TransValueFromJavaToDB(fa,value));
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
		String GetSql(DB db) throws Exception
		{
			String sc=null;
			String op=isOr?" Or ":" And ";
			for(selectContion c:conList)			
					sc=utils.StringAdd(sc, op, c.GetSql(db));		
			return "("+sc+")";
		}
	}

}

