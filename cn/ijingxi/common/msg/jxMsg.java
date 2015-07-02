
package cn.ijingxi.common.msg;

import java.io.UnsupportedEncodingException;
import java.util.*;

import cn.ijingxi.common.app.TopSpace;
import cn.ijingxi.common.app.jxSystem;
import cn.ijingxi.common.orm.ORM;
import cn.ijingxi.common.orm.ORMID;
import cn.ijingxi.common.orm.ORMType;
import cn.ijingxi.common.orm.jxORMobj;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.IDo;
import cn.ijingxi.common.util.IjxEnum;
import cn.ijingxi.common.util.Trans;
import cn.ijingxi.common.util.jxTimer;
import cn.ijingxi.common.util.utils;

/**
 * 带确认的消息传递，全局
 * @author andrew
 *
 */
public class jxMsg extends jxORMobj implements Iterable<byte[]>
{
    static final int Msg_Version = 1;
	
		
	public static void Init() throws Exception
	{
		InitClass(ORMType.jxMsg.ordinal(),jxMsg.class);
	}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(jxMsg.class,null);
	}

	@Override
	protected void Init_Create() throws Exception
	{
		ID=UUID.randomUUID();
	}
	//发送方的消息号
	@ORM(keyType=KeyType.PrimaryKey)
	public UUID ID;
	@ORM
	public UUID Sender;
	@ORM(Index=1)
	public int STypeID;
	@ORM(Index=1)
	public UUID SID;
	
	public ORMID getSenderID()
	{
		return new ORMID(STypeID,SID);
	}
	public void setSenderID(ORMID SenderID)
	{
		STypeID=SenderID.getTypeID();
		SID=SenderID.getID();
	}

	@ORM(Index=1)
	public UUID Receiver;
	@ORM(Index=1)
	public int RTypeID;
	@ORM(Index=1)
	public UUID RID;
	public ORMID getReceiverID()
	{
		return new ORMID(RTypeID,RID);
	}
	public void setReceiverID(ORMID SenderID)
	{
		RTypeID=SenderID.getTypeID();
		RID=SenderID.getID();
	}

	@ORM
	public jxMsgType MsgType;
	
	//邮件状态，默认是在发送，主要用于消息确认，要注意是消息传递过程中的还是有不一样的
	@ORM
	public jxMsgState State=jxMsgState.Waiting;
	

	
	//@ORM(Encrypted=true)
	//public String MSG;

	@ORM(Descr="json格式的附加信息",Encrypted=true)
	public String Info;
	public void SetParam(String PName,Object value) throws Exception
	{
		setExtendValue("Info",PName,value);
	}	
	public String GetParam(String PName) throws Exception
	{
		return getExtendValue("Info",PName);
	}	
	public void setMsg(String Msg) throws Exception
	{
		SetParam("Msg",Msg);
	}	
	public String getMsg() throws Exception
	{
		return GetParam("Msg");
	}
	public void setObj(jxORMobj obj) throws Exception
	{
		setObj("Info",obj);
	}	
	public jxORMobj getObj() throws Exception
	{
		return getObj("Info");
	}
	
	jxTimer mcTimer=null;
	

	static Map<UUID,ReceiveBufferMsg> BlockBuffer=new HashMap<UUID,ReceiveBufferMsg>();
	
	
	//@ORM(Descr="json格式的目标对象",Encrypted=true)
	//public String Obj;
	
	public TopSpace getTopSpace()
	{
		try {
			String str = GetParam("tsid");
			if(str!=null)
				return (TopSpace) TopSpace.GetByID(TopSpace.class, Trans.TransToUUID(GetParam("tsid")),null);
		} catch (Exception e) {		}
		return null;
	}
	public IjxEnum getEvent() throws Exception
	{
		if(MsgType==jxMsgType.Event)
		{
			String et=getExtendValue("Info","EventType");
			if(et==null||et=="")return null;
			String ev=getExtendValue("Info","Event");
			return (IjxEnum) Trans.TransTojxEunm(et, Trans.TransToInteger(ev));
		}
		return null;
	}

	public static jxMsg NewMsg(jxMsgType MsgType,TopSpace ts,ORMID SenderID,UUID Receiver,ORMID ReceiverID,String MSG) throws Exception
	{
		jxMsg msg=(jxMsg) jxMsg.Create(jxMsg.class);
		msg.Sender=jxSystem.SystemID;
		msg.setSenderID(SenderID);
		msg.Receiver=Receiver;
		if(ReceiverID!=null)
			msg.setReceiverID(ReceiverID);
		msg.MsgType=MsgType;
		if(MSG!=null)
			msg.setMsg(MSG);
		if(ts!=null)
			msg.SetParam("tsid",Trans.TransToString(ts.ID));
		return msg;
	}
	public static jxMsg NewRichMsg(TopSpace ts,ORMID SenderID,UUID Receiver,ORMID ReceiverID,String MSG) throws Exception
	{
		jxMsg msg=(jxMsg) jxMsg.Create(jxMsg.class);
		msg.Sender=jxSystem.System.ID;
		msg.setSenderID(SenderID);
		msg.Receiver=Receiver;
		msg.setReceiverID(ReceiverID);
		msg.MsgType=jxMsgType.RichText;
		msg.setMsg(MSG);
		msg.SetParam("tsid",Trans.TransToString(ts.ID));
		return msg;
	}
	public static jxMsg NewEventMsg(TopSpace ts,UUID uuid,UUID Receiver,ORMID ReceiverID,IjxEnum Event,String MSG) throws Exception
	{
		jxMsg msg=(jxMsg) jxMsg.Create(jxMsg.class);
		msg.Sender=jxSystem.System.ID;
		//msg.setSenderID(uuid);
		msg.Receiver=Receiver;
		msg.setReceiverID(ReceiverID);
		msg.MsgType=jxMsgType.Event;
		if(MSG!=null)
			msg.setMsg(MSG);
		msg.SetParam("tsid",Trans.TransToString(ts.ID));
		msg.SetParam("EventType",utils.GetClassName(Event.getClass()));
		msg.SetParam("Event",Trans.TransToInteger(Event));		
		return msg;
	}
	
    public static jxMsg ReceiveBlock(byte[] Block) throws Exception
    {
    	ReceiveBufferMsg rb=null;
		int msgLength=Trans.TransToInteger(Block, jxMsg.BlockStart_MsgLength);
		if(msgLength>jxMsg.Block_DataSize)
		{
	    	UUID mid=Trans.TransToUUID(Block, jxMsg.BlockStart_MsgID);
	    	rb=BlockBuffer.get(mid);
	    	if(rb==null)
	    	{
	    		rb=new ReceiveBufferMsg(msgLength);
				BlockBuffer.put(mid, rb);
	    	}
		}
		else
    		rb=new ReceiveBufferMsg(msgLength);
		
    	return rb.AddBlock(Block);
    }

    static final int Data_MaxSize = 1024*1024*64;
    static final int BlockUUID_Size = 16;
    static final int BlockLong_Size = 8;
    static final int BlockInt_Size = 4;
    static final int BlockORMID_Size = BlockInt_Size+BlockUUID_Size;
    static final int BlockStart_Sender = 0;
    static final int BlockStart_SenderID = BlockUUID_Size;
    static final int BlockStart_Receiver = BlockStart_SenderID+BlockORMID_Size;
    static final int BlockStart_ReceiverID = BlockStart_Receiver+BlockUUID_Size;
    static final int BlockStart_MsgID = BlockStart_ReceiverID+BlockORMID_Size;
    static final int BlockStart_Version = BlockStart_MsgID+BlockUUID_Size;
    static final int BlockStart_MsgState = BlockStart_Version+BlockInt_Size;
    static final int BlockStart_MsgLength = BlockStart_MsgState+BlockInt_Size;
    static final int BlockStart_MsgType = BlockStart_MsgLength+BlockInt_Size;
    static final int BlockStart_BlockID = BlockStart_MsgType+BlockInt_Size;
    static final int BlockStart_Msg = BlockStart_BlockID+BlockInt_Size;
    static final int Block_DataSize = 1360;
    static final int Block_HeadSize = BlockStart_Msg-BlockStart_Sender;
    public static final int Block_Size = Block_HeadSize+Block_DataSize;
    //180秒还没收全则自动删除
    static final int AutoDeleteDelay = 10;
    /** 
     * 实现Iterable接口中要求实现的方法 
     */  
    @Override  
    public Iterator<byte[]> iterator() 
    {  
        return new MyIterator();//返回一个MyIterator实例对象  
    }        
    /** 
     * MyIterator是内部类，实现了Iterator<E>接口的类 
     */  
    class MyIterator implements Iterator<byte[]>
    {	
    	//消息的字节数组长度
    	Integer msgLength=0;
    	byte[] Data=null;
    	byte[] bs=null;
    	Integer BlockID=0;
    	Integer BlockNum=0;
    	MyIterator()
    	{
			try {
	    		if(Info!=null){
					Data=Info.getBytes("UTF8");
					msgLength=Data.length;
	    		}
				if(msgLength>Data_MaxSize)
				{
					Data=Info.getBytes("UTF8");
					msgLength=Data.length;
					return;
				}
					if(msgLength<=Block_DataSize)
					{
						bs=new byte[Block_HeadSize+msgLength];
						BlockNum=1;
					}
					else
					{
						bs=new byte[Block_Size];
						BlockNum=msgLength/Block_DataSize+((msgLength%Block_DataSize==0)?0:1);
					}
					Trans.TransToByteArray(bs, BlockStart_Sender,Sender);
					Trans.TransToByteArray(bs, BlockStart_SenderID,getSenderID());
					Trans.TransToByteArray(bs, BlockStart_Receiver,Receiver);
					Trans.TransToByteArray(bs, BlockStart_ReceiverID,getReceiverID());
					Trans.TransToByteArray(bs, BlockStart_MsgID,ID);
					Trans.TransToByteArray(bs, BlockStart_Version,Msg_Version);
					//用于消息确认
					Trans.TransToByteArray(bs, BlockStart_MsgState,State.ordinal());
					Trans.TransToByteArray(bs, BlockStart_MsgType,MsgType.ordinal());
					Trans.TransToByteArray(bs, BlockStart_MsgLength,msgLength);				
			} catch (UnsupportedEncodingException e) {
			}
    	}
    	
        @Override  
        public boolean hasNext() {  
            return BlockID<BlockNum;  
        }
        @Override  
        public byte[] next() 
        {
        	if(msgLength==0)
        	{
        		BlockID++;
        		return bs;
        	}
			Trans.TransToByteArray(bs, BlockStart_BlockID,BlockID);
    		int start=Block_DataSize*BlockID;
    		int end=start+Block_DataSize;
    		BlockID++;
    		if(msgLength<end)
    		{
    			int len=msgLength-start;
    			if(start>0)
    			{
	    			byte[] btemp=new byte[Block_HeadSize+len];
	    			System.arraycopy(bs, 0, btemp, 0, Block_HeadSize);
    				System.arraycopy(Data, start, btemp, BlockStart_Msg, len);
    	    		return btemp;
    			}
    			else
    				System.arraycopy(Data, start, bs, BlockStart_Msg, len);
    		}
    		else
    			System.arraycopy(Data, start, bs, BlockStart_Msg, Block_DataSize);
    		return bs;
        } 
        @Override  
        public void remove() {  
            //未实现这个方法  
        }            
    }	
	
}


class ReceiveBufferMsg
{
	jxMsg msg=null;
	byte[] Data=null;
	jxTimer autodelete=null;
	boolean[] received=null;
	int msgLength=0;
	int blockNum=0;
	ReceiveBufferMsg(int msgLength)
	{
		this.msgLength=msgLength;
	}
	jxMsg AddBlock(byte[] Block) throws Exception
	{
		if(msg==null)
		{
			msg=(jxMsg) jxMsg.Create(jxMsg.class);
			msg.Sender=Trans.TransToUUID(Block, jxMsg.BlockStart_Sender);
			msg.setSenderID(Trans.TransToORMID(Block, jxMsg.BlockStart_SenderID));
			msg.Receiver=Trans.TransToUUID(Block, jxMsg.BlockStart_Receiver);
			msg.setReceiverID(Trans.TransToORMID(Block, jxMsg.BlockStart_ReceiverID));
			msg.ID=Trans.TransToUUID(Block, jxMsg.BlockStart_MsgID);
			msg.State=(jxMsgState) Trans.TransTojxEunm(jxMsgState.class, Trans.TransToInteger(Block, jxMsg.BlockStart_MsgState));
			msg.MsgType=(jxMsgType) Trans.TransTojxEunm(jxMsgType.class, Trans.TransToInteger(Block, jxMsg.BlockStart_MsgType));
			if(msgLength==0)
				return msg;
			else if(msgLength<=jxMsg.Block_DataSize)
			{
				msg.Info=new String(Block,jxMsg.BlockStart_Msg,msgLength,"UTF8");
				return msg;
			}
			Data=new byte[msgLength];
			blockNum=msgLength/jxMsg.Block_DataSize+((msgLength%jxMsg.Block_DataSize==0)?0:1);
    		received=new boolean[blockNum];

    		autodelete=jxTimer.DoAfter(jxMsg.AutoDeleteDelay, new IDo(){
				@Override
				public void Do(Object param) throws Exception {
	    			jxMsg.BlockBuffer.remove(((ReceiveBufferMsg)param).msg.ID);
				}
    		}, this);
		}    		
		int blockid=Trans.TransToInteger(Block, jxMsg.BlockStart_BlockID);
		int start=jxMsg.Block_DataSize*blockid;
		int end=start+jxMsg.Block_DataSize;
		int len=(msgLength<end)?msgLength-start:jxMsg.Block_DataSize;
		System.arraycopy(Block, jxMsg.BlockStart_Msg, Data, start, len);
		received[blockid]=true;
		
		for(int i=0;i<received.length;i++)
			if(!received[i])
				return null;
		autodelete.cancel();
		jxMsg.BlockBuffer.remove(msg.ID);
		msg.Info=new String(Data,"UTF8");
		return msg;
	}
}


