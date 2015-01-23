
package cn.ijingxi.common.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;

public class udpMsg
{
	private static final String BROADCAST_IP = "239.1.0.5";
    private static final int BROADCAST_INT_PORT = 60005;

  //用于接收广播信息
    MulticastSocket broadSocket=null;
  //广播地址
    InetAddress broadAddress=null;
    DatagramSocket sender = null;

    void Init() throws IOException
    {
    	broadSocket=new MulticastSocket(BROADCAST_INT_PORT);
    	broadAddress=InetAddress.getByName(BROADCAST_IP);
    	sender = new DatagramSocket();
    	//加入到组播地址，这样就能接收到组播信息
    	broadSocket.joinGroup(broadAddress); 
    	new Thread(new GetPacket()).start();
    }
    void send(jxMsg msg) throws IOException
    {
    	if(msg.Data==null||msg.Data.length==0)return;
 	   //广播信息到指定端口
	  //数据包，相当于集装箱，封装信息
    	for(byte[] bs:msg)
    	{
		    DatagramPacket packet=new DatagramPacket(bs, bs.length, broadAddress, BROADCAST_INT_PORT); 
		    sender.send(packet);
    	}
    }
    class GetPacket implements Runnable
    {
    	public void run() 
    	{
    		DatagramPacket inPacket;         
    	   String[] message;
    	   while(true)
    	   {
    		   inPacket=new DatagramPacket(new byte[jxMsg.Block_Size], jxMsg.Block_Size);
    		   //接收广播信息并将信息封装到inPacket中
    		   try {
				broadSocket.receive(inPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		   try {
				jxMsg msg=jxMsg.ReceiveBlock(inPacket.getData());
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

     	   }
    	}
    }
    

}