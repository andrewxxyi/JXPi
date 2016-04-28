
package cn.ijingxi.Process;

import cn.ijingxi.util.IjxEnum;


	public enum Right implements IjxEnum
	{
		None,
		Read,
		Write,
		Exec;

		@Override
		public Object TransToORMEnum(Integer param) 
		{
			return Right.values()[param];
		}

		@Override
		public String toChinese()
		{
			switch(this)
			{
			case None:
				return "空";
			case Read:
				return "读";
			case Write:
				return "写";
			case Exec:
				return "执行";
			}
			return "";
		}
	}