
package cn.ijingxi.common.Process;

import cn.ijingxi.common.util.IjxEnum;


	public enum InstanceState implements IjxEnum
	{
		None,
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
		public Object TransToORMEnum(Integer param) 
		{
			return InstanceState.values()[param];
		}

		@Override
		public String toChinese()
		{
			switch(this)
			{
			case None:
				return "空";
			case NoActive:
				return "非活动";
			case Waiting:
				return "等待";
			case Doing:
				return "执行中";
			case Closed:
				return "执行完毕";
			case Canceled:
				return "已取消";
			case Paused:
				return "暂停";
			}
			return "";
		}
	}