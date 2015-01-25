
package cn.ijingxi.common.Process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.ijingxi.common.app.Container;
import cn.ijingxi.common.orm.ContionLink;
import cn.ijingxi.common.orm.ORM;
import cn.ijingxi.common.orm.jxJson;
import cn.ijingxi.common.orm.jxORMSM;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.*;

//是否自动执行，或输出情况下Auto为true则从所有出口中按顺序挑选第一个可以执行的节点进行触发
public class ProcessNode extends Container
{    
	public static String Node_Start = "开始";
	public static String Node_End = "结束";
	public static String Node_Accept = "同意";
	public static String Node_Reject = "拒绝";  

	protected ProcessNode() throws Exception {
		super();
		ContainerType=ContainerType_ProcessNode;
	}
	public static void Init() throws Exception
	{	
		InitClass(ProcessInstance.class);
	}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(ProcessInstance.class);
	}
	
	@ORM(keyType=KeyType.PrimaryKey)
	public Integer ID;

	@ORM(Index=1)
	public Integer ProcessID;
	public jxProcess getProcess() throws Exception
	{
		return (jxProcess) GetByID(jxProcess.class,ProcessID);
	}

	@ORM(Index=2)
	public Integer PIID;

	@ORM(Index=3)
	public InstanceState State=InstanceState.Waiting;

	@ORM(Descr="json格式的输入token,From,TokenNum")
	public String InputToken;

	@ORM(Descr="json格式的附加信息")
	public String Info;
	
	public String GetPIName() throws Exception
	{
		return getExtendValue("Info","PIName");
	}	
	public UUID GetPICteaterID() throws Exception
	{
		return Trans.TransToUUID(getExtendValue("Info","PICteaterID"));
	}
	
	ProcessNode(int ProcessID,Integer PIID,String NodeName) throws Exception 
	{
		this.ProcessID=ProcessID;
		this.PIID=PIID;
		this.Name=NodeName;
	}
	
	void Start(ProcessInstance PI,IExecutor Caller) throws Exception
	{
		setExtendValue("Info","PIName",PI.Name);
		setExtendValue("Info","PICteaterID",Trans.TransToString(ProcessInstance.GetCteater(PI.ID).UniqueID));		
		Touch(Caller, null);
	}

    public void Touch(IExecutor Caller,String Msg) throws Exception
    {    	
    	jxProcess p=ProcessInstance.GetProcess(PIID);
    	IExecutor execer=p.getNode_RealExecer(Name);
    	if(execer==null)
    		//没有具体的执行者，可能是自动执行的系统节点
    		execer=ProcessInstance.GetCteater(PIID);
		jxMsg msg=jxMsg.NewEventMsg(Caller.getUniqueD(),execer.getUniqueD(),InstanceEvent.Touch,Msg);
		msg.SetParam("Purpose", "PN");
		msg.SetParam("CallerID", Caller.GetID().getID().toString());
		msg.SetParam("ID", ID.toString());
		msg.SetParam("ProcessID", ProcessID.toString());
		msg.SetParam("PIID", PIID.toString());
		msg.SetParam("PIName", GetPIName());
		msg.SetParam("PICteaterID", Trans.TransToString(GetPICteaterID()));
		MsgCenter.Post(msg);
    }
    public void Pause(String NodeName,IExecutor Caller,String Msg) throws Exception
    {    	
    	jxProcess p=ProcessInstance.GetProcess(PIID);
    	IExecutor execer=p.getNode_RealExecer(NodeName);
    	if(execer==null)
    		//没有具体的执行者，可能是自动执行的系统节点
    		execer=ProcessInstance.GetCteater(PIID);
		jxMsg msg=jxMsg.NewEventMsg(Caller.getUniqueD(),execer.getUniqueD(),InstanceEvent.Pause,Msg);
		msg.SetParam("Purpose", "PN");
		msg.SetParam("CallerID", Caller.GetID().getID().toString());
		msg.SetParam("ID", ID.toString());
		msg.SetParam("ProcessID", ProcessID.toString());
		msg.SetParam("PIID", PIID.toString());
		msg.SetParam("PIName", GetPIName());
		msg.SetParam("PICteaterID", Trans.TransToString(GetPICteaterID()));
		MsgCenter.Post(msg);
    }
    public void ReDo(IExecutor Caller,String Msg) throws Exception
    {
    	jxProcess p=ProcessInstance.GetProcess(PIID);
    	IExecutor execer=p.getNode_RealExecer(Name);
    	if(execer==null)
    		//没有具体的执行者，可能是自动执行的系统节点，所以发给流程实例的创建者那里执行
    		execer=ProcessInstance.GetCteater(PIID);
		jxMsg msg=jxMsg.NewEventMsg(Caller.getUniqueD(),execer.getUniqueD(),InstanceEvent.Trigger,Msg);
		msg.SetParam("Purpose", "PN");
		msg.SetParam("CallerID", Caller.GetID().getID().toString());
		msg.SetParam("ID", ID.toString());
		msg.SetParam("ProcessID", ProcessID.toString());
		msg.SetParam("PIID", PIID.toString());
		msg.SetParam("PIName", GetPIName());
		msg.SetParam("PICteaterID", Trans.TransToString(GetPICteaterID()));
		MsgCenter.Post(msg);
    }
	
	
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
		setExtendArrayValue(InputToken,ks,"TokenNum",TokenNum);
	}
	int getInputToken(String From) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("From", From);
		String v=getExtendArrayValue(InputToken,ks,"TokenNum");
		return Trans.TransToInteger(v);
	}
	boolean checkInputToken() throws Exception
	{
		jxProcess p=getProcess();
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
		NodeSM.AddTrans(InstanceState.Waiting, InstanceEvent.Touch, InstanceState.Waiting, new CheckNodeInput());
		NodeSM.AddTrans(InstanceState.Waiting, InstanceEvent.Trigger, InstanceState.Doing, new NodeDual());
		NodeSM.AddTrans(InstanceState.Doing, InstanceEvent.Close, InstanceState.Closed, new NodeClose());
		NodeSM.AddTrans(InstanceState.Doing, InstanceEvent.Pause, InstanceState.Paused, new NodePause());
		NodeSM.AddTrans(InstanceState.Paused, InstanceEvent.Trigger, InstanceState.Doing, new NodeDual());
	}
	
}


class CheckNodeInput implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception 
	{
		//第一个参数是节点，第二个参数是From节点名，第三个参数是出口名
		ProcessNode node= (ProcessNode)param.getParam();
		String fromname=(String) param.getParam();
		String exportname=(String) param.getParam();
		jxProcess process=node.getProcess();
		if(process.getNode_InputType(node.Name))
			//就本功能来说其实只需要锁住进程的NodeInputToken，但考虑到进程本身也有可能同时进行动作，所以整体锁住
	        synchronized (node)
	        {
	        	node.HasInput(fromname);
	        	if(node.checkInputToken())
    				//token到齐，发出一个触发事件
    				ProcessNode.NodeSM.Happen(node, "State", InstanceEvent.Trigger, param);
    		}
	}
}

class NodeDual implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception 
	{
		//第一个参数是节点，第二个参数是From节点名，第三个参数是出口名
		ProcessNode node= (ProcessNode)param.getParam();
		String fromname=(String) param.getParam();
		String exportname=(String) param.getParam();
		jxProcess process=node.getProcess();
		IExecutor execer=process.getNode_RealExecer(node.Name);
		if(node.Name==ProcessNode.Node_End)
		{			
			jxMsg msg=jxMsg.NewEventMsg(null,node.GetPICteaterID(),InstanceEvent.Close,null);
			msg.SetParam("Purpose", "PI");
			msg.SetParam("PIID", node.PIID.toString());
			msg.SetParam("NodeName", node.Name);
			MsgCenter.Post(msg);
		}
		else if(node.getProcess().getNode_OutputType(node.Name)||node.getProcess().getNode_Auto(node.Name))
			//如果是与输出则相当于自动执行，不再等待Execer的动作	
			ProcessNode.NodeSM.Happen(node, "State", InstanceEvent.Close, param);
		else if(execer!=null)
		{
			String m=String.format("<a>流程：%s的任务%s已流转到您，请尽快处理</a>", node.GetPIName(),node.Name);
			jxMsg msg=jxMsg.NewRichMsg(null,execer.getUniqueD(),m);
			msg.SetParam("Purpose", "PN");
			msg.SetParam("CallerID", param.getCaller().GetID().getID().toString());
			msg.SetParam("ID", node.ID.toString());
			msg.SetParam("From",fromname);
			msg.SetParam("Export", exportname);			
			msg.SetParam("Name", node.Name);
			msg.SetParam("ProcessID", node.ProcessID.toString());
			msg.SetParam("PIID", node.PIID.toString());
			msg.SetParam("PIName", node.GetPIName());
			msg.SetParam("PICteaterID", Trans.TransToString(node.GetPICteaterID()));
			MsgCenter.Post(msg);
		}
	}
}

class NodeClose implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception
	{
		//第一个参数是节点，第二个参数是From节点名，第三个参数是出口名
		ProcessNode node= (ProcessNode)param.getParam();
		String fromname=(String) param.getParam();
		String exportname=(String) param.getParam();
		jxProcess process=node.getProcess();
		if(process.getNode_OutputType(node.Name))
		{
			List<String> el = process.ListExport(node.Name);
			for(String e:el)
			{
				String tn=process.getTrans_To(node.Name, e);
		    	IExecutor execer=process.getNode_RealExecer(tn);
		    	if(execer==null)
		    		//没有具体的执行者，可能是自动执行的系统节点，所以发给流程实例的创建者那里执行
		    		execer=ProcessInstance.GetCteater(node.PIID);
				jxMsg msg=jxMsg.NewEventMsg(param.getCaller().getUniqueD(),execer.getUniqueD(),InstanceEvent.Touch,null);
				msg.SetParam("Purpose", "PN");
				msg.SetParam("CallerID", param.getCaller().GetID().getID().toString());
				msg.SetParam("ID", node.ID.toString());
				msg.SetParam("From", node.Name);
				msg.SetParam("Export", e);			
				msg.SetParam("Name", tn);
				msg.SetParam("ProcessID", node.ProcessID.toString());
				msg.SetParam("PIID", node.PIID.toString());
				msg.SetParam("PIName", node.GetPIName());
				msg.SetParam("PICteaterID", Trans.TransToString(node.GetPICteaterID()));
				MsgCenter.Post(msg);
			}
		}
		else if(process.getNode_Auto(node.Name))
		{
			ProcessInstance pi=null;
			String ename=null;
			boolean abe=process.getNode_AutoByExecer(node.Name);
			List<String> el = process.ListExport(node.Name);			
			for(String e:el)
			{
				if(abe)
				{
					String tn=process.getTrans_To(node.Name, e);
			    	IExecutor execer=process.getNode_RealExecer(tn);
			    	if(execer!=null)
			    	{
			    		ename=e;
			    		break;
			    	}
				}
				else
				{
					ContionLink cl=process.getTranContion(node.Name, e);
					//
					//还没考虑在分布式情况下该如何进行自动条件判断
					//
					if(pi==null)
						pi=(ProcessInstance) ProcessInstance.GetByID(ProcessInstance.class, node.PIID);				
					if(cl!=null&&cl.Judge(pi))
					{
			    		ename=e;
			    		break;
					}
				}
			}
			if(ename==null)
				throw new Exception(String.format("流程%s的节点%s自动执行，但未找到相应的执行分支！！",process.Name,node.Name));
			String tn=process.getTrans_To(node.Name, ename);
	    	IExecutor execer=process.getNode_RealExecer(tn);
	    	if(execer!=null)
	    		//没有具体的执行者，可能是自动执行的系统节点，所以发给流程实例的创建者那里执行
	    		execer=ProcessInstance.GetCteater(node.PIID);
			jxMsg msg=jxMsg.NewEventMsg(param.getCaller().getUniqueD(),execer.getUniqueD(),InstanceEvent.Touch,null);
			msg.SetParam("Purpose", "PN");
			msg.SetParam("CallerID", param.getCaller().GetID().getID().toString());
			msg.SetParam("ID", node.ID.toString());
			msg.SetParam("From", node.Name);
			msg.SetParam("Export", e);			
			msg.SetParam("Name", tn);
			msg.SetParam("ProcessID", node.ProcessID.toString());
			msg.SetParam("PIID", node.PIID.toString());
			msg.SetParam("PIName", node.GetPIName());
			msg.SetParam("PICteaterID", Trans.TransToString(node.GetPICteaterID()));
			MsgCenter.Post(msg);
		}
		else
		{
			//param.Trans是用户指定的
			String tn=process.getTrans_To(node.Name, exportname);
	    	IExecutor execer=process.getNode_RealExecer(tn);
	    	if(execer==null)
	    		//没有具体的执行者，可能是自动执行的系统节点，所以发给流程实例的创建者那里执行
	    		execer=ProcessInstance.GetCteater(node.PIID);
			jxMsg msg=jxMsg.NewEventMsg(param.getCaller().getUniqueD(),execer.getUniqueD(),InstanceEvent.Touch,null);
			msg.SetParam("Purpose", "PN");
			msg.SetParam("CallerID", param.getCaller().GetID().getID().toString());
			msg.SetParam("ID", node.ID.toString());
			msg.SetParam("From", node.Name);
			msg.SetParam("Export", exportname);			
			msg.SetParam("Name", tn);
			msg.SetParam("ProcessID", node.ProcessID.toString());
			msg.SetParam("PIID", node.PIID.toString());
			msg.SetParam("PIName", node.GetPIName());
			msg.SetParam("PICteaterID", Trans.TransToString(node.GetPICteaterID()));
			MsgCenter.Post(msg);
		}
	}
}




