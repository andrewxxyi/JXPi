
package cn.ijingxi.common.app;

import cn.ijingxi.common.util.IjxEnum;

/**
 * 
 * @author andrew
 *
 */
public enum TSEvent implements IjxEnum
{
	//广播ts的存在
	BroadCast,
	//请求加入
	RequestAdd,
	//拒绝加入
	Reject,
	//同意加入
	Accept,
	//请求开始同步
	RequestSync,
	//同步数据传递
	Sync,
	//最后发送一个数据传送完毕
	SyncEnd,;

	@Override
	public Object TransToORMEnum(Integer param) 
	{
		return TSEvent.values()[param];
	}
	
}