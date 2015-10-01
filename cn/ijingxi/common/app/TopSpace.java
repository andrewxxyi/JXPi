
package cn.ijingxi.common.app;

import cn.ijingxi.common.Process.*;
import cn.ijingxi.common.msg.jxMsg;
import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.jxCompare;

import java.util.Queue;
import java.util.UUID;

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
		People p=(People) People.GetByID(People.class, jxSystem.SystemID, null);
		CreateTopSpace(p,null,"我的空间",null);
	}
	public static void CreateDBTableInTS(TopSpace ts) throws Exception
	{
		Container.CreateDB(ts);
		jxProcess.CreateDB(ts);
		jxTask.CreateDB(ts);
		PI.CreateDB(ts);
		WorkNode.CreateDB(ts);
		Role.CreateDB(ts);
		Relation.CreateDB(ts);
		PeopleInTs.CreateDB(ts);
		ObjTag.CreateDB(ts);
	}

	@Override
	protected void Init_Create(DB db) throws Exception
	{
		ID=UUID.randomUUID();
	}
	public static ORMID GetORMID(UUID ID)
	{
		return new ORMID(ORMType.TopSpace.ordinal(),ID);
	}
	
	@ORM(keyType=KeyType.PrimaryKey)
	public UUID ID;

	@ORM(Index=1)
	public String Name;		
	
	@ORM(Descr="说明信息",Encrypted=true)
	public String Descr;
	
	@ORM
	public Boolean NoUsed;
	
	public static TopSpace getByName(String Name) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("TopSpace",null);
		s.AddContion("TopSpace", "Name", jxCompare.Equal, Name);
		return (TopSpace) Get(TopSpace.class,s,null);
	}
	
	public PeopleInTs getPeopleInTs(People p) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("PeopleInTs",this);
		s.AddContion("PeopleInTs", "ID", jxCompare.Equal, p.ID);
		return (PeopleInTs) Get(PeopleInTs.class,s,this);		
	}
	
	public static Queue<jxORMobj> ListTopSpace(int Start, int Number) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("TopSpace",null);
		s.AddContion("TopSpace", "NoUsed", jxCompare.Equal, false);
		s.setOffset(Start);
		s.setLimit(Number);
		return Select(TopSpace.class,s,null);
	}	

	public Organize CreateOrganize(TopSpace ts,PeopleInTs Caller,String OrganizeName) throws Exception
	{
		if(!Caller.CheckRight(this,Caller, Role.RoleName_Assist, false))
			throw new Exception(String.format("您没有权限执行该操作%s:%s","TopSpace","CreateOrganize"));
		Organize r=(Organize) Create(Organize.class);
		r.Name=OrganizeName;
		DB db=JdbcUtils.GetDB();
		db.Trans_Begin();
		try{
        synchronized (db)
        {		
        	r.Insert(db,ts);
        	Relation rl=(Relation)Create(Relation.class);
        	rl.ObjTypeID=ORMType.TopSpace.ordinal();
        	rl.ObjID=ID;
        	rl.TargetTypeID=ORMType.Organize.ordinal();
        	rl.TargetID=r.ID;
        	rl.RelType=RelationType.Contain;
        	rl.Insert(db,ts);
        }
        db.Trans_Commit();
	}
	catch(Exception e)
	{
		db.Trans_Cancel();
	}
        return r;
	}
	public void AddPeople(PeopleInTs Caller,People p) throws Exception
	{
		if(!Caller.CheckRight(this, Caller,Role.RoleName_Admin, false))
			throw new Exception(String.format("您没有权限执行该操作%s:%s","TopSpace","AddPeople"));
		DB db=JdbcUtils.GetDB();
		db.Trans_Begin();
		try{
        synchronized (db)
        {		
        	//p.Insert(this);
        	
    		PeopleInTs pits=(PeopleInTs) PeopleInTs.Create(PeopleInTs.class);
    		pits.ID=p.ID;
    		pits.Insert(this);
        	
        	Relation rl=(Relation)Create(Relation.class);
        	rl.ObjTypeID=ORMType.TopSpace.ordinal();
        	rl.ObjID=ID;
        	rl.TargetTypeID=ORMType.PeopleInTs.ordinal();
        	rl.TargetID=pits.ID;
        	rl.RelType=RelationType.Contain;
        	rl.Insert(db,this);
        }
        db.Trans_Commit();
	}
	catch(Exception e)
	{
		db.Trans_Cancel();
	}
	}
	void AddRole(Role r) throws Exception
	{
		DB db=JdbcUtils.GetDB();
		db.Trans_Begin();
		try{
        synchronized (db)
        {		
        	Relation rl=(Relation)Create(Relation.class);
        	rl.ObjTypeID=ORMType.TopSpace.ordinal();
        	rl.ObjID=ID;
        	rl.TargetTypeID=ORMType.Role.ordinal();
        	rl.TargetID=r.ID;
        	rl.RelType=RelationType.Contain;
        	rl.Insert(db,this);
        }
        db.Trans_Commit();
	}
	catch(Exception e)
	{
		db.Trans_Cancel();
	}
	}

	/**
	 * 当两个ts要进行同步时，其uuid也必须一致，所以这种情况下的创建ts还必须同步传入其uuid
	 * @param Caller
	 * @param TSName
	 * @param Descr
	 * @return
	 * @throws Exception
	 */
	public static TopSpace CreateTopSpace(People Caller,UUID id,String TSName,String Descr) throws Exception
	{
		TopSpace ts=(TopSpace) TopSpace.Create(TopSpace.class);
		ts.Name=TSName;
		ts.Descr=Descr;
		if(id!=null)
			ts.ID=id;
		ts.Insert(null);
		CreateDBTableInTS(ts);		
		
		PeopleInTs p=(PeopleInTs) PeopleInTs.Create(PeopleInTs.class);
		p.ID=Caller.ID;
		p.NickName=Caller.Name;
		p.Insert(ts);
		
		Role r=(Role) Role.Create(Role.class);
		r.Name=Role.RoleName_Owner;
		r.Insert(ts);
		ts.AddRole(r);
		r.SetMapToNotCheckRight(ts,p);
		
		r=(Role) Role.Create(Role.class);
		r.Name=Role.RoleName_Admin;
		r.Insert(ts);
		ts.AddRole(r);
		r.SetMapToNotCheckRight(ts,p);
		
		r=(Role) Role.Create(Role.class);
		r.Name=Role.RoleName_Assist;
		r.Insert(ts);
		ts.AddRole(r);
		r.SetMapToNotCheckRight(ts,p);
		
		r=(Role) Role.Create(Role.class);
		r.Name=Role.RoleName_Manager;
		r.Insert(ts);
		ts.AddRole(r);
		r.SetMapToNotCheckRight(ts,p);
		/*
		r=(Role) Role.Create(Role.class);
		r.Name=Role.RoleName_Agency;
		r.Insert(ts);
		ts.AddRole(r);
		*/
		
		return ts;		
	}
	

	public int getTaskNum() throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation",this);
		s.AddTable("jxTask",this);
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.TopSpace.ordinal());
		s.AddContion("Relation", "ObjID", jxCompare.Equal, ID);
		s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.jxTask.ordinal());
		s.AddContion("Relation", "TargetID", "jxTask","ID");
		s.AddContion("jxTask", "State", jxCompare.Equal, InstanceState.Doing);
		return GetCount(jxTask.class,s,this);
	}	
	public Queue<jxORMobj> ListTask() throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation",this);
		s.AddTable("jxTask",this);
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.TopSpace.ordinal());
		s.AddContion("Relation", "ObjID", jxCompare.Equal, ID);
		s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.jxTask.ordinal());
		s.AddContion("Relation", "TargetID", "jxTask","ID");
		s.AddContion("jxTask", "State", jxCompare.Equal, InstanceState.Doing);
		return Select(jxTask.class,s,this);
	}	
	public Queue<jxORMobj> ListPeople() throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation",this);
		s.AddTable("PeopleInTs",this);
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.TopSpace.ordinal());
		s.AddContion("Relation", "ObjID", jxCompare.Equal, ID);
		s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.Role.ordinal());
		s.AddContion("Relation", "TargetID", "PeopleInTs","ID");
		return Select(PeopleInTs.class,s,this);
	}	
	public Queue<jxORMobj> ListRole() throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation",this);
		s.AddTable("Role",this);
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.TopSpace.ordinal());
		s.AddContion("Relation", "ObjID", jxCompare.Equal, ID);
		s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.Role.ordinal());
		s.AddContion("Relation", "TargetID", "Role","ID");
		return Select(Role.class,s,this);
	}	
	public Role getRole(String RoleName) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation",this);
		s.AddTable("Role",this);
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.TopSpace.ordinal());
		s.AddContion("Relation", "ObjID", jxCompare.Equal, ID);
		s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.Role.ordinal());
		s.AddContion("Relation", "TargetID", "Role","ID");
		s.AddContion("Role", "Name",  jxCompare.Equal,RoleName);
		return (Role) Get(Role.class,s,this);
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


}