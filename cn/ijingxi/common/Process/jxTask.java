
package cn.ijingxi.common.Process;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import cn.ijingxi.common.app.*;
import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.*;

/**
 * 任务在发布者哪里进行记录，记录到父任务那里，如果没有则，统一记录到“我的未完成子任务”任务中哪里
 * @author andrew
 *
 */
public class jxTask extends Container
{		
	public static ORMID GetORMID(Integer ID)
	{
		return new ORMID(ORMType.jxTask.ordinal(),ID);
	}	
	
	public static void Init() throws Exception
	{	
		InitClass(ORMType.jxTask.ordinal(),PI.class);
	}
	public static void CreateDB(TopSpace ts) throws Exception
	{
		CreateTableInDB(jxTask.class,ts);
		jxTask task=(jxTask) New(jxTask.class);
		task.CreateTime=new Date();
		task.Name="未完成子任务";
		task.Descr="新创建的顶层任务的归口处";
		task.State=InstanceState.NoActive;
		task.Insert(ts);
	}
	
	@ORM(keyType=KeyType.AutoSystemGenerated)
	public int ID;

	@ORM
	public String Name;		
	
	@ORM(Descr="说明信息",Encrypted=true)
	public String Descr;
	
	@ORM(Index=1,Descr="用于两者之间的同步")
	public Date CreateTime;
	
	@ORM(Descr="json格式的附加信息")
	public String Info;	
	
	@ORM(Index=2)
	public UUID CreaterID;
	@ORM(Index=2)
	public int ParentID;
	
	public String GetParenetName() throws Exception
	{
		return (String) GetParent("Name");
	}
	public void SetInfo(String Purpose,Object value) throws Exception
	{
		setExtendValue("Info",Purpose,value);
	}
	
	@ORM(Descr="json格式的父任务")
	public String Parent;	
	protected Object GetParent(String Purpose) throws Exception
	{
		if(Parent!=null)
			return getExtendValue("Parent",Purpose);
		return null;
	}
	
	@ORM(Descr="json格式的子任务列表")
	public String SubTask;
	
	@ORM(Descr="流程的当前状态，创建就是在运行中")
	public InstanceState State=InstanceState.Doing;

	@ORM
	public Result Result;

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
	//如果是新创建一个任务，则放到此
	private static jxTask myIncompleteSubTask=null;
	static jxTask getIncompleteSubTask(TopSpace ts) throws Exception
	{
		if(myIncompleteSubTask==null)
			myIncompleteSubTask=(jxTask) GetByID(jxTask.class,1,ts);
		return myIncompleteSubTask;
	}
    //
    //方法
    //
	jxMsg ToTaskMsg(UUID Receiver) throws Exception
	{
		jxMsg msg=(jxMsg) jxMsg.New(jxMsg.class);
		msg.Sender=jxSystem.System.SystemUUID;
		msg.SenderID=ORMID.SystemID;
		msg.Receiver=Receiver;
		msg.ReceiverID=ORMID.SystemID;
		msg.MsgID=jxSystem.System.GetMsgID();
		msg.MsgType=jxMsgType.Event;
		msg.setMsg(ToJSONString());
		msg.SetParam("EventType",utils.GetClassName(InstanceEvent.class));
		msg.SetParam("Event",Trans.TransToInteger(InstanceEvent.Touch));		
		return msg;
	}
	public void AddSubTask(jxTask st) throws Exception
	{
		st.Parent=ToJSONString();
		addExtendArraySubNode("SubTask",st.ToJSON());
	}
	public void CreateSub(People Caller,People Execer,String Name,String Descr) throws Exception
    {
	   jxTask task=(jxTask) New(jxTask.class);
	   task.CreaterID=Caller.UniqueID;
	   task.ParentID=ID;
	   task.CreateTime=new Date();
	   task.Name=Name;
	   task.Descr=Descr;
	   task.Descr=Descr;
	   task.SetInfo("CteaterID", Caller.UniqueID);
	   
	   if(Caller.UniqueID.compareTo(Execer.UniqueID)==0)
		   task.Insert(Caller.CurrentTopSpace);
	   else
	   {
		   jxMsg msg=task.ToTaskMsg(Execer.UniqueID);
		   MsgCenter.Post(msg);
	   }
	   
	   AddSubTask(task);
    }
	public static void CreateTask(People Caller,People Execer,String Name,String Descr) throws Exception
    {
		jxTask task=getIncompleteSubTask(Caller.CurrentTopSpace);
		task.CreateSub(Caller, Execer, Name, Descr);
    }
	public List<jxJson> ListSubTask(People Caller) throws Exception
	{
		return getExtendArrayList("SubTask",null);
	}

	public void Pause(People Caller,String Msg) throws Exception
    {
    	CallParam param = new CallParam(Caller,Caller,Msg);
    	param.addParam(this);
    	jxTask.TaskSM.Happen(this, "State", InstanceEvent.Pause, param);
    }
    public void ReDo(People Caller,String Msg) throws Exception
    {
    	CallParam param = new CallParam(Caller,Caller,Msg);
    	param.addParam(this);
    	jxTask.TaskSM.Happen(this, "State", InstanceEvent.Trigger, param);
    }
    public void Cancle(People Caller,String Msg) throws Exception
    {
    	CallParam param = new CallParam(Caller,Caller,Msg);
    	param.addParam(this);
    	jxTask.TaskSM.Happen(this, "State", InstanceEvent.Cancel, param);
    }
    public void Close(People Caller,String Msg) throws Exception
    {
    	CallParam param = new CallParam(Caller,Caller,Msg);
    	param.addParam(this);
    	jxTask.TaskSM.Happen(this, "State", InstanceEvent.Close, param);
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
		UUID cid=task.CreaterID;
		jxLog log=jxLog.Log(jxSystem.System.SystemUUID, ORMType.jxTask.ordinal(), task.ID, task.Name, desc);
		log.setInfo("Event",InstanceEvent.Close);
		log.setInfo("State",InstanceState.Closed);
		log.Insert(null);
		if(cid.compareTo(jxSystem.SystemID)!=0)
			MsgCenter.Post(jxLog.NewLogMsg(log, cid));
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
		UUID cid=task.CreaterID;
		jxLog log=jxLog.Log(jxSystem.System.SystemUUID, ORMType.jxTask.ordinal(), task.ID, task.Name, desc);
		log.setInfo("Event",InstanceEvent.Cancel);
		log.setInfo("State",InstanceState.Canceled);
		log.Insert(null);
		if(cid.compareTo(jxSystem.SystemID)!=0)
			MsgCenter.Post(jxLog.NewLogMsg(log, cid));
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
		UUID cid=task.CreaterID;
		jxLog log=jxLog.Log(jxSystem.System.SystemUUID, ORMType.jxTask.ordinal(), task.ID, task.Name, desc);
		log.setInfo("Event",InstanceEvent.Pause);
		log.setInfo("State",InstanceState.Paused);
		log.Insert(null);
		if(cid.compareTo(jxSystem.SystemID)!=0)
			MsgCenter.Post(jxLog.NewLogMsg(log, cid));
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
		UUID cid=task.CreaterID;
		jxLog log=jxLog.Log(jxSystem.System.SystemUUID, ORMType.jxTask.ordinal(), task.ID, task.Name, desc);
		log.setInfo("Event",InstanceEvent.Trigger);
		log.setInfo("State",InstanceState.Doing);
		log.Insert(null);
		if(cid.compareTo(jxSystem.SystemID)!=0)
			MsgCenter.Post(jxLog.NewLogMsg(log, cid));
	}
}

