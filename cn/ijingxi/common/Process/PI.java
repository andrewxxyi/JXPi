
package cn.ijingxi.common.Process;

import java.util.UUID;

import cn.ijingxi.common.app.*;
import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.*;


public class PI extends Container
{	
	protected PI()
	{
		super();
		TypeName="PI";
		ContainerType=ContainerType_ProcessInstance;
	}	
	
	public static ORMID GetORMID(Integer ID)
	{
		return new ORMID(GetTypeID("PI"),ID);
	}
	
	
	public static void Init() throws Exception
	{	
		InitClass(PI.class);
	}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(PI.class);
	}
	
	@ORM(keyType=KeyType.PrimaryKey)
	public int ID;
	
	@ORM(Index=3,Descr="流程的创建者ID，一定是人")
	public Integer CteaterID;
	public static People GetCteater(int PIID) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("PI");
		s.AddContion("PI", "ID", jxCompare.Equal, PIID);
		PI pi=(PI) Get(PI.class,s);
		return (People) GetByID(People.class, pi.CteaterID);
	}
	
	@ORM(Index=4)
	public int ProcessID;
	public static jxProcess GetProcess(int PIID) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("PI");
		s.AddContion("PI", "ID", jxCompare.Equal, PIID);
		PI pi=(PI) Get(PI.class,s);
		return (jxProcess) GetByID(jxProcess.class, pi.ProcessID);
	}
	
	@ORM(Descr="流程的当前状态，创建就是在运行中")
	public InstanceState State=InstanceState.Doing;

	@ORM
	public Result Result;

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
	//变量与属性
	//
	//用来保存到数据库中
	//jxProcess process=null;
	
    //Map<String,ProcessNode> AllNode=new HashMap<String,ProcessNode>();    
    //各子节点的输入token
    //jxSparseTable<String,String,Integer> NodeInputToken=new jxSparseTable<String,String,Integer>();
    
    //流程暂停时使用
    //jxLink<String,ProcessNode> pauseNodes=null;    
    
    	
    //
    //方法
    //
   static void Start(Integer PIID,IExecutor Caller) throws Exception
    {
		jxMsg msg=jxMsg.NewEventMsg(null,GetCteater(PIID).UniqueID,InstanceEvent.Trigger,null);
		msg.SetParam("Purpose", "PI");
		msg.SetParam("PIID", PIID.toString());
		msg.SetParam("CallerID", Caller.GetID().getID().toString());
		MsgCenter.Post(msg);
    }
    public static void Pause(Integer PIID,IExecutor Caller,String Msg) throws Exception
    {
		jxMsg msg=jxMsg.NewEventMsg(null,GetCteater(PIID).UniqueID,InstanceEvent.Pause,Msg);
		msg.SetParam("Purpose", "PI");
		msg.SetParam("PIID", PIID.toString());
		msg.SetParam("CallerID", Caller.GetID().getID().toString());
		MsgCenter.Post(msg);
    }
    public static void ReDo(Integer PIID,IExecutor Caller,String Msg) throws Exception
    {
		jxMsg msg=jxMsg.NewEventMsg(null,GetCteater(PIID).UniqueID,InstanceEvent.Trigger,Msg);
		msg.SetParam("Purpose", "PI");
		msg.SetParam("PIID", PIID.toString());
		msg.SetParam("CallerID", Caller.GetID().getID().toString());
		MsgCenter.Post(msg);
    }
    public static void Cancle(Integer PIID,IExecutor Caller,String Msg) throws Exception
    {
		jxMsg msg=jxMsg.NewEventMsg(null,GetCteater(PIID).UniqueID,InstanceEvent.Cancel,Msg);
		msg.SetParam("Purpose", "PI");
		msg.SetParam("PIID", PIID.toString());
		msg.SetParam("CallerID", Caller.GetID().getID().toString());
		MsgCenter.Post(msg);
    }
    public static void Close(Integer PIID,IExecutor Caller,String Msg) throws Exception
    {
		jxMsg msg=jxMsg.NewEventMsg(null,GetCteater(PIID).UniqueID,InstanceEvent.Close,Msg);
		msg.SetParam("Purpose", "PI");
		msg.SetParam("PIID", PIID.toString());
		msg.SetParam("CallerID", Caller.GetID().getID().toString());
		MsgCenter.Post(msg);
    }

    
}