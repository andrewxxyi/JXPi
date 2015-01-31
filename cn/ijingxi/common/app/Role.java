
package cn.ijingxi.common.app;

import java.util.*;

import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.util.jxCompare;

//容器和角色的关系是一一映射
//角色和角色之间的关系是组成关系Contain，即技术部经理由二级经理、技术审核等多个不具体映射到人员的角色组成
//角色和人员的关系是一对一映射
public class Role extends Container
{
	public static final String RoleName_Owner="老板";
	public static final String RoleName_Admin="管理员";
	public static final String RoleName_Assist="协助管理者";
	public static final String RoleName_Manager="经理";
	public static final String RoleName_Agency="代理人";
	
	
	protected Role()
	{
		super();
		TypeName="Role";
		ContainerType=ContainerType_Role;
	}
	
	public static ORMID GetORMID(Integer ID)
	{
		return new ORMID(GetTypeID("Role"),ID);
	}
	
	/**
	 * 要在Container之后执行
	 * @throws Exception
	 */
	public static void Init() throws Exception{	InitClass(Role.class);}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(Role.class);
	}
	
	
	//@ORM(keyType=KeyType.PrimaryKey)
	//public int ID;

	//某岗位如果对应了多人则返回第一个（不能确定谁是第一个）
	public People GetMapTo() throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation");
		s.AddTable("People");
		s.AddContion("Relation", "ContainerID", jxCompare.Equal, ID);
		s.AddContion("Relation", "TargetContainerID", "People","ID");
		return (People) Get(People.class,s);
	}
	//某岗位可能对应了多个人
	public Queue<jxORMobj> ListMapTo() throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation");
		s.AddTable("People");
		s.AddContion("Relation", "ContainerID", jxCompare.Equal, ID);
		s.AddContion("Relation", "TargetContainerID", "People","ID");
		return Select(People.class,s);
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
		s.AddContion("Relation", "ContainerID", jxCompare.Equal, ID);
		s.AddContion("Relation", "TargetContainerID", "Role","ID");
		return Select(Role.class,s);
	}
	

}