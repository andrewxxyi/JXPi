   
package cn.ijingxi.util;
   
/**  
 * 有限状态自动机
 * @author Andrew Xu  
 */   
public class jxStateMachine<TState extends Comparable<TState>,TEvent extends Comparable<TEvent>> {

	public class realSM {
		jxStateMachine sm = null;
		Object stateObject=null;
		public Object getStateObject(){return stateObject;}
		TState currentState;

		public void happen(TEvent e, Object param) throws Exception {
			CallParam p = new CallParam(this);
			p.addParam("param", param);

			SMTrans s = m_SMTree.Search(currentState, e);
			if (s != null)
				synchronized (s) {
					currentState = s.TransToState;
					if (s.doCaller != null)
						//只改变状态而不执行动作
						if (async)
							jxTimer.asyncRun(p1 -> s.doCaller.Do(this, param), null);
						else
							s.doCaller.Do(this, param);
				}
		}
	}

	public realSM newRealSM(Object stateObject,TState initState) {
		realSM s = new realSM();
		s.sm = this;
		s.stateObject=stateObject;
		s.currentState = initState;
		return s;
	}

	class SMTrans {
		TState TransToState = null;
		IDo2<realSM,Object> doCaller = null;
	}

	public jxStateMachine() {}

	public jxStateMachine(boolean async) {
		this.async = async;
	}

	private boolean async = false;

	//必须设置
	//private IDo2 setStateFunc = null;

	//public void setStateFunc(IDo2 func) {
	//	setStateFunc = func;
	//}

	//当前状态，本状态机并不保存当前状态，而负责执行
	//private Integer m_CurrentState = 0;
	//状态跳转树
	jxSparseTable<TState, TEvent, SMTrans> m_SMTree = new jxSparseTable<>();

	//public jxStateMachine(Integer CurrentState){m_CurrentState=CurrentState;}

	/**
	 * 添加状态变迁
	 *
	 * @param FromState 在什么状态下
	 * @param Event     发生了什么事情
	 * @param ToState   跳转到什么状态
	 * @param ToDo      同时执行什么动作，其参数1是响应的realSM，参数2是happen所送入的参数
	 */
	public void AddTrans(TState FromState, TEvent Event, TState ToState, IDo2 ToDo) {
		SMTrans trans = new SMTrans();
		trans.TransToState = ToState;
		trans.doCaller = ToDo;
		m_SMTree.Add(FromState, Event, trans);
	}

	/**
	 * 发生了什么事件
	 *
	 * @param Event 事件
	 * @param param 参数
	 * @throws Exception
	 */
	/*
	public void Happen(TState CurrentState, TEvent Event, CallParam param) throws Exception {
		SMTrans s = m_SMTree.Search(CurrentState, Event);
		if (s != null) {
			setStateFunc.Do(param, s.TransToState);
			if (s.doCaller != null)
				//只改变状态而不执行动作
				if (async)
					jxTimer.asyncRun_CallParam(s.doCaller, param);
				else
					s.doCaller.Do(param);
		}
		//throw new Exception(String.format("当前状态：%s 时发生了事件 %s，未找到相应的状态转换", CurrentState.name(),Event.name()));
	}

	*/


}