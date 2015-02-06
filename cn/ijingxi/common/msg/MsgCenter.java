
package cn.ijingxi.common.msg;

import java.net.InetAddress;
import java.util.*;

import cn.ijingxi.common.app.jxSystem;
import cn.ijingxi.common.orm.jxORMobj;
import cn.ijingxi.common.util.CallParam;
import cn.ijingxi.common.util.IDoSomething;
import cn.ijingxi.common.util.jxSparseTable;
import cn.ijingxi.common.util.jxTimer;

public class MsgCenter
{
	static void Init()
	{		
		dualReceiveMsg.setPriority(Thread.MIN_PRIORITY);
		dualReceiveMsg.start();
	}
	//缓存所有待发送消息，但必要性不强
	static jxSparseTable<UUID,Long,jxMsg> Msgs=new jxSparseTable<UUID,Long,jxMsg>();	
	public static void Post(jxMsg msg) throws Exception
	{
		if(msg.Receiver.compareTo(jxSystem.SystemID)==0)
		{
			//投递到本机
			jxORMobj obj=jxORMobj.GetByID(msg.ReceiverID.getTypeID(), msg.ReceiverID.getID(), msg.getTopSpace());
			if(obj!=null)
				obj.DualMsg(msg);
		}
		else if(LanServer!=null)
		{
			InetAddress ia=LanNeighbor.get(msg.ReceiverID);
			if(ia!=null)
			{
				//在邻居里
				msg.mcTimer=jxTimer.DoAfter(jxMsg.AutoDeleteDelay, null, new MsgSendTimeOut(), msg);
				udpMsg.send(ia,msg);
				return;
			}
		}
		else if(JXServer!=null)
		{
			//最后只好发给JXServer
			//要用TCP，重要信息还要加密
			udpMsg.send(JXServer,msg);
			return;
		}
	}
	
	//接收到等待投递到本机的消息，但超时未发送成功的消息也会再次加入进来
	static Queue<jxMsg> WaitPostMsgs=new LinkedList<jxMsg>();
	//接收外部发到本机的低优先级投递队列
	static Thread dualReceiveMsg=new Thread(
			new Runnable()  
			{
				@Override
				public void run() {
					while(true)
						synchronized (this)
				        {
							jxMsg msg=WaitPostMsgs.poll();
							while(msg!=null)
							{
					     		   try {

										Post(msg);
					 			} catch (Exception e) {
					 				// TODO Auto-generated catch block
					 				e.printStackTrace();
					 			}
					     		msg=WaitPostMsgs.poll();
							}
							try {
								//没有就睡着
								wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				        }
				}
			}
		);
	//接收到了发到本机的消息
	public static void ReceivedMsg(jxMsg msg) throws Exception
	{		
		synchronized (WaitPostMsgs)
		{
			if(msg.State==jxMsgState.Sending)
			{
				//正常消息，需确认
				jxMsg mo=getOK(msg);
				//post需改进
				Post(mo);
				WaitPostMsgs.offer(msg);
				dualReceiveMsg.notify();
			}
			else if(msg.State==jxMsgState.Received)
			{
				//确认消息，收发是反过来的
				jxMsg mo=Msgs.Search(msg.Receiver, msg.MsgID);
				if(mo!=null)
				{
					mo.mcTimer.cancel();
					mo.DelaySave(null);
					MsgCenter.Msgs.Delete(mo.Sender, mo.MsgID);
				}
			}
		}
	}

	//准备接收确认消息
	static jxMsg ReceivedMsg=null;
	static jxMsg getOK(jxMsg msg) throws Exception
	{
		if(ReceivedMsg==null)
		{
			ReceivedMsg=(jxMsg) jxMsg.New(jxMsg.class);
			ReceivedMsg.State=jxMsgState.Received;
		}
		ReceivedMsg.Sender=msg.Receiver;
		ReceivedMsg.Receiver=msg.Sender;
		ReceivedMsg.MsgID=msg.MsgID;
		return ReceivedMsg;
	}
	

	static InetAddress LanServer=null;
	static InetAddress JXServer=null;
	static Map<UUID,InetAddress> LanNeighbor=new HashMap<UUID,InetAddress>();
	
	
	
	

	//只会注册在本机上
			
}

class MsgSendTimeOut implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception 
	{
		//第一个参数是msg
		jxMsg msg= (jxMsg)param.getParam();
		MsgCenter.WaitPostMsgs.offer(msg);
		//MsgCenter.Msgs.Delete(msg.SenderID, msg.MsgID);
	}
}




