package cn.ijingxi.common.util;

/**
 * 为执行枚举和db的类型转换所必须
 * @author andrew
 *
 */
public interface IjxEnum
{
	public Object TransToORMEnum(Integer param);
}