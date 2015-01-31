
package cn.ijingxi.common.Process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.ijingxi.common.app.Container;
import cn.ijingxi.common.app.People;
import cn.ijingxi.common.app.jxSystem;
import cn.ijingxi.common.orm.ContionLink;
import cn.ijingxi.common.orm.OPLink;
import cn.ijingxi.common.orm.ORM;
import cn.ijingxi.common.orm.ORMID;
import cn.ijingxi.common.orm.jxJson;
import cn.ijingxi.common.orm.jxORMSM;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.*;

//是否自动执行，或输出情况下Auto为true则从所有出口中按顺序挑选第一个可以执行的节点进行触发
public class PN extends Container
{    
	public static String Node_Start = "开始";
	public static String Node_End = "结束";
	public static String Node_Accept = "同意";
	public static String Node_Reject = "拒绝";  

	protected PN()
	{
		super();
		TypeName="PN";
		ContainerType=ContainerType_ProcessNode;
	}
	
	public static ORMID GetORMID(Integer ID)
	{
		return new ORMID(GetTypeID("PN"),ID);
	}
	
	public static void Init() throws Exception
	{	
		InitClass(PI.class);
	}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(PI.class);
	}
	@Override
	protected boolean CheckForMsgRegister() throws Exception
	{
		return true;
	}
	
	
	
	@ORM(keyType=KeyType.PrimaryKey)
	public Integer ID;

	@ORM(Index=1)
	public Integer ProcessID;
	public jxProcess getProcess() throws Exception
	{
		return (jxProcess) GetByID(jxProcess.class,ProcessID);
	}

	@ORM(Descr="该pi创建者")
	public UUID PIOwnerID;
	@ORM(Descr="这个PIID不是本机的，而是位于该pi创建者机器上的id")
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
		
	PN(int ProcessID,PI pi,String NodeName) throws Exception 
	{
		this.ProcessID=ProcessID;
		this.PIOwnerID=pi.GetOwnerID();
		this.PIID=pi.ID;
		this.Name=NodeName;
	}
	
	jxMsg GetEvnetMsg(IExecutor Caller,IExecutor Execer,ORMID ReceiverID,IjxEnum Event,String Msg,String FromName,String ExportName,String ToName) throws Exception
	{
		jxMsg msg=jxMsg.NewEventMsg(Execer.getUniqueID(),ReceiverID,Event,Msg);
		setMsgParam(msg,Caller,FromName,ExportName,ToName);

		return msg;
	}

	PN GetPNFromMsg(jxMsg msg) throws Exception
	{
		PN pn=(PN) PN.New(PN.class);
		pn.Name=msg.GetParam("To");
		pn.ProcessID=Trans.TransToInteger(msg.GetParam("ProcessID"));
		pn.PIOwnerID=Trans.TransToUUID(msg.GetParam("PIOwnerID"));
		pn.PIID=Trans.TransToInteger(msg.GetParam("PIID"));
		pn.setExtendValue("Info","PIName",msg.GetParam("PIName"));
		return pn;
	}
	
	void setMsgParam(jxMsg msg,IExecutor Caller,String FromName,String ExportName,String ToName) throws Exception
	{
		msg.SetParam("Purpose", "PN");
		msg.SetParam("NodeName", Name);
		msg.SetParam("CallerID", Caller.GetID().getID().toString());
		msg.SetParam("ID", ID.toString());
		msg.SetParam("From",FromName);
		msg.SetParam("Export", ExportName);
		msg.SetParam("To", ToName);
		msg.SetParam("ProcessID", ProcessID.toString());
		msg.SetParam("PIOwnerID",  Trans.TransToString(PIOwnerID));
		msg.SetParam("PIID", PIID.toString());
		msg.SetParam("PIName", GetPIName());
	}
	
	
	
	void Start(PI pi,IExecutor Caller) throws Exception
	{
		setExtendValue("Info","PIName",pi.Name);
		Touch(Caller, null);
	}

    public void Touch(IExecutor Caller,String Msg) throws Exception
    {    	
    	jxProcess p=PI.GetProcess(PIID);
    	IExecutor execer=p.getNode_RealExecer(Name);
    	if(execer==null)
    		//没有具体的执行者，可能是自动执行的系统节点
    		execer=PI.GetCteater(PIID);
		jxMsg msg=GetEvnetMsg(Caller,execer,GetID(),InstanceEvent.Touch,Msg,Name,null,null);
		MsgCenter.Post(msg);
    }
    public void Close(String ExportName,IExecutor Caller,String Msg) throws Exception
    {    	
    	jxProcess p=PI.GetProcess(PIID);
    	IExecutor execer=p.getNode_RealExecer(Name);
    	if(execer==null)
    		//没有具体的执行者，可能是自动执行的系统节点
    		execer=PI.GetCteater(PIID);
		jxMsg msg=GetEvnetMsg(Caller,execer,GetID(),InstanceEvent.Close,Msg,Name,ExportName,null);
		MsgCenter.Post(msg);
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
		//NodeSM.AddTrans(InstanceState.Doing, InstanceEvent.Pause, InstanceState.Paused, new NodePause());
		//NodeSM.AddTrans(InstanceState.Paused, InstanceEvent.Trigger, InstanceState.Doing, new NodeDual());
	}
	
}


class CheckNodeInput implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception 
	{
		//第一个参数是节点，第二个参数是From节点名，第三个参数是出口名
		PN node= (PN)param.getParam();
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
    				PN.NodeSM.Happen(node, "State", InstanceEvent.Trigger, param);
    		}
	}
}

class NodeDual implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception 
	{
		//第一个参数是节点，第二个参数是From节点名，第三个参数是出口名
		PN node= (PN)param.getParam();
		String fromname=(String) param.getParam();
		String exportname=(String) param.getParam();
		jxProcess process=node.getProcess();
		IExecutor execer=process.getNode_RealExecer(node.Name);
		if(node.Name==PN.Node_End)
		{			
			jxMsg msg=jxMsg.NewEventMsg(node.GetPICteaterID(),PI.GetORMID(node.PIID),InstanceEvent.Close,null);
			msg.SetParam("NodeName", node.Name);
			MsgCenter.Post(msg);
		}
		else if(node.getProcess().getNode_OutputType(node.Name)||node.getProcess().getNode_Auto(node.Name))
			//如果是与输出则相当于自动执行，不再等待Execer的动作	
			PN.NodeSM.Happen(node, "State", InstanceEvent.Close, param);
		else if(execer!=null)
		{
			String m=String.format("<a>流程：%s的任务%s已流转到您，请尽快处理</a>", node.GetPIName(),node.Name);
			jxMsg msg=jxMsg.NewRichMsg(execer.getUniqueID(),execer.GetID(),m);
			node.setMsgParam(msg,param.getCaller(),fromname,exportname,node.Name);
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
		PN node= (PN)param.getParam();
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
		    		execer=PI.GetCteater(node.PIID);
				jxMsg msg=node.GetEvnetMsg(param.getCaller(),execer,People.GetORMID(1),InstanceEvent.Touch,null,node.Name,e,tn);
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
			    	IExecutor execer=process.getNode_RealExecer(tn);
			    	if(execer!=null)
			    	{
			    		ename=e;
			    		break;
			    	}
				}
				else
				{
					//
					//执行都会发回pi来执行
					//
					if(pi==null)
						pi=(PI) PI.GetByID(PI.class, node.PIID);
					//操作：目前只有设置结果
					OPLink ol=process.getNodeOP(node.Name);
					if(ol!=null)
						ol.Exec(pi);
					//后继分支选择
					ContionLink cl=process.getTranContion(node.Name, e);

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
	    		execer=PI.GetCteater(node.PIID);
			jxMsg msg=node.GetEvnetMsg(param.getCaller(),execer,execer.GetID(),InstanceEvent.Touch,null,node.Name,ename,tn);
			MsgCenter.Post(msg);
		}
		else
		{
			//param.Trans是用户指定的
			String tn=process.getTrans_To(node.Name, exportname);
	    	IExecutor execer=process.getNode_RealExecer(tn);
	    	if(execer==null)
	    		//没有具体的执行者，可能是自动执行的系统节点，所以发给流程实例的创建者那里执行
	    		execer=PI.GetCteater(node.PIID);
			jxMsg msg=node.GetEvnetMsg(param.getCaller(),execer,execer.GetID(),InstanceEvent.Touch,null,node.Name,exportname,tn);
			MsgCenter.Post(msg);
		}
		MsgCenter.UnRegisterMsgHandle(node.GetID());
	}
}




