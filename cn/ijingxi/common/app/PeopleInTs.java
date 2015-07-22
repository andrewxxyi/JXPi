
package cn.ijingxi.common.app;

import cn.ijingxi.common.Process.IExecutor;
import cn.ijingxi.common.Process.InstanceEvent;
import cn.ijingxi.common.Process.InstanceState;
import cn.ijingxi.common.Process.jxTask;
import cn.ijingxi.common.msg.jxMsg;
import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.jxCompare;

import java.util.*;

/**
 * 全局的，所有topspace共享
 * @author andrew
 *
 */
public class PeopleInTs extends jxORMobj implements IExecutor
{
	public static ORMID GetORMID(UUID ID)
	{
		return new ORMID(ORMType.PeopleInTs.ordinal(),ID);
	}
	
	/**
	 * 要在Container之后执行
	 * @throws Exception
	 */
	public static void Init() throws Exception{	InitClass(ORMType.PeopleInTs.ordinal(),PeopleInTs.class);}
	public static void CreateDB(TopSpace ts) throws Exception
	{
		CreateTableInDB(PeopleInTs.class,ts);
	}

	@Override
	protected void myInit(DB db) throws Exception
	{
		Real=(People) GetByID(People.class, ID, null);
	}
	
	@Override
	protected boolean DualEventMsg(jxMsg msg) throws Exception{
		InstanceEvent event = (InstanceEvent) msg.getEvent();
		if(event!=null)
			switch(event)
			{
			case Create:
				if(msg.GetParam("EventInfo").compareTo("Task")==0)
				{
					DB db=JdbcUtils.GetDB();
					db.Trans_Begin();
						try{
				        synchronized (db)
				        {
				        	TopSpace ts=msg.getTopSpace();
				        	jxTask task=(jxTask) msg.getObj();
				        	task.Insert(db,ts);
				        	/*
						   task=(jxTask) Create(jxTask.class);
						   task.CreatorTypeID=Trans.TransToInteger(tag.getExtendValue("Addition", "ctid"));
						   task.CreatorID=Trans.TransToUUID(tag.getExtendValue("Addition", "cid"));
						   task.CreateTime=tag.TagTime;
						   task.Name=tag.Name;
						   task.ParentOwnerID=msg.Sender;
						   task.ParentID=msg.SID;
						   task.setExtendValue("Info", "PlanOrder",Trans.TransToInteger(tag.Number));
						   task.Insert(db,ts);
						   if(tag.Time!=null)
							   task.AddTag(db,ts, ObjTag.Tag_System_LastTime, tag.Time, null);
						   //当前只创建自己负责的任务
						   Relation.AddRela(db, ts, ORMType.PeopleInTs.ordinal(), ID, ORMType.jxTask.ordinal(), task.ID, RelationType.Main);
						   */
				        }
				        db.Trans_Commit();
					}
					catch(Exception e)
					{
						db.Trans_Cancel();
					}
					return true;
				}
				break;
			default:
				break;
			}
		return false;
	}
	
	//1号是手机主人，但如果某人在两台手机上都装了，则会出现冲突，需要加以解决
	@ORM(keyType=KeyType.PrimaryKey)
	public UUID ID;

	@ORM(Descr="在本TS中的别名")
	public String NickName;	
		
	@ORM(Descr="是否已离职")
	public Boolean Leaved;

	public People Real=null;
	
	/**
	 * 
	 * @param Caller
	 * @param rt Main:我负责的，Slave：我参与的，None：全体
	 * @return
	 * @throws Exception
	 */
	public Queue<jxORMobj> ListTask(TopSpace ts,RelationType rt) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation",ts);
		s.AddTable("jxTask",ts);
		s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.PeopleInTs.ordinal());
		s.AddContion("Relation", "TargetID", jxCompare.Equal,ID);
		if(rt!=RelationType.None)
			s.AddContion("Relation", "RelType", jxCompare.Equal, rt);
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.jxTask.ordinal());
		s.AddContion("Relation", "ObjID", "jxTask", "ID");
		s.AddContion("jxTask", "State", jxCompare.Equal, InstanceState.Doing);
		return Select(jxTask.class,s,ts);
	}	
	public Queue<jxORMobj> ListTask_myCreate(TopSpace ts) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("jxTask",ts);
		s.AddContion("jxTask", "CreatorTypeID", jxCompare.Equal, ORMType.PeopleInTs.ordinal());
		s.AddContion("jxTask", "CreatorID", jxCompare.Equal,ID);
		s.AddContion("jxTask", "State", jxCompare.Equal, InstanceState.Doing);
		return Select(jxTask.class,s,ts);
	}	
	/**
	 * 检查本人是否具有某组织的某角色
	 * @param c
	 * @param RoleName
	 * @param CheckParent 为真则上级部门的权限可覆盖下级部门，如上级部门的经理也可以检查本部门的工作执行情况
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean CheckRight(TopSpace ts,jxORMobj obj,String RoleName,boolean CheckParent) throws Exception
	{
		if(obj instanceof Organize)
		{
			//对组织角色是否匹配进行检查
			Organize org=(Organize)obj;
			while(org!=null)
			{
				Queue<jxORMobj> pl=org.ListRealMapTo(ts,RoleName);		
				if(pl!=null)
					for(jxORMobj p:pl)
						if(((PeopleInTs)p).getID().compareTo(getID())==0)
							return true;
				if(!CheckParent)
					return false;
				org=org.GetParentOrganize(ts);
			}
			return false;
		}
		else if(obj instanceof TopSpace)
		{
			//对topspace角色是否匹配进行检查
			//TopSpace t=(TopSpace)obj;
			Queue<jxORMobj> rl=ts.ListRole();
			for(jxORMobj ro:rl)
				if(((Role)ro).Name.compareTo(RoleName)==0)
				{
					PeopleInTs p = ((Role)ro).GetMapTo(ts);
					if(p.getID().compareTo(getID())==0)
						return true;
				}
		}
		return false;
	}

	public Role GetRoleMap(TopSpace ts)
	{
		
		
		
		return null;
	}
	public void SetRoleMap(Role role)
	{
		
	}
	
	@Override
	public String getName() {
		return Real.Name;
	}

	@Override
	public UUID getOwnerID() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IExecutor getRealExecutor() {
		// TODO Auto-generated method stub
		return null;
	}

	

	
}