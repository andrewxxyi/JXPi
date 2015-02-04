
package cn.ijingxi.common.Process;

import cn.ijingxi.common.util.IjxEnum;


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
	}