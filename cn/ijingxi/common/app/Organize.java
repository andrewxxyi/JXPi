
package cn.ijingxi.common.app;

import cn.ijingxi.common.Process.FlowChart;
import cn.ijingxi.common.Process.FlowChartInstance;
import cn.ijingxi.common.Process.InstanceState;
import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.util.jxCompare;

import java.util.*;

//容器和角色的关系是一一映射
//角色和角色之间的关系是组成关系Contain，即技术部经理由二级经理、技术审核等多个不具体映射到人员的角色组成
//角色和人员的关系是一对一映射
//
//上级组织和下级组织是Contain关系
//但topspace和最顶层组织是OneToOne
//
public class Organize extends Container
{
	public static final String RoleName_Creater="拥有者";
	public static final String RoleName_Admin="管理员";
	public static final String RoleName_Assist="协助管理者";
	public static final String RoleName_Manager="经理";
	public static final String RoleName_Agency="代理人";
			
	public static ORMID GetORMID(UUID ID)
	{
		return new ORMID(ORMType.Organize.ordinal(),ID);
	}
	
	/**
	 * 要在Container之后执行
	 * @throws Exception
	 */
	public static void Init() throws Exception{
		InitClass(ORMType.Organize.ordinal(),Organize.class,"组织");
	}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(Organize.class);
	}	
	
	public Role CreateRole(People Caller,String RoleName) throws Exception
	{
		//if(!Caller.CheckRight(ts, Caller,Role.RoleName_Assist, false))
		//	throw new Exception(String.format("您没有权限执行该操作%s:%s","Organize","CreateRole"));
		Role r=(Role) Create(Role.class);
		r.Name=RoleName;
		DB db=JdbcUtils.GetDB(null,this);
		db.Trans_Begin();
		try{
	        synchronized (db)
	        {		
	        	r.Insert(db);
	        	Relation rl=(Relation)Create(Relation.class);
	        	rl.ObjTypeID=ORMType.Organize.ordinal();
	        	rl.ObjID=ID;
	        	rl.TargetTypeID=ORMType.Role.ordinal();
	        	rl.TargetID=r.ID;
	        	rl.RelType=RelationType.OneToMulti;
	        	rl.Insert(db);
	        }
	        db.Trans_Commit();
		}
		catch(Exception e)
		{
			db.Trans_Cancel();
		}
		db.Release(this);
        return r;
	}
	public Organize CreateSubOrganize(People Caller,String OrganizeName) throws Exception
	{
		//if(!Caller.CheckRight(ts,Caller, Role.RoleName_Assist, false))
		//	throw new Exception(String.format("您没有权限执行该操作%s:%s","Organize","CreateSubOrganize"));
		Organize r=(Organize) Create(Organize.class);
		r.Name=OrganizeName;
		DB db=JdbcUtils.GetDB(null,this);
		db.Trans_Begin();
		try{
			synchronized (db)
			{
				r.Insert(db);
				Relation rl=(Relation)Create(Relation.class);
				rl.ObjTypeID=ORMType.Organize.ordinal();
				rl.ObjID=ID;
				rl.TargetTypeID=ORMType.Organize.ordinal();
				rl.TargetID=r.ID;
				rl.RelType=RelationType.Contain;
				rl.Insert(db);
			}
			db.Trans_Commit();
		}
		catch(Exception e)
		{
			db.Trans_Cancel();
		}
		db.Release(this);
        return r;
	}

	//上级组织和下级组织是Contain关系
	//但topspace和最顶层组织是OneToOne
	public Organize GetParentOrganize() throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation");
		s.AddTable("Organize");
		s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.Organize.ordinal());
		s.AddContion("Relation", "TargetID", jxCompare.Equal, ID);
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.Organize.ordinal());
		s.AddContion("Relation", "ObjID", "Organize","ID");
		return (Organize) Get(Organize.class,s);
	}
	public Organize GetTopOrganize() throws Exception
	{
		Organize rs=this;
		Organize p=GetParentOrganize();
		while(p!=null)
		{
			rs=p;
			p=rs.GetParentOrganize();
		}		
		return rs;
	}
	//这个用不着，一定是当前的topspace
	public TopSpace GetTopSpace() throws Exception
	{
		if(myTopSpace!=null)return myTopSpace;
		Organize org=GetTopOrganize();
		SelectSql s=new SelectSql();
		s.AddTable("Relation");
		s.AddTable("TopSpace");
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.Organize.ordinal());
		s.AddContion("Relation", "ObjID", jxCompare.Equal, org.ID);
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.TopSpace.ordinal());
		s.AddContion("Relation", "ObjID", "TopSpace","ID");
		myTopSpace=(TopSpace) Get(TopSpace.class,s);
		return myTopSpace;
	}

	/**
	 * 某组织所有设置的实际职位
	 * @return
	 * @throws Exception
	 */
	public Queue<jxORMobj> ListRole() throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation");
		s.AddTable("Role");
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.Organize.ordinal());
		s.AddContion("Relation", "ObjID", jxCompare.Equal, ID);
		s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.Role.ordinal());
		s.AddContion("Relation", "TargetID", "Role","ID");
		return Select(Role.class,s);
	}	
	/**
	 * 获取某个虚拟角色在本部门内实际对应的真实角色
	 * @param RoleName
	 * @return
	 * @throws Exception
	 */
	public Role GetRealRole(String RoleName) throws Exception
	{
		Queue<jxORMobj> rl=ListRole();
		for(jxORMobj obj:rl)
		{
			if(((Role)obj).Name.compareTo(RoleName)==0)
				return (Role)obj;
		}
		return null;
	}
	public People GetRealMapTo(String RoleName) throws Exception
	{
		Queue<jxORMobj> rl=ListRole();
		for(jxORMobj obj:rl)
		{
			//检查其所有组成的真实角色中是否有符合的，如检查技术部中技术部经理是谁
			if(((Role)obj).Name.compareTo(RoleName)==0)
				return ((Role)obj).GetMapTo();
		}
		//检查如果是虚拟角色其所对应的真实角色中是否有符合的，如检查技术部中经理是谁
		Role r=GetRealRole(RoleName);
		if(r!=null)
			return r.GetMapTo();
		return null;
	}
	public Queue<jxORMobj> ListRealMapTo(String RoleName) throws Exception
	{
		Queue<jxORMobj> rl=ListRole();
		for(jxORMobj obj:rl)
		{
			//检查其所有组成的真实角色中是否有符合的，如检查技术部中测试工程师都是谁
			if(((Role)obj).Name.compareTo(RoleName)==0)
				return ((Role)obj).ListMapTo();
		}
		//检查如果是虚拟角色其所对应的真实角色中是否有符合的，如检查技术部中经理是谁
		Role r=GetRealRole(RoleName);
		if(r!=null)
			return r.ListMapTo();
		return null;
	}
	public Queue<jxORMobj> ListPeople() throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation");
		s.AddTable("People");
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.Organize.ordinal());
		s.AddContion("Relation", "ObjID", jxCompare.Equal, ID);
		s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.People.ordinal());
		s.AddContion("Relation", "TargetID", "People","ID");
		return Select(People.class,s);
	}	
	public Queue<jxORMobj> ListSubOrganize() throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation");
		s.AddTable("Organize");
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.Organize.ordinal());
		s.AddContion("Relation", "ObjID", jxCompare.Equal, ID);
		s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.Organize.ordinal());
		s.AddContion("Relation", "TargetID", "Organize","ID");
		return Select(Organize.class,s);
	}	
	public Queue<jxORMobj> ListProcess() throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation");
		s.AddTable("FlowChart");
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.Organize.ordinal());
		s.AddContion("Relation", "ObjID", jxCompare.Equal, ID);
		s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.FlowChart.ordinal());
		s.AddContion("Relation", "TargetID", "FlowChart","ID");
		return Select(FlowChart.class,s);
	}

	/**
	 * 查询当前正在做的流程，和上面各函数不同，本函数只能运行在组织经理的手机上！！
	 * @param Caller
	 * @param piState 为none则全查
	 * @param start 从这个时间点开始，包括该时间点
	 * @param end 截止到这个时间点，不包括该时间点
	 * @return 返回null是没有权限！！
	 * @throws Exception
	 */
	public Queue<jxORMobj> ListPI(People Caller,InstanceState piState,Date start,Date end) throws Exception
	{
		//if(Caller.CheckRight(ts,Caller, Role.RoleName_Manager,true)||Caller.CheckRight(ts, Caller,Role.RoleName_Owner,true))
		{
			SelectSql s=new SelectSql();
			s.AddTable("Relation");
			s.AddTable("FlowChartInstance");
			s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.Organize.ordinal());
			s.AddContion("Relation", "ObjID", jxCompare.Equal, ID);
			s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.FlowChartInstance.ordinal());
			s.AddContion("Relation", "TargetID", "FlowChartInstance","ID");
			if(piState!=InstanceState.None)
				s.AddContion("FlowChartInstance", "State", jxCompare.Equal,piState);
			s.AddContion("FlowChartInstance", "CreateTime", jxCompare.GreateEqual,start);
			s.AddContion("FlowChartInstance", "CreateTime", jxCompare.Less,end);
			return Select(FlowChartInstance.class,s);
		}
		//return null;
	}	
	
	
}