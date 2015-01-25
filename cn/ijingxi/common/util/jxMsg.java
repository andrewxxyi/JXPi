
package cn.ijingxi.common.util;

import java.io.UnsupportedEncodingException;
import java.util.*;

import cn.ijingxi.common.orm.ORM;
import cn.ijingxi.common.orm.jxORMobj;
import cn.ijingxi.common.orm.ORM.KeyType;

public class jxMsg extends jxORMobj implements Iterable<byte[]>
{
	public static void Init() throws Exception
	{
		InitClass(jxMsg.class);
	}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(jxMsg.class);
	}

	@ORM(keyType=KeyType.PrimaryKey)
	public UUID SenderID;
	//发送方的消息号
	@ORM(keyType=KeyType.PrimaryKey)
	public Long MsgTime;

	@ORM(Index=1)
	public UUID ReceiverID;

	@ORM
	public jxMsgType MsgType;
	
	@ORM(Encrypted=true)
	public String MSG;

	@ORM(Descr="json格式的附加信息",Encrypted=true)
	public String Addition;
	public void SetParam(String PName,String value) throws Exception
	{
		setExtendValue("Addition",PName,value);
	}	
	public String GetParam(String PName) throws Exception
	{
		return getExtendValue("Addition",PName);
	}
	
	@ORM(Descr="json格式的目标对象",Encrypted=true)
	public String Obj;
	
	
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
		MsgTime=(new Date()).getTime();
	}
	
	public static jxMsg NewRichMsg(UUID SenderID,UUID ReceiverID,String MSG) throws Exception
	{
		jxMsg msg=(jxMsg) jxMsg.New(jxMsg.class);
		msg.SenderID=SenderID;
		msg.ReceiverID=ReceiverID;
		msg.MsgType=jxMsgType.RichText;
		msg.MSG=MSG;
		return msg;
	}
	public static jxMsg NewEventMsg(UUID SenderID,UUID ReceiverID,IjxEnum Event,String MSG) throws Exception
	{
		jxMsg msg=(jxMsg) jxMsg.New(jxMsg.class);
		msg.SenderID=SenderID;
		msg.ReceiverID=ReceiverID;
		msg.MsgType=jxMsgType.Event;
		msg.MSG=MSG;
		msg.setExtendValue("Addition","EventType",utils.GetClassName(Event.getClass()));
		msg.setExtendValue("Addition","Event",Trans.TransToInteger(Event));		
		return msg;
	}
	

	//消息的字节数组长度
	Integer MsgLength=0;
	byte[] Data=null;
	
	public jxMsg(UUID SenderID,UUID ReceiverID,String MSG) throws Exception
	{
		this.SenderID=SenderID;
		this.ReceiverID=ReceiverID;
		Date d=new Date();
		MsgTime=d.getTime();
		this.MSG=MSG;
		this.MsgType=jxMsgType.Text;
	}
	public jxMsg(String ReceiverName,String MSG) throws Exception
	{
		this.SenderID=SenderID;
		this.ReceiverID=ReceiverID;
		Date d=new Date();
		MsgTime=d.getTime();
		this.MSG=MSG;
		this.MsgType=jxMsgType.Text;
	}
	public jxMsg(String ReceiverName,String MSG,IjxEnum Event,CallParam param) throws Exception
	{
		this.SenderID=SenderID;
		this.ReceiverID=ReceiverID;
		Date d=new Date();
		MsgTime=d.getTime();
		this.MSG=MSG;
		this.MsgType=jxMsgType.Event;
	}

    static final int Block_Size = 1380;
    static final int BlockData_Size = 1300;
    //180秒还没收全则自动删除
    static final int AutoDeleteDelay = 180;

	static UUID getSenderID(byte[] Block) throws UnsupportedEncodingException
	{
		byte[] bs=new byte[32];
		System.arraycopy(Block, 0, bs, 0, 32);
		String str=new String(bs,"UTF8");
		return Trans.TransToUUID(str);
	}
	static long getMsgTime(byte[] Block)
	{
		return Trans.TransToLong(Block, 32);
	}

	static jxSparseTable<UUID,Long,ReceiveBufferMsg> BlockBuffer=new jxSparseTable<UUID,Long,ReceiveBufferMsg>();
	
    static jxMsg ReceiveBlock(byte[] Block) throws UnsupportedEncodingException
    {
    	jxMsg msg=null;
    	UUID uuid=getSenderID(Block);
    	long mid=getMsgTime(Block);
    	int mlen=Trans.TransToInteger(Block, 72);
    	if(mlen==Block.length-80)
    	{
    		msg=new jxMsg();
    		byte[] bs=new byte[32];
    		System.arraycopy(Block, 0, bs, 0, 32);
    		msg.SenderIDSTR=new String(bs,"UTF8");
    		msg.MsgTime=Trans.TransToLong(Block, 32);
    		System.arraycopy(Block, 0, bs, 40, 32);
    		msg.ReceiverIDSTR=new String(bs,"UTF8");
    		msg.MsgLength=mlen;
    		msg.Data=new byte[msg.MsgLength]; 
    		System.arraycopy(Block, 80, msg.Data, 0, msg.MsgLength);
    		msg.MSG=new String(msg.Data,"UTF8");
    		return msg;
    	}
    	ReceiveBufferMsg rb=BlockBuffer.Search(uuid, mid);
    	if(rb==null)
    	{
    		rb=new ReceiveBufferMsg();
    		BlockBuffer.Add(uuid, mid, rb);
    	}
		msg=rb.AddBlock(Block);
		if(msg!=null)
		{
			BlockBuffer.Delete(uuid, mid);
			rb.autodelete.cancel();
			return msg;
		}
		return null;
    }

    
    /** 
     * 实现Iterable接口中要求实现的方法 
     */  
    @Override  
    public Iterator<byte[]> iterator() 
    {  
        return new MyIterator();//返回一个MyIterator实例对象  
    }        
    /** 
     * MyIt	
    class ReceiveBufferMsg

erator是内部类，实现了Iterator<E>接口的类 
     */  
    class MyIterator implements Iterator<byte[]>
    {
    	Integer BlockNum=0;
        @Override  
        public boolean hasNext() {  
            return BlockNum>=0;  
        }
        @Override  
        public byte[] next() 
        {  
    		if(BlockNum<0)
    			//已经结束
    			return null;
    		byte[] Block=null;
    		int start=BlockData_Size*BlockNum;
    		int end=BlockData_Size*(BlockNum+1);
    		int len=0;
    		if(MsgLength<end)
    		{
    			len=MsgLength-start;
    			Block=new byte[80+len];
    		}
    		else
    		{
    			len=BlockData_Size;
    			Block=new byte[Block_Size];
    		}
    		byte[] bs=null;
			try {
    			Block=new byte[Block_Size];
    			bs=SenderID.getBytes("UTF8");
    			System.arraycopy(bs, 0, Block, 0, 32);
    			Trans.TransToByteArray(Block, 32, MsgTime);
    			bs=ReceiverID.getBytes("UTF8");
			} catch (UnsupportedEncodingException e) {
				return null;
			}
			System.arraycopy(bs, 0, Block, 40, 32);
			Trans.TransToByteArray(Block, 72,MsgLength);
			Trans.TransToByteArray(Block, 76, BlockNum);
			System.arraycopy(Data, start, Block, 80, len);
    		if(end<MsgLength)
        		BlockNum++;
    		else
    			BlockNum=-1;
    		return Block;
        } 
        @Override  
        public void remove() {  
            //未实现这个方法  
        }            
    }	
	
}


class ReceiveBufferMsg
{
	Timer autodelete=null;
	boolean[] received=null;
	jxMsg msg=null;
	UUID getSenderID()
	{
		return msg.getSenderID();
	}
	long getMsgTime()
	{
		return msg.MsgTime;
	}
	
	ReceiveBufferMsg()
	{
		CallParam param=new CallParam(null, null, null);
		param.addParam(this);
		autodelete=jxTimer.DoAfter(jxMsg.AutoDeleteDelay, null, new AutoDeleteFromBuffer(), param);
	}

	jxMsg AddBlock(byte[] Block) throws UnsupportedEncodingException
	{
		msg=new jxMsg();
		if(msg==null)
		{
    		byte[] bs=new byte[32];
    		System.arraycopy(Block, 0, bs, 0, 32);
    		msg.SenderIDSTR=new String(bs,"UTF8");
    		msg.MsgTime=Trans.TransToLong(Block, 32);
    		System.arraycopy(Block, 0, bs, 40, 32);
    		msg.ReceiverIDSTR=new String(bs,"UTF8");
    		msg.MsgLength=Trans.TransToInteger(Block,72);
    		msg.Data=new byte[msg.MsgLength];   
    		int num=msg.MsgLength/jxMsg.BlockData_Size;
    		if(msg.MsgLength%jxMsg.BlockData_Size!=0)num++;    		
    		received=new boolean[num];    		 		
		}
		int bn=Trans.TransToInteger(Block, 76);
		int start=jxMsg.BlockData_Size*bn;
		int end=jxMsg.BlockData_Size*(bn+1);
		int len=0;
		if(msg.MsgLength<end)
			len=msg.MsgLength-start;
		else
			len=jxMsg.BlockData_Size;
		System.arraycopy(Block, 80, msg.Data, start, len);
		received[bn]=true;
		for(int i=0;i<received.length;i++)
			if(!received[i])
				return null;
		msg.MSG=new String(msg.Data,"UTF8");
		return msg;
	}
}


class AutoDeleteFromBuffer implements IDoSomething
{
	public void Do(CallParam param) throws Exception
	{
		ReceiveBufferMsg ib=(ReceiveBufferMsg)param.getParam();
		jxMsg.BlockBuffer.Delete(ib.getSenderID(), ib.getMsgTime());
	}    	
}

