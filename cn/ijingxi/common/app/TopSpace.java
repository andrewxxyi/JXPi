
package cn.ijingxi.common.app;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import cn.ijingxi.common.Process.PI;
import cn.ijingxi.common.Process.PN;
import cn.ijingxi.common.Process.jxProcess;
import cn.ijingxi.common.msg.jxMsg;
import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.jxCompare;

/**
 * 全局的，所有topspace共享
 * @author andrew
 *
 */
public class TopSpace extends jxORMobj
{
	public static void Init() throws Exception{	InitClass(ORMType.TopSpace.ordinal(),TopSpace.class);}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(TopSpace.class,null);
		TopSpace ts=(TopSpace) jxORMobj.New(TopSpace.class);
		ts.Name="我的空间";
		ts.UniqueID=UUID.randomUUID();
		ts.Insert(null);
	}
	public static void CreateDBTableInTS(TopSpace ts) throws Exception
	{
		Container.CreateDB(ts);
		jxProcess.CreateDB(ts);
		PI.CreateDB(ts);
		PN.CreateDB(ts);
		Role.CreateDB(ts);
		Relation.CreateDB(ts);
	}
	
	public static ORMID GetORMID(Integer ID)
	{
		return new ORMID(ORMType.TopSpace.ordinal(),ID);
	}
	
	@ORM(keyType=KeyType.AutoDBGenerated)
	public int ID;

	@ORM(Index=1,Encrypted=true)
	public String Name;		
	
	@ORM(Index=2,Descr="全局确定")
	public UUID UniqueID;
	@ORM(Descr="说明信息",Encrypted=true)
	public String Descr;

	//缓存顶层空间的uuid
	private static Map<UUID,TopSpace> TopSpaceIDTree=new HashMap<UUID,TopSpace>();
	
	public static TopSpace getByName(String Name) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("TopSpace",null);
		s.AddContion("TopSpace", "Name", jxCompare.Equal, Name);
		return (TopSpace) Get(TopSpace.class,s,null);
	}
	public static TopSpace getByUniqueID(UUID uuid) throws Exception
	{
		TopSpace ts=TopSpaceIDTree.get(uuid);
		if(ts!=null)return ts;
		SelectSql s=new SelectSql();
		s.AddTable("TopSpace",null);
		s.AddContion("TopSpace", "UniqueID", jxCompare.Equal, uuid);
		ts=(TopSpace) Get(TopSpace.class,s,null);
		if(ts!=null)
		{
			TopSpaceIDTree.put(uuid, ts);
			return ts;
		}
		return null;
	}
	
	public Organize CreateOrganize(People Caller,String OrganizeName) throws Exception
	{
		if(!Caller.CheckRight(this, Role.RoleName_Assist, false))
			throw new Exception(String.format("您没有权限执行该操作%s:%s","TopSpace","CreateOrganize"));
		Organize r=(Organize) New(Organize.class);
		r.Name=OrganizeName;
		DB db=JdbcUtils.GetDB();
        synchronized (db)
        {		
        	r.Insert(db,Caller.CurrentTopSpace);
        	Relation rl=(Relation)New(Relation.class);
        	rl.ObjTypeID=ORMType.TopSpace.ordinal();
        	rl.ObjID=ID;
        	rl.TargetTypeID=ORMType.Organize.ordinal();
        	rl.TargetID=r.ID;
        	rl.RelationType=RelationType.Contain.ordinal();
        	rl.Insert(db,Caller.CurrentTopSpace);
        	db.Release();
        }
        return r;
	}
	public void AddPeople(People Caller,People p) throws Exception
	{
		if(!Caller.CheckRight(this, Role.RoleName_Admin, false))
			throw new Exception(String.format("您没有权限执行该操作%s:%s","TopSpace","AddPeople"));
		DB db=JdbcUtils.GetDB();
        synchronized (db)
        {		
        	p.Insert(this);
        	Relation rl=(Relation)New(Relation.class);
        	rl.ObjTypeID=ORMType.TopSpace.ordinal();
        	rl.ObjID=ID;
        	rl.TargetTypeID=ORMType.People.ordinal();
        	rl.TargetID=p.ID;
        	rl.RelationType=RelationType.Contain.ordinal();
        	rl.Insert(db,this);
        	db.Release();
        }
	}
	void AddRole(Role r) throws Exception
	{
		DB db=JdbcUtils.GetDB();
        synchronized (db)
        {		
        	Relation rl=(Relation)New(Relation.class);
        	rl.ObjTypeID=ORMType.TopSpace.ordinal();
        	rl.ObjID=ID;
        	rl.TargetTypeID=ORMType.Role.ordinal();
        	rl.TargetID=r.ID;
        	rl.RelationType=RelationType.Contain.ordinal();
        	rl.Insert(db,this);
        	db.Release();
        }
	}

	public static TopSpace CreateTopSpaceID(People Caller,String TSName,String Descr) throws Exception
	{
		TopSpace ts=(TopSpace) TopSpace.New(TopSpace.class);
		ts.Name=TSName;
		ts.UniqueID=UUID.randomUUID();
		ts.Descr=Descr;
		ts.Insert(null);
		CreateDBTableInTS(ts);		
		Role r=(Role) Role.New(Role.class);
		r.Name=Role.RoleName_Owner;
		r.Insert(ts);
		r.SetMapToNotCheckRight(Caller,Caller);
		r=(Role) Role.New(Role.class);
		r.Name=Role.RoleName_Admin;
		r.Insert(ts);
		r.SetMapToNotCheckRight(Caller,Caller);
		r=(Role) Role.New(Role.class);
		r.Name=Role.RoleName_Assist;
		r.Insert(ts);
		r.SetMapToNotCheckRight(Caller,Caller);
		r=(Role) Role.New(Role.class);
		r.Name=Role.RoleName_Manager;
		r.Insert(ts);
		r.SetMapToNotCheckRight(Caller,Caller);
		r=(Role) Role.New(Role.class);
		r.Name=Role.RoleName_Agency;
		r.Insert(ts);
		Caller.CurrentTopSpace=ts;
		return ts;		
	}
	
	
	public Queue<jxORMobj> ListRole(People Caller) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation",Caller.CurrentTopSpace);
		s.AddTable("Role",Caller.CurrentTopSpace);
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.TopSpace.ordinal());
		s.AddContion("Relation", "ObjID", jxCompare.Equal, ID);
		s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.Role.ordinal());
		s.AddContion("Relation", "TargetID", "Role","ID");
		return Select(Role.class,s,Caller.CurrentTopSpace);
	}	

	protected boolean DualEventMsg(jxMsg msg)
	{
		
		return true;
	}
	
	//邀请加入时才广播
	public void BroadCast() throws Exception
	{
		//jxMsg msg=jxMsg.NewEventMsg(GetID(),jxSystem.broadUUID,null,TSEvent.BroadCast,null);
	}

	//申请加入
	public static void AddPeople()
	{
		
	}

	//向刚加入者同步
	public static void SyncSpace()
	{
		
	}

	//同时增加组织、角色、流程等表
	public static void CreateTopSpace(String Name) throws Exception
	{
		TopSpace ts=getByName(Name);
		if(ts==null)
		{
			ts=(TopSpace) jxORMobj.New(TopSpace.class);
			ts.Name=Name;
			ts.UniqueID=UUID.randomUUID();
			ts.Insert(null);
		}
		CreateDBTableInTS(ts);
	}
}