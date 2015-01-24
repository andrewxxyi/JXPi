
package cn.ijingxi.common.Process;

import cn.ijingxi.common.util.*;

class CheckNodeInput implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception 
	{
		//第一个参数是节点，第二个参数是From节点名，第三个参数是出口名
		ProcessNode node= (ProcessNode)param.getParam();
		ProcessInstance process=node.process;
		String fn=(String)param.getParam();
		
		if(node.InputTypeIsAnd)
			//就本功能来说其实只需要锁住进程的NodeInputToken，但考虑到进程本身也有可能同时进行动作，所以整体锁住
	        synchronized (node.process)
	        {
	        	jxLink<String,String> ib=process.getInBranch(node.Name);
    			int[] allin=new int[ib.getCount()];
    			boolean no=false;
    			int i=0;
    			int fnum=node.process.NodeInputToken.Search(node.Name, fn)+1;
    			for(LinkNode<String,String> n : ib)
    			{
    				if(fn==n.getKey())
    					allin[i]=fnum;
    				else
    					allin[i]=node.process.NodeInputToken.Search(node.Name, n.getKey());
    				if(allin[i]<1)
    				{
    					no=true;
    					break;
    				}
    				i++;
    			}
    			if(no)
    			{
    				node.process.NodeInputToken.Add(node.Name, fn, fnum);
    			}
    			else
    			{
    				//token到齐
    				i=0;
    				for(LinkNode<String,String> n : ib)
	    			{
	    				String ffn=n.getKey();
	    				allin[i]--;
	    				node.process.NodeInputToken.Add(node.Name, ffn, allin[i]);
	    				i++;
	    			}
    				//发起一个触发事件
    				ProcessNode.NodeSM.Happen(node, "state", InstanceEvent.Trigger, param);
    			}    			
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
		if(node.Execer!=null)
		{
			jxEvent<InstanceEvent> e=new jxEvent<InstanceEvent>(InstanceEvent.Trigger);
			//事件的触发者
			e.Param.offer(param.getCaller());
			//事件的现场，这里是所触发的节点
			e.Param.offer(node);
			//字符串消息
			e.Param.offer(null);
			node.Execer.Inform(e);
		}
		if(node.Name==ProcessNode.Node_End)
		{
			CallParam pparam=new CallParam(null, null, null);
			pparam.addParam(node.process);
			ProcessInstance.ProcessSM.Happen(node.process, "State", InstanceEvent.Close, param);
		}
		else if(node.OutputTypeIsAnd||node.Auto)
			//如果是与输出则相当于自动执行，不再等待Execer的动作	
			ProcessNode.NodeSM.Happen(node, "state", InstanceEvent.Close, param);
	}
}

class NodeClose implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception
	{
		//第一个参数是节点，第二个参数是From节点名，第三个参数是出口名
		ProcessNode node= (ProcessNode)param.getParam();
		if(node==null)
		{
			//流程实例被强行关闭所导致的节点被关闭
			jxEvent<InstanceEvent> e=new jxEvent<InstanceEvent>(InstanceEvent.Close);
			e.Param.offer(param.getCaller());
			e.Param.offer(null);
			e.Param.offer(param.getMsg());
			param.getExecer().Inform(e);	
			return;
		}
		ProcessInstance process=node.process;
		String fn=(String)param.getParam();
		String en=(String)param.getParam();
		if(node.OutputTypeIsAnd)
		{
			jxLink<String,String> ol=process.getOutBranch(node.Name);
			for(LinkNode<String,String> n : ol)
			{
				String tn=n.getValue();
				ProcessNode nt=process.getNode(tn);
				CallParam p=new CallParam(nt.Execer,node.Execer,param.getMsg());
				p.addParam(nt);				
				p.addParam(node.Name);				
				p.addParam(n.getKey());				
				ProcessNode.NodeSM.Happen(nt, "state", InstanceEvent.Touch, p);
			}
		}
		else if(node.Auto)
		{
			jxLink<Integer,String> oo=node.process.process.OutBranchOrder.search(node.Name);
			if(oo!=null)
			{
				for(LinkNode<Integer,String> n:oo)
				{
					int num=1;
					String ename=n.getValue();					
					ProcessNode tn=node.process.getToNode(node.Name,ename);
					if(tn!=null&&tn.Execer!=null||num==oo.getCount())
					{
						//第一个参数是节点，第二个参数是From节点名，第三个参数是出口名
						CallParam p=new CallParam(tn.Execer,node.Execer,param.getMsg());
						p.addParam(tn);				
						p.addParam(node.Name);				
						p.addParam(ename);				
						ProcessNode.NodeSM.Happen(tn, "state", InstanceEvent.Touch, p);
						break;
					}
					num++;
				}
			}
		}
		else
		{
			//param.Trans是用户指定的
			ProcessNode nt=process.getToNode(fn, en);
			CallParam p=new CallParam(nt.Execer,node.Execer,param.getMsg());
			p.addParam(nt);
			p.addParam(node.Name);
			p.addParam(en);
			ProcessNode.NodeSM.Happen(nt, "state", InstanceEvent.Touch, p);
		}
		if(node.Execer!=null)
		{
			jxEvent<InstanceEvent> e=new jxEvent<InstanceEvent>(InstanceEvent.Close);
			e.Param.offer(param.getCaller());
			e.Param.offer(node);
			e.Param.offer(param.getMsg());
			node.Execer.Inform(e);	
		}
	}
}
class NodeCancel implements IDoSomething
{
	@Override
	public void Do(CallParam param) 
	{
		//第一个参数是节点，第二个参数是From节点名，第三个参数是出口名
		ProcessNode node= (ProcessNode)param.getParam();
		jxEvent<InstanceEvent> e=new jxEvent<InstanceEvent>(InstanceEvent.Cancel);
		e.Param.offer(param.getCaller());
		e.Param.offer(node);
		e.Param.offer(param.getMsg());
		node.Execer.Inform(e);	
	}
}
class NodePause implements IDoSomething
{
	@Override
	public void Do(CallParam param) 
	{
		//第一个参数是节点，第二个参数是From节点名，第三个参数是出口名
		ProcessNode node= (ProcessNode)param.getParam();
		jxEvent<InstanceEvent> e=new jxEvent<InstanceEvent>(InstanceEvent.Pause);
		e.Param.offer(param.getCaller());
		e.Param.offer(node);
		e.Param.offer(param.getMsg());
		node.Execer.Inform(e);	
	}
}

//流程的状态事件
class ProcessClose implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception
	{
		ProcessInstance process=(ProcessInstance)param.getParam();
		for(String  s: process.AllNode.keySet())
		{
			ProcessNode node=process.AllNode.get(s);				
			if(node.getState()==InstanceState.Doing)
			{
				CallParam p=new CallParam(node.Execer,param.getCaller(),"流程实例被强行关闭！！！");
				ProcessNode.NodeSM.Happen(node, "state", InstanceEvent.Close, p);
			}			
		}
		/* 应该通知流程实例的发起者
		jxEvent<InstanceEvent> e=new jxEvent<InstanceEvent>(InstanceEvent.Close);
		e.Param.offer(param.getCaller());
		e.Param.offer(null);
		e.Param.offer(param.getMsg());
		node.Execer.Inform(e);	
		*/
	}
}
class ProcessCancel implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception 
	{
		ProcessInstance process=(ProcessInstance)param.getParam();
        synchronized (process)
        {
			for(String  s: process.AllNode.keySet())
			{
				ProcessNode node=process.AllNode.get(s);					
				if(node.getState()==InstanceState.Doing)
				{
					CallParam p=new CallParam(node.Execer,param.getCaller(),"流程实例被强行关闭！！！");
					ProcessNode.NodeSM.Happen(node, "state", InstanceEvent.Cancel, p);
				}			
			}
		}
	}
}
class ProcessPause implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception 
	{
		ProcessInstance process=(ProcessInstance)param.getParam();
        synchronized (process)
        {
			process.pauseNodes=new jxLink<String,ProcessNode>();		
			for(String  s: process.AllNode.keySet())
			{
				ProcessNode node=process.AllNode.get(s);			
				if(node.getState()==InstanceState.Doing)
				{
					//正在运行的节点也全部进入暂停状态并予以保存
					//否则用户会有些莫名其妙的就突然不能用了
					CallParam p=new CallParam(node.Execer,param.getCaller(),"流程实例被暂停！！！");
					ProcessNode.NodeSM.Happen(node, "state", InstanceEvent.Pause, p);
					process.pauseNodes.addByRise(node.Name, node);
				}			
			}
        }
	}
}
class ProcessDual implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception 
	{
		ProcessInstance process=(ProcessInstance)param.getParam();
        synchronized (process)
        {
			for(LinkNode<String, ProcessNode>  tn: process.pauseNodes)
			{
				//将之前暂停的节点全部恢复
				ProcessNode node=tn.getValue();
				CallParam p=new CallParam(node.Execer,param.getCaller(),"流程实例已恢复！！！");
				ProcessNode.NodeSM.Happen(node, "state", InstanceEvent.Trigger, p);
			}
			process.pauseNodes=null;
        }
	}
}
