
package cn.ijingxi.common.app;

import cn.ijingxi.common.util.IjxEnum;

/**
 * 关系都是两个对象之间的关系
 * a RelationType b
 * 所以关系都说的是a相当于b如何如何
 * a Prev b说的就是在时空关系上a在b先
 * a Parent b说的就是在层级关系上a在b上
 * a Contain b说的就是在组合关系上a包含b
 * 
 * @author andrew
 *
 */
public enum RelationType implements IjxEnum
{
	//数量关系
	//一对多
	OneToMulti,
	//多对一
	MultiToOne,
	//一对一
	OneToOne,
	//多对多
	MultiToMulti,
	//在目标对象之前
	Prev,
	//在目标对象之后
	Next,
	//是目标对象的上级
	Parent,
	//隶属于目标对象
	Son,
	//由目标对象组成
	Contain,
	//是目标对象组成的一部分
	Part;

	@Override
	public Object TransToORMEnum(Integer param) 
	{
		return RelationType.values()[param];
	}
	
}