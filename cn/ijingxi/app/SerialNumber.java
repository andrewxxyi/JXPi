
package cn.ijingxi.app;

import cn.ijingxi.Process.IExecutor;
import cn.ijingxi.orm.*;
import cn.ijingxi.util.*;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * 流水号，如：
 * {Purpose}-Te{Name}-{YYYYMMDD}{SNT4}则产生为：
 * 报销单-Te徐晓轶-201512110123
 * SND：代表每天的流水
 * SNT：代表总的流水
 *  如果后跟数字，则代表位数
 * Purpose：代表用途
 * Name：代表调用者的名字
 *
 * a:f，代表访问obj的json格式的a字段中的f属性，a不能为array，而必须为对象
 * &f，代表访问obj的f属性
 *
 *
 *
 *
 */
public class SerialNumber extends ObjTag
{
    private enum SNState{None,Common,Model}
    private enum Event_InputChar{Common,Bracket_L,Bracket_R}

    private jxStateMachine.realSM sm=null;
    
	static jxStateMachine<SNState,Event_InputChar> SNSM=null;
    static
    {
        //同步调度，避免异步调度导致送入的参数乱序
        SNSM=new jxStateMachine<>(false);
        /*
        SNSM.setStateFunc(new IDo2() {
            @Override
            public void Do(Object param1, Object param2) throws Exception {
                CallParam param=(CallParam)param1;
                SNState state=(SNState)param2;
                SerialNumber sn = (SerialNumber) param.getParam("SN");
                sn.state=state;
            }
        });
        */
    	SNSM.AddTrans(SNState.None, Event_InputChar.Common, SNState.Common, new SN_InputCommon());
    	SNSM.AddTrans(SNState.None, Event_InputChar.Bracket_L, SNState.Model, new SN_StartModel());
    	SNSM.AddTrans(SNState.Common, Event_InputChar.Common, SNState.Common, new SN_InputCommon());
    	SNSM.AddTrans(SNState.Common, Event_InputChar.Bracket_L, SNState.Model, new SN_StartModel());
    	SNSM.AddTrans(SNState.Model, Event_InputChar.Common, SNState.Model, new SN_ModelInput());
    	SNSM.AddTrans(SNState.Model, Event_InputChar.Bracket_R, SNState.Common, new SN_EndModel());
    }

    //临时存储收集到的model字块
    String temp=null;
    String SN_RS = null;
    
    SNState state=SNState.None;

    public static SerialNumber New(String Purpose,String Model) throws Exception
    {
        SerialNumber sn=(SerialNumber)SerialNumber.Create(SerialNumber.class);
        sn.Category=Purpose;
        sn.Name=Model;

        sn.setSN_LastDate(utils.Now());
        return sn;
    }

    public static SerialNumber get(String Purpose) throws Exception {
        SelectSql s=new SelectSql();
        s.AddTable("SerialNumber");
        s.AddContion("SerialNumber", "Category", jxCompare.Equal, Purpose);
        return (SerialNumber) Get(SerialNumber.class,s);
    }
    int GetSN_Day() throws Exception
    {
        int num=getSN_DayNumber();
        //jxLog.logger.debug("num:"+num);
    	Calendar cal= utils.GetDate(utils.Now());
        //jxLog.logger.debug("cal:"+cal);
        Calendar ld=getSN_LastDate();
        //jxLog.logger.debug("ld:"+ld);
    	if(utils.checkCalendarDate(ld,cal))
    	{
            num=0;
            setSN_LastDate(cal);
    	}
        num++;
        //jxLog.logger.debug("num:"+num);
    	setSN_DayNumber(num);
    	return num;
    }
    int GetSN_Total() throws Exception
    {
        int num=getSN_TotalNumber();
        num++;
        setSN_TotalNumber(num);
    	return num;
    }

    /**
     *
     * @param db
     * @param caller
     * @param obj 可访问某对象的数据
     * @return
     * @throws Exception
     */
    public String next(DB db,IExecutor caller,jxORMobj obj) throws Exception
    {
    	SN_RS="";
        synchronized (this) {
            state = SNState.None;
            for (int i = 0; i < Name.length(); i++) {
                String sub = Name.substring(i, i + 1);
                Event_InputChar e = Event_InputChar.Common;
                if ("{".compareTo(sub)==0)
                    e = Event_InputChar.Bracket_L;
                else if ("}".compareTo(sub)==0)
                    e = Event_InputChar.Bracket_R;
                CallParam param = new CallParam(null, caller, null);
                param.addParam("SN", this);
                param.addParam("sub", sub);
                param.addParam("obj", obj);
                sm.happen(e, param);
            }
            Update(db);
            return SN_RS;
        }
    }

    public void setSN_Model(String model){Name=model;}
    public String getSN_Model(){return Name;}

    public void setSN_DayNumber(int num) throws Exception {
        setExtendValue("Info","DayNumber",num);
    }
    public int getSN_DayNumber() throws Exception {
        return Trans.TransToInteger(getExtendValue("Info","DayNumber"));
    }

    public void setSN_TotalNumber(int num) throws Exception {
        setExtendValue("Info","TotalNumber",num);
    }
    public int getSN_TotalNumber() throws Exception {
        return Trans.TransToInteger(getExtendValue("Info","TotalNumber"));
    }

    public void setSN_LastDate(Calendar d) throws Exception {
        Calendar c = utils.GetDate(d);
        setExtendValue("Info","LastDate",Trans.TransToString(c.getTime()));
    }
    public Calendar getSN_LastDate() throws Exception {
        Calendar st=Calendar.getInstance();
        st.setTime(Trans.TransToDate(getExtendValue("Info","LastDate")));
        return st;
    }


    public static ORMID GetORMID(UUID ID)
    {
        return new ORMID(ORMType.SerialNumber.ordinal(),ID);
    }
    public static void Init() throws Exception{
        InitClass(ORMType.SerialNumber.ordinal(), SerialNumber.class,"序列号");
    }
    @Override
    protected void Init_Create(DB db) throws Exception
    {
        super.Init_Create(db);
        TagID=getTagID("序列号");
        sm=SNSM.newRealSM(this,SNState.None);
    }

}
class SN_InputCommon implements IDo2<jxStateMachine.realSM,Object>
{
	@Override
	public void Do(jxStateMachine.realSM sm,Object param)
	{
		//第一个参数是SN，第二个参数是送入的字符串
		SerialNumber sn= (SerialNumber)sm.getStateObject();
		String sub= (String) ((CallParam)param).getParam("sub");
		synchronized (sn)
		{
			if(sn.SN_RS==null)
				sn.SN_RS=sub;
			else
				sn.SN_RS+=sub;
		}
	}
}
class SN_StartModel implements IDo2<jxStateMachine.realSM,Object>
{
	@Override
	public void Do(jxStateMachine.realSM sm,Object param)
    {
		//第一个参数是SN，第二个参数是送入的字符串
        SerialNumber sn= (SerialNumber)sm.getStateObject();
		synchronized (sn)
		{
			sn.temp=null;
		}
	}
}
class SN_ModelInput implements IDo2<jxStateMachine.realSM,Object>
{
	@Override
	public void Do(jxStateMachine.realSM sm,Object param)
    {
		//第一个参数是SN，第二个参数是送入的字符串
        SerialNumber sn= (SerialNumber)sm.getStateObject();
        String sub= (String) ((CallParam)param).getParam("sub");
		synchronized (sn)
		{
			if(sn.temp==null)
				sn.temp=sub;
			else
				sn.temp+=sub;
		}
	}
}
class SN_EndModel implements IDo2<jxStateMachine.realSM,Object> {
    @Override
    public void Do(jxStateMachine.realSM sm, Object param) {
        try {
            //第一个参数是SN，第二个参数是送入的字符串
            SerialNumber sn = (SerialNumber) sm.getStateObject();
            if (sn.temp == null) return;
            synchronized (sn) {
                String str = "";
                Calendar st = Calendar.getInstance();
                st.setTime(new Date());
                switch (sn.temp) {
                    case "Purpose":
                        str = sn.Category;
                        break;
                    case "Name":
                        IExecutor caller = ((CallParam) param).Caller;
                        if (caller != null)
                            str = caller.getName();
                        else
                            str = "无名氏";
                        break;
                    case "YYYY":
                        str = String.format("%04d", st.get(Calendar.YEAR));
                        break;
                    case "MM":
                        str = String.format("%02d", st.get(Calendar.MONTH) + 1);
                        break;
                    case "M":
                        str = String.format("%d", st.get(Calendar.MONTH) + 1);
                        break;
                    case "DD":
                        str = String.format("%02d", st.get(Calendar.DAY_OF_MONTH));
                        break;
                    case "D":
                        str = String.format("%d", st.get(Calendar.DAY_OF_MONTH));
                        break;
                    case "YYYYMMDD":
                        str = String.format("%04d%02d%02d", st.get(Calendar.YEAR),
                                st.get(Calendar.MONTH) + 1, st.get(Calendar.DAY_OF_MONTH));
                        break;
                    case "hh":
                        str = String.format("%02d", st.get(Calendar.HOUR_OF_DAY));
                        break;
                    case "h":
                        str = String.format("%d", st.get(Calendar.HOUR_OF_DAY));
                        break;
                    case "mm":
                        str = String.format("%02d", st.get(Calendar.MINUTE));
                        break;
                    case "m":
                        str = String.format("%d", st.get(Calendar.MINUTE));
                        break;
                    case "ss":
                        str = String.format("%02d", st.get(Calendar.SECOND));
                        break;
                    case "s":
                        str = String.format("%d", st.get(Calendar.SECOND));
                        break;
                    case "hhmmss":
                        str = String.format("%02d%02d%02d", st.get(Calendar.HOUR_OF_DAY),
                                st.get(Calendar.MINUTE), st.get(Calendar.SECOND));
                        break;
                    default:
                        if (sn.temp.indexOf("SND") == 0) {
                            //日流水；每天都从1开始
                            Integer len = 0;
                            if (sn.temp.length() > 3) {
                                len = Integer.parseInt(sn.temp.substring(3));
                                if (len == 0)
                                    throw new Exception("日流水以SND开头后跟所需要的日流水位数：" + sn.temp);
                            }
                            String f = null;
                            if (len > 0)
                                f = "%0" + len.toString() + "d";
                            else
                                f = "%d";
                            int n = sn.GetSN_Day();
                            str = String.format(f, n);
                        } else if (sn.temp.indexOf("SNT") == 0) {
                            //总流水号；即用于该目的的流水从1开始始终增加
                            Integer len = 0;
                            if (sn.temp.length() > 3) {
                                len = Integer.parseInt(sn.temp.substring(3));
                                if (len == 0)
                                    throw new Exception("总流水以SNT开头后跟所需要的流水位数：" + sn.temp);
                            }
                            String f = null;
                            if (len > 0)
                                f = "%0" + len.toString() + "d";
                            else
                                f = "%d";
                            int n = sn.GetSN_Total();
                            str = String.format(f, n);
                        } else {
                            jxORMobj obj = (jxORMobj) ((CallParam) param).obj;
                            String[] ss = sn.temp.split(":");
                            if (ss.length == 2) {
                                str = obj.getExtendValue(ss[0], ss[1]);
                            } else {
                                ss = sn.temp.split("&");
                                if (ss.length == 2) {
                                    Object o = obj.getFiledValue(obj, ss[1]);
                                    if (o != null)
                                        str = o.toString();
                                } else
                                    throw new Exception("尚不支持的格式串：" + sn.temp);
                            }
                        }
                        break;
                }
                sn.SN_RS += str;
            }
        } catch (Exception e) {
            jxLog.error(e);
        }

    }


}


