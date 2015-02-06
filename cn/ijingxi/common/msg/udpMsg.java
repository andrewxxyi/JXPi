
package cn.ijingxi.common.msg;

import java.io.IOException;
import java.net.*;



public class udpMsg
{
	private static final String BROADCAST_IP = "239.1.0.5";
    private static final int BROADCAST_INT_PORT = 60005;

  //用于接收广播信息
    static MulticastSocket broadSocket=null;
  //广播地址
    static InetAddress broadAddress=null;
    static DatagramSocket sender = null;
    static Thread ReceiveMsg=null;
    public static void Init() throws IOException
    {
    	broadSocket=new MulticastSocket(BROADCAST_INT_PORT);
    	broadAddress=InetAddress.getByName(BROADCAST_IP);
    	sender = new DatagramSocket();
    	//加入到组播地址，这样就能接收到组播信息
    	broadSocket.joinGroup(broadAddress); 
    	ReceiveMsg=new Thread(
    			new Runnable()  
    			{
    				@Override
    				public void run() {
    		    		DatagramPacket inPacket;
    		     	   while(true)
    		     	   {
    		     		   inPacket=new DatagramPacket(new byte[jxMsg.Block_Size], jxMsg.Block_Size);
    		     		   //接收广播信息并将信息封装到inPacket中
    		     		   try {
	    		 				broadSocket.receive(inPacket);
	    		 				jxMsg msg=jxMsg.ReceiveBlock(inPacket.getData());
	    		 				if(msg!=null)
	    		 					MsgCenter.ReceivedMsg(msg);
    		 			} catch (Exception e) {
    		 				// TODO Auto-generated catch block
    		 				e.printStackTrace();
    		 			}
    		      	   }
    				}
    			}
    		);
    	ReceiveMsg.setPriority(Thread.MAX_PRIORITY);
    	ReceiveMsg.start();
    }
    public static void send(InetAddress ia,jxMsg msg) throws IOException
    {
 	   //广播信息到指定端口
	  //数据包，相当于集装箱，封装信息
    	for(byte[] bs:msg)
    	{
		    DatagramPacket packet=new DatagramPacket(bs, bs.length, ia==null?broadAddress:ia, BROADCAST_INT_PORT); 
		    sender.send(packet);
    	}
    }
}