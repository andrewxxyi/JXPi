
package cn.ijingxi.common.Process;

import cn.ijingxi.common.app.TopSpace;
import cn.ijingxi.common.app.jxSystem;
import cn.ijingxi.common.msg.jxMsg;
import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.CallParam;
import cn.ijingxi.common.util.IDoSomething;
import cn.ijingxi.common.util.IjxEnum;
import cn.ijingxi.common.util.jxLog;

import java.util.UUID;


public class PI extends WorkNode
{		
	public static ORMID GetORMID(UUID ID)
	{
		return new ORMID(ORMType.PI.ordinal(),ID);
	}	
	
	public static void Init() throws Exception
	{	
		InitClass(ORMType.PI.ordinal(),PI.class);
	}
	public static void CreateDB(TopSpace ts) throws Exception
	{
		CreateTableInDB(PI.class,ts);
	}	

	@Override
	protected void Init_Create(DB db) throws Exception
	{
		ID=UUID.randomUUID();
		Type=NodeType.ProcessInstance;
		State=InstanceState.Doing;
	}
	
	protected boolean DualEventMsg(jxMsg msg) throws Exception
	{
		IjxEnum event = msg.getEvent();
		if(event==null||!(event instanceof InstanceEvent))return false;
		TopSpace ts=msg.getTopSpace();
		if(ts==null)return false;
		switch((InstanceEvent)event)
		{
		case Close:
			   CallParam param=new CallParam(null,null,null);
			   param.addParam(this);
			   ProcessSM.Happen(this, "State", InstanceEvent.Close, param);
			   Update(null);
				
			
			
			return true;
		default:
			break;
		}
		return false;
	
	
	
	}
	
	
	@ORM(keyType=KeyType.PrimaryKey)
	public UUID ID;
	
	@ORM(Index=1)
	public UUID ProcessID;
	public jxProcess GetProcess(TopSpace ts) throws Exception
	{
		return (jxProcess) GetByID(jxProcess.class, ProcessID,ts);
	}
	public static jxProcess getProcess(UUID PIID,TopSpace ts)
	{
		//return (jxProcess) GetByID(jxProcess.class, ProcessID,ts);
		return null;
	}

	//
	//静态变量与构造函数
	//
	static jxORMSM<InstanceState,InstanceEvent> ProcessSM=null;
	static
	{
		ProcessSM=new jxORMSM<InstanceState,InstanceEvent>();
		//初始化节点的状态转换
		ProcessSM.AddTrans(InstanceState.Doing, InstanceEvent.Close, InstanceState.Closed, new ProcessClose());
		//ProcessSM.AddTrans(InstanceState.Doing, InstanceEvent.Cancel, InstanceState.Canceled, new NodeCancel());
		//ProcessSM.AddTrans(InstanceState.Doing, InstanceEvent.Pause, InstanceState.Paused, new NodePause());
		//ProcessSM.AddTrans(InstanceState.Paused, InstanceEvent.Trigger, InstanceState.Doing, new NodeDual());
	}
   
    	
    //
    //方法
    //
   /*
   void Start(PeopleInTs Caller,String Msg) throws Exception
    {
	   CallParam param=new CallParam(null,null,Msg);
	   param.addParam(this);
	   param.addParam(Caller.CurrentTopSpace);
	   ProcessSM.Happen(this, "State", InstanceEvent.Touch, param);
	   Update(null);
	   
		jxMsg msg=jxMsg.NewEventMsg(Caller.CurrentTopSpace,Caller.GetID(),Caller.UniqueID,GetID(),InstanceEvent.Trigger,null);
		msg.SetParam("CallerTypeID", Caller.GetID().getTypeID().toString());
		msg.SetParam("CallerID", Caller.GetID().getID().toString());
		MsgCenter.Post(msg);
    }
    public void Pause(PeopleInTs Caller,String Msg) throws Exception
    {
		jxMsg msg=jxMsg.NewEventMsg(null,Caller.UniqueID,null,InstanceEvent.Pause,Msg);
		msg.SetParam("CallerTypeID", Caller.GetID().getTypeID().toString());
		msg.SetParam("CallerID", Caller.GetID().getID().toString());
		MsgCenter.Post(msg);
    }
    public void ReDo(PeopleInTs Caller,String Msg) throws Exception
    {
		jxMsg msg=jxMsg.NewEventMsg(null,Caller.UniqueID,null,InstanceEvent.Trigger,Msg);
		msg.SetParam("CallerTypeID", Caller.GetID().getTypeID().toString());
		msg.SetParam("CallerID", Caller.GetID().getID().toString());
		MsgCenter.Post(msg);
    }
    public void Cancle(PeopleInTs Caller,String Msg) throws Exception
    {
		jxMsg msg=jxMsg.NewEventMsg(null,Caller.UniqueID,null,InstanceEvent.Cancel,Msg);
		msg.SetParam("CallerTypeID", Caller.GetID().getTypeID().toString());
		msg.SetParam("CallerID", Caller.GetID().getID().toString());
		MsgCenter.Post(msg);
    }
    public void Close(PeopleInTs Caller,String Msg) throws Exception
    {
		jxMsg msg=jxMsg.NewEventMsg(null,Caller.UniqueID,null,InstanceEvent.Close,Msg);
		msg.SetParam("CallerTypeID", Caller.GetID().getTypeID().toString());
		msg.SetParam("CallerID", Caller.GetID().getID().toString());
		MsgCenter.Post(msg);
    }

    */
}


//流程的状态事件
//正常结束，发送到PI的owner，然后由其触发本事件
class ProcessClose implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception
	{
		PI pi=(PI)param.getParam();
		String desc= (String)param.getMsg();
		jxLog log=jxLog.Log(jxSystem.SystemID, ORMType.PI.ordinal(), pi.ID, pi.Name, desc);
		log.setInfo("Event",InstanceEvent.Close);
		log.setInfo("State",InstanceState.Closed);
		log.Insert(null);
	}
}