
package cn.ijingxi.common.Process;

import cn.ijingxi.common.util.IjxEnum;


	public enum InstanceState implements IjxEnum
	{
		//非活动状态
		NoActive,
		//等待执行
		Waiting,
		//正在执行
		Doing,
		//已执行完毕，不会再执行
		Closed,
		//已取消
		Canceled,
		//暂时停止执行
		Paused;

		@Override
		public Object TransDBToORMEnum(Integer param) 
		{
			return InstanceState.values()[param];
		}
	}