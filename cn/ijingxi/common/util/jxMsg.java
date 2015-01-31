
package cn.ijingxi.common.util;

import java.io.UnsupportedEncodingException;
import java.util.*;

import cn.ijingxi.common.app.jxSystem;
import cn.ijingxi.common.orm.ORM;
import cn.ijingxi.common.orm.ORMID;
import cn.ijingxi.common.orm.jxORMobj;
import cn.ijingxi.common.orm.ORM.KeyType;

/**
 * 带确认的消息传递
 * @author andrew
 *
 */
public class jxMsg extends jxORMobj implements Iterable<byte[]>
{
	
	public static ORMID GetORMID(Integer ID)
	{
		return new ORMID(GetTypeID("jxMsg"),ID);
	}
	
	public static void Init() throws Exception
	{
		InitClass(jxMsg.class);
	}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(jxMsg.class);
	}

	@ORM(keyType=KeyType.PrimaryKey)
	public UUID Sender;
	//发送方的消息号
	@ORM(keyType=KeyType.PrimaryKey)
	public Long MsgID;

	@ORM(Index=1)
	public UUID Receiver;
	@ORM(Index=1)
	public ORMID ReceiverID;

	@ORM
	public jxMsgType MsgType;
	
	//邮件状态，默认是在发送，主要用于消息确认，要注意是消息传递过程中的还是有不一样的
	@ORM
	public jxMsgState State=jxMsgState.Sending;
	

	
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
	
	Timer mcTimer=null;
	

	static jxSparseTable<UUID,Long,ReceiveBufferMsg> BlockBuffer=new jxSparseTable<UUID,Long,ReceiveBufferMsg>();
	
	
	//@ORM(Descr="json格式的目标对象",Encrypted=true)
	//public String Obj;
	
	
	public IjxEnum getEvent() throws Exception
	{
		if(MsgType==jxMsgType.Event)
		{
			String et=getExtendValue("Addition","EventType");
			String ev=getExtendValue("Addition","Event");
			return (IjxEnum) Trans.TransTojxEunm(et, Trans.TransToInteger(ev));
		}
		return null;
	}
	
	protected jxMsg() throws Exception
	{
		super();
	}
	
	public static jxMsg NewRichMsg(UUID Receiver,ORMID ReceiverID,String MSG) throws Exception
	{
		jxMsg msg=(jxMsg) jxMsg.New(jxMsg.class);
		msg.Sender=jxSystem.System.SystemUUID;
		msg.Receiver=Receiver;
		msg.ReceiverID=ReceiverID;
		msg.MsgID=jxSystem.System.GetMsgID();
		msg.MsgType=jxMsgType.RichText;
		msg.setMsg(MSG);
		return msg;
	}
	public static jxMsg NewEventMsg(UUID Receiver,ORMID ReceiverID,IjxEnum Event,String MSG) throws Exception
	{
		jxMsg msg=(jxMsg) jxMsg.New(jxMsg.class);
		msg.Sender=jxSystem.System.SystemUUID;
		msg.Receiver=Receiver;
		msg.ReceiverID=ReceiverID;
		msg.MsgID=jxSystem.System.GetMsgID();
		msg.MsgType=jxMsgType.Event;
		msg.setMsg(MSG);
		msg.SetParam("EventType",utils.GetClassName(Event.getClass()));
		msg.SetParam("Event",Trans.TransToInteger(Event));		
		return msg;
	}
	
    static jxMsg ReceiveBlock(byte[] Block) throws Exception
    {
    	ReceiveBufferMsg rb=null;
		int msgLength=Trans.TransToInteger(Block, jxMsg.BlockStart_MsgLength);
		if(msgLength>jxMsg.Block_DataSize)
		{
	    	UUID uuid=Trans.TransToUUID(Block, jxMsg.BlockStart_Sender);
	    	long mid=Trans.TransToLong(Block, jxMsg.BlockStart_MsgID);
	    	rb=BlockBuffer.Search(uuid, mid);
	    	if(rb==null)
	    	{
	    		rb=new ReceiveBufferMsg(msgLength);
				BlockBuffer.Add(uuid, mid, rb);
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
    static final int BlockStart_Sender = 0;
    static final int BlockStart_Receiver = BlockUUID_Size;
    static final int BlockStart_ReceiverID = BlockStart_Receiver+BlockLong_Size;
    static final int BlockStart_MsgID = BlockStart_ReceiverID+BlockUUID_Size;
    static final int BlockStart_MsgState = BlockStart_MsgID+BlockLong_Size;
    static final int BlockStart_MsgLength = BlockStart_MsgState+BlockInt_Size;
    static final int BlockStart_MsgType = BlockStart_MsgLength+BlockInt_Size;
    static final int BlockStart_BlockID = BlockStart_MsgType+BlockInt_Size;
    static final int BlockStart_Msg = BlockStart_BlockID+BlockInt_Size;
    static final int Block_DataSize = 1360;
    static final int Block_HeadSize = BlockStart_Msg-BlockStart_Sender;
    static final int Block_Size = Block_HeadSize+Block_DataSize;
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
					Trans.TransToByteArray(bs, BlockStart_Receiver,Receiver);
					Trans.TransToByteArray(bs, BlockStart_ReceiverID,ReceiverID);
					Trans.TransToByteArray(bs, BlockStart_MsgID,MsgID);
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
	Timer autodelete=null;
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
			msg=(jxMsg) jxMsg.New(jxMsg.class);
			msg.Sender=Trans.TransToUUID(Block, jxMsg.BlockStart_Sender);
			msg.Receiver=Trans.TransToUUID(Block, jxMsg.BlockStart_Receiver);
			msg.ReceiverID=Trans.TransToORMID(Block, jxMsg.BlockStart_ReceiverID);
			msg.MsgID=Trans.TransToLong(Block, jxMsg.BlockStart_MsgID);
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

    		CallParam param=new CallParam(null, null, null);
    		param.addParam(this);
    		autodelete=jxTimer.DoAfter(jxMsg.AutoDeleteDelay, null, new AutoDeleteFromBuffer(), param);
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
		jxMsg.BlockBuffer.Delete(msg.Sender, msg.MsgID);
		msg.Info=new String(Data,"UTF8");
		return msg;
	}
}


class AutoDeleteFromBuffer implements IDoSomething
{
	public void Do(CallParam param) throws Exception
	{
		ReceiveBufferMsg ib=(ReceiveBufferMsg)param.getParam();
		jxMsg.BlockBuffer.Delete(ib.msg.Sender, ib.msg.MsgID);
	}    	
}

