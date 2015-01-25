
package cn.ijingxi.common.Process;

import cn.ijingxi.common.util.*;

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
