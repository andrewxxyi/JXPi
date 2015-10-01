
package cn.ijingxi.common.msg;

import cn.ijingxi.common.app.jxSystem;
import cn.ijingxi.common.orm.DB;
import cn.ijingxi.common.orm.jxORMobj;
import cn.ijingxi.common.util.IDo;
import cn.ijingxi.common.util.jxTimer;

import java.net.InetAddress;
import java.util.*;

public class MsgCenter
{
	static void Init()
	{		
		dualReceiveMsg.setPriority(Thread.MIN_PRIORITY);
		dualReceiveMsg.start();
	}
	//缓存所有待发送消息，但必要性不强
	static Map<UUID,jxMsg> Msgs=new HashMap<UUID,jxMsg>();	
	public static void Post(jxMsg msg) throws Exception
	{
		if(msg.Receiver.compareTo(jxSystem.SystemID)==0)
		{
			//投递到本机
			jxORMobj obj=jxORMobj.GetByID(msg.RTypeID, msg.RID, msg.getTopSpace());
			//if(obj!=null)
			//	obj.DualMsg(msg);
		}
		else if(LanServer!=null)
		{
			InetAddress ia=LanNeighbor.get(msg.getReceiverID());
			if(ia!=null)
			{
				//在邻居里
				msg.mcTimer=jxTimer.DoAfter(jxMsg.AutoDeleteDelay, new IDo(){
					@Override
					public void Do(Object param) throws Exception {
						MsgCenter.WaitPostMsgs.offer((jxMsg)param);
					}
				}, msg);
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
	public static void Post(DB db,jxMsg msg) throws Exception
	{
		
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
				jxMsg mo=Msgs.get(msg.ID);
				if(mo!=null)
				{
					mo.mcTimer.cancel();
					mo.DelaySave(null);
					MsgCenter.Msgs.remove(mo.ID);
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
			ReceivedMsg=(jxMsg) jxMsg.Create(jxMsg.class);
			ReceivedMsg.State=jxMsgState.Received;
		}
		ReceivedMsg.Sender=msg.Receiver;
		ReceivedMsg.Receiver=msg.Sender;
		ReceivedMsg.ID=msg.ID;
		return ReceivedMsg;
	}
	

	static InetAddress LanServer=null;
	static InetAddress JXServer=null;
	static Map<UUID,InetAddress> LanNeighbor=new HashMap<UUID,InetAddress>();
	
	
	
	

	//只会注册在本机上
			
}





