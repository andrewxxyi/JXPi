
package cn.ijingxi.common.app;

import java.util.*;

import cn.ijingxi.common.Process.IExecutor;
import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.jxCompare;

/**
 * 全局的，所有topspace共享
 * @author andrew
 *
 */
public class People extends jxORMobj implements IExecutor
{
	public static ORMID GetORMID(Integer ID)
	{
		return new ORMID(ORMType.People.ordinal(),ID);
	}
	
	/**
	 * 要在Container之后执行
	 * @throws Exception
	 */
	public static void Init() throws Exception{	InitClass(ORMType.People.ordinal(),People.class);}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(People.class,null);
	}

	@Override
	protected void myInit() throws Exception
	{
		//在本机上，则设置当前空间为默认空间
		if(ID==1)
			CurrentTopSpace=(TopSpace) TopSpace.GetByID(TopSpace.class, 1, null);
	}
	
	//1号是手机主人，但如果某人在两台手机上都装了，则会出现冲突，需要加以解决
	@ORM(keyType=KeyType.AutoSystemGenerated)
	public int ID;

	@ORM(Index=1,Descr="用于全局确定")
	public UUID UniqueID;	
	public static People getByUniqueD(UUID uuid) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("People",null);
		s.AddContion("People", "UniqueID", jxCompare.Equal, uuid);
		return (People) Get(People.class,s,null);
	}
	
	@ORM(Index=2,Encrypted=true)
	public String Name;		
	
	@ORM(Index=3)
	public Date CreateTime;

	@ORM(Index=4)
	public String LoginName;

	@ORM(Index=5)
	public Date Birthday;
			
	@ORM
	public String Passwd;	
	
	@ORM(Descr="json格式的安全问题与答案：Question、Answer")
	public String Secure;
	
	@ORM(Descr="json格式的联系方式，包括但不限于Mail、Tel、Mobile、Fax、Address、国家等等")
	public String Contact;
		
	@ORM
	public Boolean NoUsed;

	//当前空间
	public TopSpace CurrentTopSpace;	
	//当前部门
	public Organize CurrentDepartment;	
	
	
	/**
	 * 检查本人是否具有某组织的某角色
	 * @param c
	 * @param RoleName
	 * @param CheckParent 为真则上级部门的权限可覆盖下级部门，如上级部门的经理也可以检查本部门的工作执行情况
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean CheckRight(jxORMobj obj,String RoleName,boolean CheckParent) throws Exception
	{
		if(obj instanceof Organize)
		{
			//对组织角色是否匹配进行检查
			Organize org=(Organize)obj;
			while(org!=null)
			{
				Queue<jxORMobj> pl=org.ListRealMapTo(this,RoleName);		
				if(pl!=null)
					for(jxORMobj p:pl)
						if(((People)p).GetID().Equal(GetID()))
							return true;
				if(!CheckParent)
					return false;
				org=org.GetParentOrganize(this);
			}
			return false;
		}
		else if(obj instanceof TopSpace)
		{
			//对topspace角色是否匹配进行检查
			TopSpace ts=(TopSpace)obj;
			Queue<jxORMobj> rl=ts.ListRole(this);
			for(jxORMobj ro:rl)
				if(((Role)ro).Name.compareTo(RoleName)==0)
				{
					People p = ((Role)ro).GetMapTo(this);
					if(p.GetID().compareTo(GetID())==0)
						return true;
				}
		}
		return false;
	}

	public void SetRoleMap(Role role)
	{
		
	}
	
	@Override
	public String getName() {
		return Name;
	}

	@Override
	public UUID getUniqueID() {
		return UniqueID;
	}

	@Override
	public UUID GetOwnerID() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IExecutor GetRealExecutor() {
		// TODO Auto-generated method stub
		return null;
	}

	
}