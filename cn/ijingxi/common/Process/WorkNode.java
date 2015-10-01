
package cn.ijingxi.common.Process;

import cn.ijingxi.common.app.People;
import cn.ijingxi.common.app.PeopleInTs;
import cn.ijingxi.common.app.Result;
import cn.ijingxi.common.app.TopSpace;
import cn.ijingxi.common.msg.MsgCenter;
import cn.ijingxi.common.msg.jxMsg;
import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.CallParam;
import cn.ijingxi.common.util.IDoSomething;
import cn.ijingxi.common.util.Trans;

import java.util.*;


/**
 * 流程中的一个节点，也是一个工作空间
 * 
 * 是否自动执行，或输出情况下Auto为true则从所有出口中按顺序挑选第一个可以执行的节点进行触发
 * @author andrew
 *
 */
public class WorkNode extends jxORMobj
{    
	//最基本的四个节点
	public static String Node_Start = "开始";
	public static String Node_End = "结束";
	public static String Node_Accept = "同意";
	public static String Node_Reject = "拒绝";  
	
	public static ORMID GetORMID(UUID ID)
	{
		return new ORMID(ORMType.WorkNode.ordinal(),ID);
	}

	@Override
	protected void Init_Create(DB db) throws Exception
	{
		ID=UUID.randomUUID();
		CreateTime=new Date();
		State=InstanceState.Waiting;
	}
	
	public static void Init() throws Exception
	{	
		InitClass(ORMType.WorkNode.ordinal(),WorkNode.class);
	}
	public static void CreateDB(TopSpace ts) throws Exception
	{
		CreateTableInDB(WorkNode.class,ts);
	}
	/*
	@Override
	protected boolean CheckForMsgRegister() throws Exception
	{
		return true;
	}
	protected boolean DualEventMsg(jxMsg msg) throws Exception
	{
		IjxEnum event = msg.getEvent();
		if(event==null||!(event instanceof InstanceEvent))return false;
		TopSpace ts=msg.getTopSpace();
		if(ts==null)return false;
		WorkNode from=(WorkNode) jxORMobj.GetFromJSONString(msg.GetParam("Node"));
		String toname=msg.GetParam("To");
		switch((InstanceEvent)event)
		{
		case Touch:
			WorkNode WorkNode=getPN(ts,from.ParentID,toname);
			if(WorkNode==null)
			{
				WorkNode=(WorkNode) WorkNode.New(WorkNode.class);
				WorkNode.ParentOwnerID=msg.Receiver;
				WorkNode.ParentID=from.ParentID;
				WorkNode.ProcessID=from.ProcessID;
				WorkNode.Parent=from.Parent;
				WorkNode.Name=toname;
				WorkNode.Insert(ts);
			}
			WorkNode.Touch(ts, msg.getMsg());
				
			
			
			return true;
		default:
			break;
		}
		return false;
	
	
	
	}
	
	void setParam(PeopleInTs Caller,UUID ProcessID,PI pi,String NodeName) throws Exception 
	{
		this.ParentOwnerID=jxSystem.SystemID;
		this.ParentID=pi.ID;
		this.ProcessID=ProcessID;
		this.Parent=pi.ToJSONString();
		this.Name=NodeName;
	}
	
	jxMsg GetEvnetMsg(TopSpace ts,UUID Receiver,ORMID ReceiverID,IjxEnum Event,String Msg,String FromName,String ExportName,String ToName) throws Exception
	{
		//消息一定是发给pi，但由于pi也有相同的事件，所有要加以区分
		ORMID pid=new ORMID(ORMType.PI.ordinal(),Trans.TransToUUID((String)GetParent("ID")));
		jxMsg msg=jxMsg.NewEventMsg(ts,getID(),Receiver,pid,Event,Msg);
		msg.SetParam("Node",this.ToJSONString());
		msg.SetParam("tsid",Trans.TransToString(ts.ID));
		setMsgParam(msg,FromName,ExportName,ToName);
		return msg;
	}

	static WorkNode GetPNFromMsg(jxMsg msg) throws Exception
	{
		//msg里还有信息
		return (WorkNode) GetFromJSON(jxJson.JsonToObject(msg.getMsg()));
	}
	
	void setMsgParam(jxMsg msg,String FromName,String ExportName,String ToName) throws Exception
	{
		//消息一定是发给pi，但由于pi也有相同的事件，所有要加以区分
		msg.SetParam("Purpose", "WorkNode");
		msg.SetParam("NodeName", Name);
		msg.SetParam("ID", ID.toString());
		msg.SetParam("From",FromName);
		msg.SetParam("Export", ExportName);
		msg.SetParam("To", ToName);
		//TopSpace ts=(TopSpace) TopSpace.GetByID(TopSpace.class, Trans.TransToUUID(msg.GetParam("tsid")),null);
	}

	
	
	*/
	
	
	@ORM(keyType=KeyType.PrimaryKey)
	public UUID ID;
	
	@ORM(Index=1)
	public String Name;		
	
	@ORM(Descr="说明信息",Encrypted=true)
	public String Descr;
	
	@ORM(Index=2,Descr="用于两者之间的同步")
	public Date CreateTime;
	
	@ORM
	public NodeType Type;

	@ORM(Index=3,Descr="创建者的typeID，可以是外部人员创建")
	public int CreatorTypeID;
	@ORM(Index=3,Descr="创建者的ID")
	public UUID CreatorID;	
	public jxORMobj getCreator(TopSpace ts) throws Exception
	{
		return jxORMobj.GetByID(CreatorTypeID, CreatorID, ts);
	}
	@ORM(Index=4,Descr="所有者的typeID，就是责任人")
	public int OwnorTypeID;
	@ORM(Index=4,Descr="所有者的ID")
	public UUID OwnorID;
	public jxORMobj getOwnor(TopSpace ts) throws Exception
	{
		return jxORMobj.GetByID(CreatorTypeID, CreatorID, ts);
	}
	
	@ORM(Index=5,Descr="流程号，如果没有则是单独的一个工作空间")
	public UUID PIID;
	public jxProcess getProcess(TopSpace ts)
	{
		if(PIID!=null)
			return PI.getProcess(PIID,ts);
		return null;
	}
	@ORM(Index=6,Descr="流程的当前状态")
	public InstanceState State;

	@ORM
	public Result Result;

	@ORM(Descr="json格式的输入token,From,TokenNum")
	public String InputToken;

	@ORM(Descr="json格式的信息")
	public String Info;	

	//创建新对象时会被调用；从数据库读出时也会被调用，但会立刻被myInit覆盖
	WorkNode()
	{
		State=InstanceState.Waiting;
	}

	//第一个参数是节点，第二个参数是From节点名，第三个参数是出口名
    public void Touch(String Msg) throws Exception
    {
    	CallParam param=new CallParam(null,null,Msg);
    	param.addParam("Name",Name);
    	NodeSM.Happen(this, "State", InstanceEvent.Touch, param);
    	Update(null);
    }
    public void Close(TopSpace ts,String ExportName,String Msg) throws Exception
    {    	
    	CallParam param=new CallParam(null,null,Msg);
		param.addParam("Name",Name);
    	param.addParam("ExportName",ExportName);
    	NodeSM.Happen(this, "State", InstanceEvent.Close, param);
    	Update(null);
    }
    /*
    public void Pause(String NodeName,IExecutor Caller,String Msg) throws Exception
    {    	
    	jxProcess p=PI.GetProcess(PIID);
    	IExecutor execer=p.getNode_RealExecer(NodeName);
    	if(execer==null)
    		//没有具体的执行者，可能是自动执行的系统节点
    		execer=PI.GetCteater(PIID);
		jxMsg msg=GetEvnetMsg(Caller,execer,InstanceEvent.Pause,Msg,null,null,Name);
		MsgCenter.Post(msg);
    }
    public void ReDo(IExecutor Caller,String Msg) throws Exception
    {
    	jxProcess p=PI.GetProcess(PIID);
    	IExecutor execer=p.getNode_RealExecer(Name);
    	if(execer==null)
    		//没有具体的执行者，可能是自动执行的系统节点，所以发给流程实例的创建者那里执行
    		execer=PI.GetCteater(PIID);
		jxMsg msg=GetEvnetMsg(Caller,execer,InstanceEvent.Trigger,Msg,null,null,Name);
		MsgCenter.Post(msg);
    }
	
	*/
	
	
	void HasInput(String From) throws Exception
	{
		if(From==null)return;
		int num=getInputToken(From);
		num++;
		setInputToken(From,num);
	}	
	void setInputToken(String From,Integer TokenNum) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("From", From);
		setExtendArrayValue(InputToken, ks, "TokenNum", TokenNum);
	}
	int getInputToken(String From) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("From", From);
		String v=getExtendArrayValue(InputToken,ks,"TokenNum");
		return Trans.TransToInteger(v);
	}
	boolean checkInputToken(TopSpace ts) throws Exception
	{
		jxProcess p=getProcess(ts);
		boolean allin=true;
		int num=0;
		List<String> fl=p.ListFrom(Name);
		Map<String,Integer> itl=new HashMap<String,Integer>();
		for(String f:fl)
		{
			num=getInputToken(f);
			if(num<=0)
			{
				allin=false;
				break;
			}
			itl.put(f, num);
		}
		if(allin)
			for(String f:fl)
			{
				num=itl.get(f);
				num--;
				setInputToken(f,num);
			}
		return allin;		
	}
	
	
	static jxORMSM<InstanceState,InstanceEvent> NodeSM=null;
	static
	{
		NodeSM=new jxORMSM<InstanceState,InstanceEvent>();
		//初始化节点的状态转换
		//NodeSM.AddTrans(InstanceState.Waiting, InstanceEvent.Touch, InstanceState.Waiting, new CheckNodeInput());
		//NodeSM.AddTrans(InstanceState.Waiting, InstanceEvent.Trigger, InstanceState.Doing, new NodeDual());
		//NodeSM.AddTrans(InstanceState.Doing, InstanceEvent.Close, InstanceState.Closed, new NodeClose());
		//NodeSM.AddTrans(InstanceState.Doing, InstanceEvent.Pause, InstanceState.Paused, new NodePause());
		//NodeSM.AddTrans(InstanceState.Paused, InstanceEvent.Trigger, InstanceState.Doing, new NodeDual());
	}
	
}


class CheckNodeInput implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception 
	{
		//第一个参数是节点，第二个参数是From节点名，第三个参数是出口名，第四个参数是TopSpaceID
		WorkNode node= (WorkNode)param.getParam();
		People caller=(People) param.getCaller();
		String fromname=(String) param.getParam();
		String exportname=(String) param.getParam();
		TopSpace ts=(TopSpace) param.getParam();
		jxProcess process=node.getProcess(ts);
		if(process.getNode_InputType(node.Name))
			//就本功能来说其实只需要锁住进程的NodeInputToken，但考虑到进程本身也有可能同时进行动作，所以整体锁住
	        synchronized (node)
	        {
	        	node.HasInput(fromname);
	        	if(node.checkInputToken(ts))
    				//token到齐，发出一个触发事件
    				WorkNode.NodeSM.Happen(node, "State", InstanceEvent.Trigger, param);
    		}
	}
}

class NodeDual implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception 
	{
		//第一个参数是节点，第二个参数是From节点名，第三个参数是出口名
		WorkNode node= (WorkNode)param.getParam();
		PeopleInTs caller=(PeopleInTs) param.getCaller();
		String fromname=(String) param.getParam();
		String exportname=(String) param.getParam();
		TopSpace ts=(TopSpace) param.getParam();
		jxProcess process=node.getProcess(ts);
		IExecutor execer=process.getNode_RealExecer(ts,node.Name);
		if(node.Name==WorkNode.Node_End)
		{			
			jxMsg msg=jxMsg.NewEventMsg(ts,node.getID(),node.PIID,PI.GetORMID(node.PIID),InstanceEvent.Close,null);
			msg.SetParam("NodeName", node.Name);
			MsgCenter.Post(msg);
		}
		else if(node.getProcess(ts).getNode_OutputType(node.Name)||node.getProcess(ts).getNode_Auto(node.Name))
			//如果是与输出则相当于自动执行，不再等待Execer的动作	
			WorkNode.NodeSM.Happen(node, "State", InstanceEvent.Close, param);
		else if(execer!=null)
		{
			/*
			String m=String.format("<a>流程：%s的任务%s已流转到您，请尽快处理</a>", node.GetParent("Name"),node.Name);
			jxMsg msg=jxMsg.NewRichMsg(ts,node.getID(),execer.getUniqueID(),execer.getID(),m);
			node.setMsgParam(msg,fromname,exportname,node.Name);
			MsgCenter.Post(msg);
			*/
		}
	}
}


/*
class NodeClose implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception
	{
		//第一个参数是节点，第二个参数是From节点名，第三个参数是出口名
		WorkNode node= (WorkNode)param.getParam();
		PeopleInTs caller=(PeopleInTs) param.getCaller();
		String fromname=(String) param.getParam();
		String exportname=(String) param.getParam();
		TopSpace ts=(TopSpace) param.getParam();
		jxProcess process=node.getProcess(ts);
		if(process.getNode_OutputType(node.Name))
		{
			List<String> el = process.ListExport(node.Name);
			for(String e:el)
			{
				String tn=process.getTrans_To(node.Name, e);
				UUID epid=null;
				ORMID eid=null;
				PeopleInTs execer=process.getNode_RealExecer(ts,tn);
		    	if(execer==null)
		    	{
		    		//没有具体的执行者，可能是自动执行的系统节点，所以发给流程实例的创建者那里执行
		    		epid=node.ParentOwnerID;
		    		eid=PI.GetORMID(node.ParentID);
		    	}
		    	else
		    	{
		    		//下一节点的执行者，可能还没有创建这个节点，所以消息是发给进程的，如果没创建则创建，然后再转交
		    		epid=execer.ID;
		    		eid=jxProcess.GetORMID(node.ProcessID);
		    	}
				jxMsg msg=node.GetEvnetMsg(ts,epid,eid,InstanceEvent.Touch,null,node.Name,e,tn);
				MsgCenter.Post(msg);
			}
		}
		else if(process.getNode_Auto(node.Name))
		{
			PI pi=null;
			String ename=null;
			boolean abe=process.getNode_AutoByExecer(node.Name);
			List<String> el = process.ListExport(node.Name);			
			for(String e:el)
			{
				if(abe)
				{
					String tn=process.getTrans_To(node.Name, e);
					PeopleInTs execer=process.getNode_RealExecer(ts,tn);
			    	if(execer!=null)
			    	{
			    		ename=e;
			    		break;
			    	}
				}
				else
				{
					//后继分支选择
					ContionLink cl=process.getTranContion(node.Name, e);
					if(cl!=null&&cl.Judge(pi))
					{
			    		ename=e;
			    		break;
					}
					//
					//执行都会发回pi来执行
					//
					//操作：目前只有设置结果
					OPLink ol=process.getNodeOP(node.Name);
					if(ol!=null)
					{
						jxMsg msg=node.GetEvnetMsg(ts,node.ParentOwnerID,PI.GetORMID(node.PIID),InstanceEvent.SetResult,null,node.Name,null,null);
						msg.SetParam("Result", ol.Exec(pi));
						MsgCenter.Post(msg);
					}
				}
			}
			if(ename==null)
				throw new Exception(String.format("流程%s的节点%s自动执行，但未找到相应的执行分支！！",process.Name,node.Name));
			String tn=process.getTrans_To(node.Name, ename);
			UUID epid=null;
			ORMID eid=null;
			PeopleInTs execer=process.getNode_RealExecer(ts,tn);
	    	if(execer==null)
	    	{
	    		//没有具体的执行者，可能是自动执行的系统节点，所以发给流程实例的创建者那里执行
	    		epid=node.ParentOwnerID;
	    		eid=PI.GetORMID(node.PIID);
	    	}
	    	else
	    	{
	    		//下一节点的执行者，可能还没有创建这个节点，所以消息是发给进程的，如果没创建则创建，然后再转交
	    		epid=execer.ID;
	    		eid=jxProcess.GetORMID(node.ProcessID);
	    	}
			jxMsg msg=node.GetEvnetMsg(ts,epid,eid,InstanceEvent.Touch,null,node.Name,ename,tn);
			MsgCenter.Post(msg);
		}
		else
		{
			//param.Trans是用户指定的
			String tn=process.getTrans_To(node.Name, exportname);
			PeopleInTs execer=process.getNode_RealExecer(ts, tn);
			UUID epid=null;
			ORMID eid=null;
	    	if(execer==null)
	    	{
	    		//没有具体的执行者，可能是自动执行的系统节点，所以发给流程实例的创建者那里执行
	    		epid=node.ParentOwnerID;
	    		eid=PI.GetORMID(node.ParentID);
	    	}
	    	else
	    	{
	    		//下一节点的执行者，可能还没有创建这个节点，所以消息是发给进程的，如果没创建则创建，然后再转交
	    		epid=execer.ID;
	    		eid=jxProcess.GetORMID(node.ProcessID);
	    	}
			jxMsg msg=node.GetEvnetMsg(ts,epid,eid,InstanceEvent.Touch,null,node.Name,exportname,tn);
			MsgCenter.Post(msg);
		}
	}
}

*/


