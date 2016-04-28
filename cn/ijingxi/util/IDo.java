package cn.ijingxi.util;

/**
 * 发起某动作/活动/执行某函数者
 * @author andrew
 *
 */
public interface IDo<T>{
	void Do(T param) throws Exception;
}