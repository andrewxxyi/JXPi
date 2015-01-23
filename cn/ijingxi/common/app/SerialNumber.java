
package cn.ijingxi.common.app;

import java.util.*;

import cn.ijingxi.common.Process.*;
import cn.ijingxi.common.util.*;

class SN
{
    private enum SNState{None,Common,Model}
    private enum Event_InputChar{Common,Bracket_L,Bracket_R}
    
	static jxStateMachine<SNState,Event_InputChar> SNSM=null;
    static
    {
    	SNSM.AddTrans(SNState.None, Event_InputChar.Common, SNState.Common, new SN_InputCommon());
    	SNSM.AddTrans(SNState.None, Event_InputChar.Bracket_L, SNState.Model, new SN_StartModel());
    	SNSM.AddTrans(SNState.Common, Event_InputChar.Common, SNState.Common, new SN_InputCommon());
    	SNSM.AddTrans(SNState.Common, Event_InputChar.Bracket_L, SNState.Model, new SN_StartModel());
    	SNSM.AddTrans(SNState.Model, Event_InputChar.Common, SNState.Model, new SN_ModelInput());
    	SNSM.AddTrans(SNState.Model, Event_InputChar.Bracket_R, SNState.Common, new SN_EndModel());
    }

    String Purpose = null;
    String SN_Model = null;
    int Number=0;
    int TatolNumber=0;
    Calendar SN_Time=null;
    String SN_RS = null;
    
    SNState state=SNState.None;
    
    SN(String Purpose,String Model,Date Time)
    {
    	this.Purpose=Purpose;
    	SN_Time = Calendar.getInstance();
        // 初始化 Calendar 对象，但并不必要，除非需要重置时间  
    	if(Time!=null)
    	{
    		SN_Time.setTime(Time);  
    		SN_Time=utils.GetDate(SN_Time);
    	}
    	else
    	{
        	SN_Time.setTime(new Date());  
        	SN_Time=utils.GetDate(SN_Time);    		
    	}
        SN_Model=Model;    
    }
    int GetSN_Day()
    {
    	Calendar cal= utils.GetDate();
    	if(SN_Time.compareTo(cal)<0)
    	{
    		SN_Time=cal;
    		Number=0;
    	}
    	Number++;
    	return Number;
    }
    int GetSN_Total()
    {
    	TatolNumber++;
    	return TatolNumber;
    }

    String GetNumber(IExecutor caller) throws Exception
    {
    	SN_RS=null;
    	state=SNState.None;
    	for(int i=0;i<SN_Model.length();i++)
    	{
    		String sub=SN_Model.substring(i, i+1);
    		Event_InputChar e=Event_InputChar.Common;
            if (sub == "{")
                e = Event_InputChar.Bracket_L;
            else if (sub == "}")
                e = Event_InputChar.Bracket_R;
            CallParam param=new CallParam(null, caller, null);
            param.addParam(this);
            param.addParam(sub);
            state=SNSM.Happen(state, e, param);
    	}
    	return SN_RS;
    }


}
class SN_InputCommon implements IDoSomething
{
	@Override
	public void Do(CallParam param) 
	{
		//第一个参数是SN，第二个参数是送入的字符串
		SN sn= (SN)param.getParam();
		String sub= (String)param.getParam();
		synchronized (sn)
		{
			if(sn.SN_RS==null)
				sn.SN_RS=sub;
			else
				sn.SN_RS+=sub;
		}
	}
}
class SN_StartModel implements IDoSomething
{
	@Override
	public void Do(CallParam param) 
	{
		//第一个参数是SN，第二个参数是送入的字符串
		SN sn= (SN)param.getParam();
		synchronized (sn)
		{
			sn.SN_Model=null;
		}
	}
}
class SN_ModelInput implements IDoSomething
{
	@Override
	public void Do(CallParam param) 
	{
		//第一个参数是SN，第二个参数是送入的字符串
		SN sn= (SN)param.getParam();
		String sub= (String)param.getParam();
		synchronized (sn)
		{
			if(sn.SN_Model==null)
				sn.SN_Model=sub;
			else
				sn.SN_Model+=sub;
		}
	}
}
class SN_EndModel implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception 
	{
		//第一个参数是SN，第二个参数是送入的字符串
		SN sn= (SN)param.getParam();
		synchronized (sn)
		{
            String str = null;
            switch (sn.SN_Model)
            {
            	case "Purpose":
            		str = sn.Purpose;
            	case "Name":
            		str = param.getCaller().GetName();
                case "YYYY":
                    str = String.format("%04d", sn.SN_Time.get(Calendar.YEAR));
                    break;
                case "MM":
                    str = String.format("%02d", sn.SN_Time.get(Calendar.MONTH)+1);
                    break;
                case "M":
                    str = String.format("%d", sn.SN_Time.get(Calendar.MONTH)+1);
                    break;
                case "DD":
                    str = String.format("%02d", sn.SN_Time.get(Calendar.DAY_OF_MONTH));
                    break;
                case "D":
                    str = String.format("%d", sn.SN_Time.get(Calendar.DAY_OF_MONTH));
                    break;
                case "YYYYMMDD":
                    str = String.format("%04d%02d%02d", sn.SN_Time.get(Calendar.YEAR),sn.SN_Time.get(Calendar.MONTH)+1,sn.SN_Time.get(Calendar.DAY_OF_MONTH));
                    break;
                case "hh":
                    str = String.format("%02d", sn.SN_Time.get(Calendar.HOUR_OF_DAY));
                    break;
                case "h":
                    str = String.format("%d", sn.SN_Time.get(Calendar.HOUR_OF_DAY));
                    break;
                case "mm":
                    str = String.format("%02d", sn.SN_Time.get(Calendar.MINUTE));
                    break;
                case "m":
                    str = String.format("%d", sn.SN_Time.get(Calendar.MINUTE));
                    break;
                case "ss":
                    str = String.format("%02d", sn.SN_Time.get(Calendar.SECOND));
                    break;
                case "s":
                    str = String.format("%d", sn.SN_Time.get(Calendar.SECOND));
                    break;
                case "hhmmss":
                    str = String.format("%02d%02d%02d", sn.SN_Time.get(Calendar.HOUR_OF_DAY),sn.SN_Time.get(Calendar.MINUTE),sn.SN_Time.get(Calendar.SECOND));
                    break;
                default:
                    if (sn.SN_Model != null && sn.SN_Model.indexOf("SND") == 0)
                    {
                        //日流水；每天都从1开始
                        Integer len = 0;
                        if (sn.SN_Model.length() > 3)
                        {
                            len = Integer.parseInt(sn.SN_Model.substring(3));
                            if (len == 0)
                                throw new Exception("日流水以SND开头后跟所需要的日流水位数："+sn.SN_Model);
                        }
                        String f=null;
                        if (len > 0)
                            f="%0" + len.toString()+"d";
                        else
                        	f="%d";
                        int n=sn.GetSN_Day();
                        str = String.format(f, n);
                    }
                    else if (sn.SN_Model != null && sn.SN_Model.indexOf("SNT") == 0)
                    {
                        //总流水号；即用于该目的的流水从1开始始终增加
                    	Integer len = 0;
                        if (sn.SN_Model.length() > 3)
                        {
                            len = Integer.parseInt(sn.SN_Model.substring(3));
                            if (len == 0)
                                throw new Exception("总流水以SNT开头后跟所需要的流水位数："+sn.SN_Model);
                        }
                        String f=null;
                        if (len > 0)
                            f="%0" + len.toString()+"d";
                        else
                        	f="%d";
                        int n=sn.GetSN_Total();
                        str = String.format(f, n);
                    }
                    else
                        throw new Exception("尚不支持的格式串："+sn.SN_Model);
                    break;
            }
            sn.SN_RS += str;
		}
	}
}


