
package cn.ijingxi.common.Process;

import java.util.UUID;

import cn.ijingxi.common.app.TopSpace;
import cn.ijingxi.common.orm.jxORMobj;

public interface IExecutor
{
	public String getName();
	public UUID getID();
	public int getTypeID() throws Exception;
	public UUID getOwnerID() throws Exception;
	//public void Inform(jxEvent<?> event);	
	//比如指定的是某个角色，在运行时需将其转换为具体的某个人，GetInsteadExecutor只获取第一次转换
	//而GetRealExecutor则获取最终的替代者，如
	//1、技术部经理角色可能是经理-技术部经理-某人这样的层次性或组合性指代
	//2、CEO生病了，就需要一个CEO-Insteader角色-Insteader的替代序列-最终才能落实到某人
	//这种替代一定是实时的，不应在开始就完成所有的替代，而是实际分发任务时才执行替代
	//public IExecutor GetInsteadExecutor();
	public IExecutor getRealExecutor();
	boolean CheckRight(TopSpace ts, jxORMobj obj, String RoleName,boolean CheckParent) throws Exception;
}