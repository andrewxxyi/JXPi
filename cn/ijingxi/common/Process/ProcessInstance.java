
package cn.ijingxi.common.Process;

import cn.ijingxi.common.app.Container;
import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.*;


public class ProcessInstance extends Container
{	
	public static void Init() throws Exception{	InitClass(ProcessInstance.class);}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(ProcessInstance.class);
	}
	
	@ORM(keyType=KeyType.PrimaryKey)
	public int ID;
	
	@ORM(Index=3,Descr="流程的创建者ID，一定是人")
	public Integer CteaterID;
	
	@ORM(Index=4)
	public int ProcessID;

	@ORM(Descr="流程的当前状态，创建就是在运行中")
	public InstanceState State=InstanceState.Doing;
	
	
	//
	//静态变量与构造函数
	//
	static jxORMSM<InstanceState,InstanceEvent> ProcessSM=null;
	static
	{
		ProcessSM=new jxORMSM<InstanceState,InstanceEvent>();
		//初始化节点的状态转换
		ProcessSM.AddTrans(InstanceState.Doing, InstanceEvent.Close, InstanceState.Closed, new ProcessClose());
		ProcessSM.AddTrans(InstanceState.Doing, InstanceEvent.Cancel, InstanceState.Canceled, new NodeCancel());
		ProcessSM.AddTrans(InstanceState.Doing, InstanceEvent.Pause, InstanceState.Paused, new NodePause());
		ProcessSM.AddTrans(InstanceState.Paused, InstanceEvent.Trigger, InstanceState.Doing, new NodeDual());
	}

	//
	//变量与属性
	//
	//用来保存到数据库中
	Process process=null;
	
    jxBTree<String,ProcessNode> AllNode=new jxBTree<String,ProcessNode>();    
    //各子节点的输入token
    jxSparseTable<String,String,Integer> NodeInputToken=new jxSparseTable<String,String,Integer>();
    
    //流程暂停时使用
    jxLink<String,ProcessNode> pauseNodes=null;    
    
    	
    //
    //方法
    //
	@Override
	protected void myInit()
	{
		try
		{
			Process p=(Process)jxORMobj.GetByID(Process.class, ProcessID);
			if(p!=null)
				p.ResetInstance(this);
		}
		catch(Exception e)
		{
			  e.printStackTrace();
		}
	}
	
	public ProcessNode getNode(String NodeName)
	{
		return AllNode.Search(NodeName);
	}
	jxJson getNodesJSON() throws Exception
    {
		if(AllNode!=null)
		{
			jxJson j=jxJson.GetObjectNode("Nodes");
			for(BTreeNode<String,ProcessNode> node: AllNode)
			{
				j.AddSubObjNode(node.getValue().ToJsonNode());
			}			
		}
        return null;
    }    	
    
	/**
	 * 查找指定节点的输出路径
	 * @param NodeName
	 * @return 出口名为键，目的节点为值
	 */
	public jxLink<String,String> getOutBranch(String NodeName)
	{
		return process.OutBranch.search(NodeName);
	}
	//根据From节点名和出口名查找To节点
	ProcessNode getToNode(String From,String Export)
	{
		jxLink<String,String> ol=process.OutBranch.search(From);
		if(ol!=null)
		{
			String n=ol.search(Export);
			if(n!=null)
				return AllNode.Search(n);			
		}
		return null;
	}
	
	/**
	 * 查找指定节点的输入路径
	 * @param NodeName
	 * @return 源节点为键，出口名为值
	 */
	public jxLink<String,String> getInBranch(String NodeName)
	{
		return process.InBranch.search(NodeName);
	}
    jxJson getTransJSON() throws Exception
    {
		jxJson rs=jxJson.GetObjectNode("Trans");
		for(LinkNode<String, jxLink<String, String>> node: process.OutBranch)
		{
			jxLink<String, String> ts=node.getValue();
			for(LinkNode<String, String> n:ts)
			{
				jxJson j=jxJson.GetObjectNode("Trans");
				rs.AddSubObjNode(j);
				j.AddValue("Name", n.getKey());
				j.AddValue("From", node.getKey());
				j.AddValue("To", n.getValue());
			}			
		}
        return rs;
    }    	
    
    void Start(IExecutor Caller,String msg) throws Exception
    {
    	ProcessNode node=getNode(ProcessNode.Node_Start);
		CallParam param=new CallParam(null, Caller, msg);
		param.addParam(node);
		ProcessNode.NodeSM.Happen(node, "state", InstanceEvent.Trigger, param);
    }
    public void Pause(IExecutor Caller,String msg) throws Exception
    {
		CallParam param=new CallParam(null, Caller, msg);
		param.addParam(this);
		ProcessSM.Happen(this, "State", InstanceEvent.Pause, param);
    }
    public void ReDo(IExecutor Caller,String msg) throws Exception
    {
		CallParam param=new CallParam(null, Caller, msg);
		param.addParam(this);
		ProcessSM.Happen(this, "State", InstanceEvent.Trigger, param);
    }
    public void Cancle(IExecutor Caller,String msg) throws Exception
    {
		CallParam param=new CallParam(null, Caller, msg);
		param.addParam(this);
		ProcessSM.Happen(this, "State", InstanceEvent.Cancel, param);
    }
    public void Close(IExecutor Caller,String msg) throws Exception
    {
		CallParam param=new CallParam(null, Caller, msg);
		param.addParam(this);
		ProcessSM.Happen(this, "State", InstanceEvent.Close, param);
    }
    
    public void Node_Pause(String NodeName,IExecutor Caller,String msg) throws Exception
    {
    	ProcessNode node=getNode(NodeName);
		CallParam param=new CallParam(node.Execer, Caller, msg);
		param.addParam(node);
		ProcessNode.NodeSM.Happen(node, "state", InstanceEvent.Pause, param);
    }
    public void Node_ReDo(String NodeName,IExecutor Caller,String msg) throws Exception
    {
    	ProcessNode node=getNode(NodeName);
		CallParam param=new CallParam(node.Execer, Caller, msg);
		param.addParam(node);
		ProcessNode.NodeSM.Happen(node, "state", InstanceEvent.Trigger, param);
    }
    
}