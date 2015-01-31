
package cn.ijingxi.common.app;

import java.util.*;

import cn.ijingxi.common.Process.IExecutor;
import cn.ijingxi.common.Process.PI;
import cn.ijingxi.common.Process.jxProcess;
import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.util.jxCompare;

//容器和角色的关系是一一映射
//角色和角色之间的关系是组成关系Contain，即技术部经理由二级经理、技术审核等多个不具体映射到人员的角色组成
//角色和人员的关系是一对一映射
public class Organize extends Container
{
	public static final String RoleName_Creater="拥有者";
	public static final String RoleName_Admin="管理员";
	public static final String RoleName_Assist="协助管理者";
	public static final String RoleName_Manager="经理";
	public static final String RoleName_Agency="代理人";
	
	
	protected Organize()
	{
		super();
		TypeName="Organize";
		ContainerType=ContainerType_Organize;
	}
	
	public static ORMID GetORMID(Integer ID)
	{
		return new ORMID(GetTypeID("Organize"),ID);
	}
	
	/**
	 * 要在Container之后执行
	 * @throws Exception
	 */
	public static void Init() throws Exception{	InitClass(Organize.class);}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(Organize.class);
	}
	
	
	public Role CreateRole(String RoleName) throws Exception
	{
		Role r=(Role) New(Role.class);
		r.Name=RoleName;
		r.TopSpaceID=TopSpaceID;
		DB db=JdbcUtils.GetDB();
        synchronized (db)
        {		
        	r.Insert(db);
        	Relation rl=(Relation)New(Relation.class);
        	rl.ContainerID=ID;
        	rl.TargetContainerID=r.ID;
        	rl.RelationType=RelationType.OneToOne.ordinal();
        	rl.Insert(db);
        	db.Release();
        }
        return r;
	}
	public Organize CreateSubOrganize(String OrganizeName) throws Exception
	{
		Organize r=(Organize) New(Organize.class);
		r.Name=OrganizeName;
		r.TopSpaceID=TopSpaceID;
		DB db=JdbcUtils.GetDB();
        synchronized (db)
        {		
        	r.Insert(db);
        	Relation rl=(Relation)New(Relation.class);
        	rl.ContainerID=ID;
        	rl.TargetContainerID=r.ID;
        	rl.RelationType=RelationType.OneToMulti.ordinal();
        	rl.Insert(db);
        	db.Release();
        }
        return r;
	}
	
	//@ORM(keyType=KeyType.PrimaryKey)
	//public int ID;

	public Organize GetParentOrganize() throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation");
		s.AddTable("Organize");
		s.AddContion("Relation", "TargetContainerID", jxCompare.Equal, ID);
		s.AddContion("Relation", "ContainerID", "Organize","ID");
		return (Organize) Get(Organize.class,s);
	}
	public Organize GetTopOrganize() throws Exception
	{
		Organize rs=this;
		Organize p=GetParentOrganize();
		while(p!=null)
		{
			p=rs;
			p=GetParentOrganize();			
		}
		return rs;
	}
	
	/**
	 * 某组织所有设置的实际职位
	 * @param ContainID
	 * @return
	 * @throws Exception
	 */
	public Queue<jxORMobj> ListRole() throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation");
		s.AddTable("Role");
		s.AddContion("Relation", "ContainerID", jxCompare.Equal, ID);
		s.AddContion("Relation", "TargetContainerID", "Role","ID");
		return Select(Role.class,s);
	}	
	public People GetRealMapTo(String RoleName) throws Exception
	{
		Queue<jxORMobj> rl=ListRole();
		for(jxORMobj obj:rl)
		{
			//检查其所有组成的真实角色中是否有符合的，如检查技术部中技术部经理是谁
			if(((Organize)obj).Name.compareTo(RoleName)==0)
				return ((Role)obj).GetMapTo();
			Queue<jxORMobj> crl=((Role)obj).ListContainRole();
			for(jxORMobj ro:crl)
				//检查其所有组成的虚拟角色中是否有符合的，如检查技术部中经理是谁
				if(((Role)ro).Name.compareTo(RoleName)==0)
					return ((Role)obj).GetMapTo();			
		}
		return null;
	}
	public Queue<jxORMobj> ListRealMapTo(String RoleName) throws Exception
	{
		Queue<jxORMobj> rl=ListRole();
		for(jxORMobj obj:rl)
		{
			//检查其所有组成的真实角色中是否有符合的，如检查技术部中技术部经理是谁
			if(((Role)obj).Name.compareTo(RoleName)==0)
				return ((Role)obj).ListMapTo();
			Queue<jxORMobj> crl=((Role)obj).ListContainRole();
			for(jxORMobj ro:crl)
				//检查其所有组成的虚拟角色中是否有符合的，如检查技术部中经理是谁
				if(((Role)ro).Name.compareTo(RoleName)==0)
					return ((Role)obj).ListMapTo();			
		}
		return null;
	}
	public Queue<jxORMobj> ListPeople() throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation");
		s.AddTable("People");
		s.AddContion("Relation", "ContainerID", jxCompare.Equal, ID);
		s.AddContion("Relation", "TargetContainerID", "People","ID");
		return Select(People.class,s);
	}	
	public Queue<jxORMobj> ListSubOrganize() throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation");
		s.AddTable("Organize");
		s.AddContion("Relation", "ContainerID", jxCompare.Equal, ID);
		s.AddContion("Relation", "TargetContainerID", "Organize","ID");
		return Select(Organize.class,s);
	}	
	public Queue<jxORMobj> ListProcess() throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation");
		s.AddTable("jxProcess");
		s.AddContion("Relation", "ContainerID", jxCompare.Equal, ID);
		s.AddContion("Relation", "TargetContainerID", "jxProcess","ID");
		return Select(jxProcess.class,s);
	}	
	
	
	

	/**
	 * 查询当前正在做的流程，和上面各函数不同，本函数只能运行在组织经理的手机上！！
	 * @param Caller
	 * @return 返回null是没有权限！！
	 * @throws Exception
	 */
	public Queue<jxORMobj> ListPI(IExecutor Caller) throws Exception
	{
		if(Caller.CheckRight(this, Role.RoleName_Manager)||Caller.CheckRight(this, Role.RoleName_Owner))
		{
			SelectSql s=new SelectSql();
			s.AddTable("Relation");
			s.AddTable("PI");
			s.AddContion("Relation", "ContainerID", jxCompare.Equal, ID);
			s.AddContion("Relation", "TargetContainerID", "PI","ID");
			return Select(PI.class,s);
		}
		return null;
	}	
	
	
}