   
package cn.ijingxi.common.util;   
   
/**  
 * 有限状态自动机
 * @author Andrew Xu  
 */   
public class jxStateMachine<TState extends Enum<?>,TEvent extends Enum<?>,TParam>
{
	class SMTrans
	{
		TState TransToState=null;
		IDoSomething doCaller=null;		
	}	
	//当前状态，本状态机并不保存当前状态，而负责执行
    //private Integer m_CurrentState = 0;
    //状态跳转树
    jxSparseTable<String,String,SMTrans> m_SMTree=new jxSparseTable<String,String,SMTrans>();
    
    //public jxStateMachine(Integer CurrentState){m_CurrentState=CurrentState;}
    
    /**
     * 添加状态变迁
     * @param FromState 在什么状态下
     * @param Event 发生了什么事情
     * @param ToState 跳转到什么状态
     * @param ToDo 同时执行什么动作
     */
    public void AddTrans(TState FromState,TEvent Event,TState ToState,IDoSomething ToDo)
    {
    	String fs=FromState.name(),e=Event.name();
    	SMTrans trans=new SMTrans();
    	trans.TransToState=ToState;
    	trans.doCaller=ToDo;
    	m_SMTree.Add(fs, e, trans);
    }
    /**
     * 发生了什么事件
     * @param Event 事件
     * @param param 参数
     * @throws Exception 
     */
    public TState Happen(TState CurrentState,TEvent Event,TParam param) throws Exception
    {
    	String fs=CurrentState.name(),e=Event.name();
	    	SMTrans s=m_SMTree.Search(fs, e);
	    	if(s!=null)
	    	{
	    		s.doCaller.Do(param);
	    		return s.TransToState;
	    	}
	    	throw new Exception(String.format("当前状态：%s 时发生了事件 %s，未找到相应的状态转换", CurrentState.name(),Event.name()));
    }

}