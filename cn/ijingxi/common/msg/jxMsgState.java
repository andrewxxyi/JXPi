
package cn.ijingxi.common.msg;

import cn.ijingxi.common.util.IjxEnum;

public enum jxMsgState implements IjxEnum
{
	//新消息
	New,
	//正在发送中
	Waiting,
	//正在发送中
	Sending,
	//已发送
	Sended,
	//已接收
	Received,
	//已投递
	Posted,
	//已处理
	Dualed;
		
	@Override
	public Object TransToORMEnum(Integer param) 
	{
		return jxMsgState.values()[param];
	}
}