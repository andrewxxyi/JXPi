
package cn.ijingxi.common.Process;
/*
import cn.ijingxi.common.app.*;
import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.*;

import java.util.Date;
import java.util.Queue;
import java.util.UUID;

/**
 * 任务在发布者哪里进行记录，记录到父任务那里，如果没有则，统一记录到“我的未完成子任务”任务中哪里
 * @author andrew
 *
 */
/*
public class jxTask extends WorkNode
{		
	public static ORMID GetORMID(UUID ID)
	{
		return new ORMID(ORMType.jxTask.ordinal(),ID);
	}	
	
	public static void Init() throws Exception
	{	
		InitClass(ORMType.jxTask.ordinal(),jxTask.class);
	}
	public static void CreateDB(TopSpace ts) throws Exception
	{
		CreateTableInDB(jxTask.class,ts);
	}

	@Override
	protected void Init_Create(DB db) throws Exception
	{
		ID=UUID.randomUUID();
		Type=NodeType.Task;
	}

	/*

	@Override
	protected boolean DualEventMsg(jxMsg msg) throws Exception{
		jxMsgType event = (jxMsgType) msg.getEvent();
		if(event!=null)
			switch(event)
			{
			case Report:
				if(msg.GetParam("EventInfo").compareTo("Tag")==0)
				{
					DB db=JdbcUtils.GetDB();
					db.Trans_Begin();
						try{
				        synchronized (db)
				        {
				        	TopSpace ts=msg.getTopSpace();
				        	ObjTag tag=(ObjTag) msg.getObj();
				        	//设置本tag为未查看/处理
				        	tag.TagState=InstanceState.Waiting.ordinal();
				        	tag.Insert(db, ts);
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
	
	*/
	
	
	/*
	@ORM(keyType=KeyType.PrimaryKey)
	public UUID ID;
			
	//
	//静态变量与构造函数
	//
	static jxORMSM<InstanceState,InstanceEvent> TaskSM=null;
	static
	{
		TaskSM=new jxORMSM<InstanceState,InstanceEvent>();
		//初始化节点的状态转换
		TaskSM.AddTrans(InstanceState.Doing, InstanceEvent.Close, InstanceState.Closed, new TaskClose());
		TaskSM.AddTrans(InstanceState.Doing, InstanceEvent.Cancel, InstanceState.Canceled, new TaskCancel());
		TaskSM.AddTrans(InstanceState.Doing, InstanceEvent.Pause, InstanceState.Paused, new TaskPause());
		TaskSM.AddTrans(InstanceState.Paused, InstanceEvent.Trigger, InstanceState.Doing, new TaskRedo());
	}
    //
    //方法
    //
	/**
	 * 
	 * @param ts
	 * @param rt Main or Slave分别为列表主负责人与参与者，None则列表全体
	 * @return
	 * @throws Exception
	 */
/*
	public Queue<jxORMobj> ListExecer(TopSpace ts,RelationType rt) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation",ts);
		s.AddTable("PeopleInTs",ts);
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.jxTask.ordinal());
		s.AddContion("Relation", "ObjID", jxCompare.Equal, ID);
		s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.PeopleInTs.ordinal());
		s.AddContion("Relation", "TargetID", "PeopleInTs","ID");
		if(rt!=RelationType.None)
			s.AddContion("Relation", "RelType", jxCompare.Equal, rt);
		return Select(PeopleInTs.class,s,ts);
	}

	public PeopleInTs GetExecer(TopSpace ts) throws Exception
	{
		Queue<jxORMobj> el=ListExecer(ts,RelationType.Main);
		for(jxORMobj obj:el)
		{
			PeopleInTs p=(PeopleInTs)obj;
			return p;
		}
		return null;
	}
	
	public void AddExecer(DB db,TopSpace ts,PeopleInTs p,RelationType rt) throws Exception
	{
		if(rt==RelationType.Main)
			ChangeMainExecer(db,ts,p);
		else
		{
		        	Relation rl=(Relation)Create(Relation.class);
		        	rl.ObjTypeID=ORMType.jxTask.ordinal();
		        	rl.ObjID=ID;
		        	rl.TargetTypeID=ORMType.PeopleInTs.ordinal();
		        	rl.TargetID=p.ID;
		        	rl.RelType=RelationType.Slave;
		        	rl.Insert(db,ts);


		    }
	}

	public void ChangeMainExecer(DB db,TopSpace ts,PeopleInTs p) throws Exception
	{
		//原参与者改负责人未考虑
			SelectSql s=new SelectSql();
			s.AddTable("Relation",ts);
			s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.jxTask.ordinal());
			s.AddContion("Relation", "ObjID", jxCompare.Equal, ID);
			s.AddContion("Relation", "RelType", jxCompare.Equal, RelationType.Main);
			Queue<jxORMobj> rlist=Select(db,Relation.class,s,ts);
			for(jxORMobj obj:rlist)
			{
				Relation rl=(Relation)obj;
				rl.RelType=RelationType.Slave;
				rl.Update(db,ts);
				break;
			}
        	Relation rl=(Relation)Create(Relation.class);
        	rl.ObjTypeID=ORMType.jxTask.ordinal();
        	rl.ObjID=ID;
        	rl.TargetTypeID=ORMType.PeopleInTs.ordinal();
        	rl.TargetID=p.ID;
        	rl.RelType=RelationType.Main;
        	rl.Insert(db,ts);
	}

	public void AddSubTask(jxTask st) throws Exception
	{
		//st.Parent=ToJSONString();
		addExtendArraySubNode("SubTask",st.ToJSON());
	}
	public void CreateSub(TopSpace ts,PeopleInTs Caller,PeopleInTs Execer,String Name,String Descr) throws Exception
    {
		DB db=JdbcUtils.GetDB();
		db.Trans_Begin();
		try{
	        synchronized (db)
	        {
			   jxTask task=(jxTask) Create(jxTask.class);
			   task.CreatorTypeID=Caller.getTypeID();
			   task.CreatorID=Caller.ID;
			   //task.ParentOwnerID=jxSystem.SystemID;
			   //task.ParentID=ID;
			   task.CreateTime=new Date();
			   task.Name=Name;
			   task.Descr=Descr;
			   task.Insert(db, ts);
		
		      	Relation rl=(Relation)Create(Relation.class);
		      	rl.ObjTypeID=ORMType.jxTask.ordinal();
		      	rl.ObjID=ID;
		      	rl.TargetTypeID=ORMType.jxTask.ordinal();
		      	rl.TargetID=task.ID;
		      	rl.RelType=RelationType.OneToMulti;
		      	rl.Insert(db,ts);	

		      	if(Execer==null)
		      		Execer=Caller;
		      	this.AddExecer(db,ts, Execer, RelationType.Main);
		      	if(Execer.ID.compareTo(jxSystem.SystemID)!=0)
		      	{

		      	}
	        }
	        db.Trans_Commit();
		}
		catch(Exception e)
		{
			db.Trans_Cancel();
		}
		

    }

	public Queue<jxORMobj> ListSubTask(TopSpace ts) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("Relation",ts);
		s.AddTable("jxTask",ts);
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.jxTask.ordinal());
		s.AddContion("Relation", "ObjID", jxCompare.Equal, ID);
		s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.jxTask.ordinal());
		s.AddContion("Relation", "TargetID", "jxTask","ID");
		s.AddContion("Relation", "RelType", jxCompare.Equal, RelationType.OneToMulti);
		return Select(jxTask.class,s,ts);
	}
	
	
	public static jxTask CreateTask(DB db,TopSpace ts,PeopleInTs Caller,Date doAt,String Name,String Descr) throws Exception
    {
		 jxTask task=(jxTask) Create(jxTask.class);
			   task.CreatorTypeID=Caller.getTypeID();
			   task.CreatorID=Caller.ID;
			   task.CreateTime=new Date();
			   task.Name=Name;
			   task.Descr=Descr;
			   task.Insert(db, ts);
			   //if(doAt!=null)
				//   task.AddTag(db,ts, ObjTag.Tag_System_LastTime, doAt, null);
     
			return task;
    }

	/**
	 * 向所有参与方播报一条tag
	 * @param ts
	 * @param Receiver
	 * @param ReceiverID
	 * @param tag
	 * @throws Exception
	 */
/*
	private void ReportTag(TopSpace ts,ObjTag tag) throws Exception
	{

	}
	/**
	 * 为本任务添加一条计划
	 * @param ts
	 * @param Caller 只有本任务的执行者才能为其添加计划
	 * @param order 应以100为单位分开，便于插入等调整
	 * @param doAt
	 * @param execer 为null则指定给创建者，即本任务执行者
	 * @param plan
	 * @return
	 * @throws Exception
	 */
/*
	public ObjTag AddPlan(TopSpace ts,PeopleInTs Caller,int order,Date doAt,PeopleInTs execer,String plan) throws Exception
	{
		PeopleInTs pe=GetExecer(ts);
		if(pe!=null&&pe.ID==Caller.ID)
		{
			ObjTag tag=AddTag(ts,ObjTag.getTagID("计划"),doAt,"任务",plan);
			tag.setExtendValue("Addition", "CallerID", Trans.TransToString(Caller.ID));
			tag.setExtendValue("Addition", "CallerName", Caller.Real.Name);
			tag.Number=(float) order;
			tag.Update(ts);
			ReportTag(ts,tag);
			return tag;
		}
		return null;
	}
	
	public void InformToAll()
	{
		
	}


	public void Pause(TopSpace ts,PeopleInTs Caller,String Msg) throws Exception
    {
    	CallParam param = new CallParam(Caller,Caller,Msg);
    	param.addParam(this);
    	jxTask.TaskSM.Happen(this, "State", InstanceEvent.Pause, param);
		ObjTag.AddTag(ts, ObjTag.getTagID("状态切换"), getTypeID(), ID, 0f,"状态", "Pause");
    }
    public void ReDo(TopSpace ts,PeopleInTs Caller,String Msg) throws Exception
    {
    	CallParam param = new CallParam(Caller,Caller,Msg);
    	param.addParam(this);
    	jxTask.TaskSM.Happen(this, "State", InstanceEvent.Trigger, param);
		ObjTag.AddTag(ts, ObjTag.getTagID("状态切换"), getTypeID(), ID, 0f, "状态", "Trigger");
    }
    public void Cancle(TopSpace ts,PeopleInTs Caller,String Msg) throws Exception
    {
    	CallParam param = new CallParam(Caller,Caller,Msg);
    	param.addParam(this);
    	jxTask.TaskSM.Happen(this, "State", InstanceEvent.Cancel, param);
		ObjTag.AddTag(ts, ObjTag.getTagID("状态切换"), getTypeID(), ID, 0f,"状态",  "Cancel");
    }
    public void Close(TopSpace ts,PeopleInTs Caller,String Msg) throws Exception
    {
    	CallParam param = new CallParam(Caller,Caller,Msg);
    	param.addParam(this);
    	jxTask.TaskSM.Happen(this, "State", InstanceEvent.Close, param);
		ObjTag.AddTag(ts, ObjTag.getTagID("状态切换"), getTypeID(), ID, 0f,"状态",  "Close");
    }

    
}


class TaskClose implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception 
	{
		//第一个参数是任务
		jxTask task= (jxTask)param.getParam();
		String desc= (String)param.getMsg();
		//UUID cid=task.ParentOwnerID;
		
		
		jxLog log=jxLog.Log(jxSystem.System.ID, ORMType.jxTask.ordinal(), task.ID, task.Name, desc);
		log.setInfo("Event",InstanceEvent.Close);
		log.setInfo("State",InstanceState.Closed);
		log.Insert(null);
		//if(cid.compareTo(jxSystem.SystemID)!=0)
		//	MsgCenter.Post(jxLog.NewLogMsg(log, cid));
	}
}

class TaskCancel implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception 
	{
		//第一个参数是任务
		jxTask task= (jxTask)param.getParam();
		String desc= (String)param.getMsg();
		//UUID cid=task.ParentOwnerID;
		jxLog log=jxLog.Log(jxSystem.System.ID, ORMType.jxTask.ordinal(), task.ID, task.Name, desc);
		log.setInfo("Event",InstanceEvent.Cancel);
		log.setInfo("State",InstanceState.Canceled);
		log.Insert(null);
		//if(cid.compareTo(jxSystem.SystemID)!=0)
		//	MsgCenter.Post(jxLog.NewLogMsg(log, cid));
	}
}
class TaskPause implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception 
	{
		//第一个参数是任务
		jxTask task= (jxTask)param.getParam();
		String desc= (String)param.getMsg();
		//UUID cid=task.ParentOwnerID;
		jxLog log=jxLog.Log(jxSystem.System.ID, ORMType.jxTask.ordinal(), task.ID, task.Name, desc);
		log.setInfo("Event",InstanceEvent.Pause);
		log.setInfo("State",InstanceState.Paused);
		log.Insert(null);
		//if(cid.compareTo(jxSystem.SystemID)!=0)
		//	MsgCenter.Post(jxLog.NewLogMsg(log, cid));
	}
}
class TaskRedo implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception 
	{
		//第一个参数是任务
		jxTask task= (jxTask)param.getParam();
		String desc= (String)param.getMsg();
		//UUID cid=task.ParentOwnerID;
		jxLog log=jxLog.Log(jxSystem.System.ID, ORMType.jxTask.ordinal(), task.ID, task.Name, desc);
		log.setInfo("Event",InstanceEvent.Trigger);
		log.setInfo("State",InstanceState.Doing);
		log.Insert(null);
		//if(cid.compareTo(jxSystem.SystemID)!=0)
		//	MsgCenter.Post(jxLog.NewLogMsg(log, cid));
	}
}


*/
