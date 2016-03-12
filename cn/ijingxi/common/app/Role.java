
package cn.ijingxi.common.app;

import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.util.jxCompare;
import java.util.*;

//容器和角色的关系是一一映射
//角色和角色之间的关系是组成关系Contain，即技术部经理由二级经理、技术审核等多个不具体映射到人员的角色组成
//角色和人员的关系是一对一映射
public class Role extends Container
{
	public static final String RoleName_Owner="老板";
	public static final String RoleName_Admin="管理员";
	//主要指按管理员的安排进行系统配置
	public static final String RoleName_Assist="协助管理者";
	public static final String RoleName_Manager="经理";
	public static final String RoleName_Agency="代理人";
	
	public static ORMID GetORMID(UUID ID)
	{
		return new ORMID(ORMType.Role.ordinal(),ID);
	}
	
	/**
	 * 要在Container之后执行
	 * @throws Exception
	 */
	public static void Init() throws Exception{
		InitClass(ORMType.Role.ordinal(),Role.class,"角色");
	}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(Role.class);
	}
	
	
	//@ORM(keyType=KeyType.PrimaryKey)
	//public int ID;
	
	//设置人员映射
	public void SetMapTo(People Caller,People p) throws Exception
	{
		//if(!Caller.CheckRight(ts,Caller, Role.RoleName_Admin, false))
		//	throw new Exception(String.format("您没有权限执行该操作%s:%s","Role","SetMapTo"));
		DB db=JdbcUtils.GetDB(null,this);
		db.Trans_Begin();
		try{
        synchronized (db)
        {
        	Relation rl=(Relation)Create(Relation.class);
        	rl.ObjTypeID=ORMType.Role.ordinal();
        	rl.ObjID=ID;
        	rl.TargetTypeID=ORMType.People.ordinal();
        	rl.TargetID=p.ID;
        	rl.RelType=RelationType.OneToMulti;
        	rl.Insert(db);
        }
        db.Trans_Commit();
	}
	catch(Exception e)
	{
		db.Trans_Cancel();
	}
	}
	void SetMapToNotCheckRight(People p) throws Exception
	{
		DB db=JdbcUtils.GetDB(null,this);
		db.Trans_Begin();
		try{
        synchronized (db)
        {
        	Relation rl=(Relation)Create(Relation.class);
        	rl.ObjTypeID=ORMType.Role.ordinal();
        	rl.ObjID=ID;
        	rl.TargetTypeID=ORMType.People.ordinal();
        	rl.TargetID=p.ID;
        	rl.RelType=RelationType.OneToMulti;
        	rl.Insert(db);
        }
        db.Trans_Commit();
	}
	catch(Exception e)
	{
		db.Trans_Cancel();
	}
	}
	
	public Organize getOrganize() throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation");
		s.AddTable("Organize");
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.Organize.ordinal());
		s.AddContion("Relation", "ObjID", "Organize", "ID");
		s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.Role.ordinal());
		s.AddContion("Relation", "TargetID", jxCompare.Equal, ID);
		return (Organize) Get(Organize.class,s);
	}	

	//某岗位如果对应了多人则返回第一个（不能确定谁是第一个），有岗位直接对应的一定是真实角色，虚拟角色一般需要和某部门结合才能
	//确定相应的真实角色，但其永远不应有人员对应
	public People GetMapTo() throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation");
		s.AddTable("People");
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.Role.ordinal());
		s.AddContion("Relation", "ObjID", jxCompare.Equal,ID);
		s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.People.ordinal());
		s.AddContion("Relation", "TargetID",  "People","ID");
		return (People) Get(People.class,s);
	}
	//某岗位可能对应了多个人
	public Queue<jxORMobj> ListMapTo() throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation");
		s.AddTable("People");
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.Role.ordinal());
		s.AddContion("Relation", "ObjID", jxCompare.Equal,ID);
		s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.People.ordinal());
		s.AddContion("Relation", "TargetID",  "People","ID");
		return Select(People.class,s);
	}

	/**
	 * 角色分为实际角色（岗位角色）和虚拟角色（职权角色），虚拟角色主要用于权限管理，
	 * 一个真实角色可以对应多个虚拟角色，也就是说一个真实岗位是由多个职权所组成
	 * @param db
	 * @param peopleID
	 * @return
	 * @throws Exception
     */
	public static Queue<UUID> listRoleMapToRoleID_SearchRightRole(DB db,
			UUID realRoleID) throws Exception {
		Queue<UUID> rs=new LinkedList<>();
		Queue<jxORMobj> ar=Relation.listObj(db,ORMType.Role.ordinal(),
				realRoleID,RelationType.OneToMulti);
		for(jxORMobj obj:ar){
			Relation rrole=(Relation)obj;
			if(rrole.ObjTypeID==ORMType.Role.ordinal()){
				rs.offer(rrole.ObjID);
			}
		}
		return rs;
	}
	/**
	 * 如果某角色为组合角色，即
	 * 真实角色，即需要安排人对应的职位，如技术部经理
	 * 虚拟角色，即用于决定权限的，不实际对应人，如二级部门经理，用来确定二级部门经理的权限，到了技术部就必须和其它角色
	 * 组合为实际的真实角色：技术部经理
	 * @return
	 * @throws Exception
	 */
	public Queue<jxORMobj> ListContainRole() throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation");
		s.AddTable("Role");
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.Role.ordinal());
		s.AddContion("Relation", "ObjID", jxCompare.Equal,ID);
		s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.Role.ordinal());
		s.AddContion("Relation", "TargetID",  "Role","ID");
		return Select(Role.class,s);
	}

}