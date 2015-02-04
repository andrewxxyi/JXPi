
package cn.ijingxi.common.Process;

/*
import cn.ijingxi.common.app.Container;
import cn.ijingxi.common.util.*;

//流程的状态事件
//正常结束，发送到PI的owner，然后由其触发本事件
class ProcessClose implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception
	{
		PI pi=(PI)param.getParam();
		jxLog.Log(null, Container.ContainerType_ProcessInstance, pi.ID, InstanceEvent.Close.ordinal(), null);
		MsgCenter.UnRegisterMsgHandle(pi.GetID());
		/*
		String m=String.format("<a>流程已结束，请尽快处理</a>", pi.Name);
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
		
		PI process=(PI)param.getParam();
		for(String  s: process.AllNode.keySet())
		{
			PN node=process.AllNode.get(s);
			if(node.getState()==InstanceState.Doing)
			{
				CallParam p=new CallParam(node.Execer,param.getCaller(),"流程实例被强行关闭！！！");
				PN.NodeSM.Happen(node, "state", InstanceEvent.Close, p);
			}			
		}

	}
}
class ProcessCancel implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception 
	{
		PI process=(PI)param.getParam();
        synchronized (process)
        {
			for(String  s: process.process.ListNodes())
			{
				PN node=process.AllNode.AllNode.get(s);					
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
		PI process=(PI)param.getParam();
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
		PI process=(PI)param.getParam();
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
*/