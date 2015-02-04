
package cn.ijingxi.common.Process;

import cn.ijingxi.common.app.*;
import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.*;


public class PI extends jxTask
{		
	public static ORMID GetORMID(Integer ID)
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

	public PI()
	{
		State=InstanceState.Doing;
	}
	@Override
	protected boolean CheckForMsgRegister() throws Exception
	{
		return true;
	}
	
	@ORM(keyType=KeyType.PrimaryKey)
	public int ID;
	
	@ORM(Index=1)
	public int ProcessID;
	public jxProcess GetProcess(People Caller) throws Exception
	{
		return (jxProcess) GetByID(jxProcess.class, ProcessID,Caller.CurrentTopSpace);
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
   void Start(People Caller) throws Exception
    {
		jxMsg msg=jxMsg.NewEventMsg(Caller.GetID(),Caller.UniqueID,GetID(),InstanceEvent.Trigger,null);
		msg.SetParam("CallerTypeID", Caller.GetID().getTypeID().toString());
		msg.SetParam("CallerID", Caller.GetID().getID().toString());
		MsgCenter.Post(msg);
    }
   /*
    public void Pause(People Caller,String Msg) throws Exception
    {
		jxMsg msg=jxMsg.NewEventMsg(null,Caller.UniqueID,null,InstanceEvent.Pause,Msg);
		msg.SetParam("CallerTypeID", Caller.GetID().getTypeID().toString());
		msg.SetParam("CallerID", Caller.GetID().getID().toString());
		MsgCenter.Post(msg);
    }
    public void ReDo(People Caller,String Msg) throws Exception
    {
		jxMsg msg=jxMsg.NewEventMsg(null,Caller.UniqueID,null,InstanceEvent.Trigger,Msg);
		msg.SetParam("CallerTypeID", Caller.GetID().getTypeID().toString());
		msg.SetParam("CallerID", Caller.GetID().getID().toString());
		MsgCenter.Post(msg);
    }
    public void Cancle(People Caller,String Msg) throws Exception
    {
		jxMsg msg=jxMsg.NewEventMsg(null,Caller.UniqueID,null,InstanceEvent.Cancel,Msg);
		msg.SetParam("CallerTypeID", Caller.GetID().getTypeID().toString());
		msg.SetParam("CallerID", Caller.GetID().getID().toString());
		MsgCenter.Post(msg);
    }
    public void Close(People Caller,String Msg) throws Exception
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