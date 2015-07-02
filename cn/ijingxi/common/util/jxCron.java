
package cn.ijingxi.common.util;

import java.util.*;

import cn.ijingxi.common.util.jxNumber.Carry;

public class jxCron
{
	private Calendar myCal=null;

	private int myYear=0;
	private jxNumber<Integer> myMonth=new jxNumber<Integer>();
	private jxNumber<Integer> myDay=new jxNumber<Integer>();
	private jxNumber<Integer> myHour=new jxNumber<Integer>();
	private jxNumber<Integer> myMinute=new jxNumber<Integer>();
	private Calendar myCal_Date=null;
	
	private jxNumber<Integer> myDayOfWeek=new jxNumber<Integer>();
	private Calendar myCal_Week=null;
	
	public Calendar GetNext() throws Exception
	{
		if(AddOne_Time())
		{
			if(myCal_Date.compareTo(myCal_Week)<=0)
				GetNext_Date();
			else
				GetNext_Week();
			if(myCal_Date.compareTo(myCal_Week)<=0)
				myCal=myCal_Date;
			else
				myCal=myCal_Week;
		}
		Calendar rs=(Calendar) myCal.clone();
		rs.set(Calendar.HOUR, myHour.getCurrentNumber());
		rs.set(Calendar.MINUTE, myMinute.getCurrentNumber());
		return rs;
	}

	private void GetNext_Week() throws Exception
	{
		int w=myDayOfWeek.getCurrentNumber();
		int n=0;
		myDayOfWeek.AddOne();
		if(myDayOfWeek.getCarry()==Carry.Positive)
			n=myDayOfWeek.getCurrentNumber()+7-w;
		else
			n=myDayOfWeek.getCurrentNumber()-w;
		myCal_Week.add(Calendar.DAY_OF_MONTH, n);
	}
	private void GetNext_Date() throws Exception
	{
		while(!AddOne_Date()){}
		myCal_Date=utils.GetCalendar(myYear,myMonth.getCurrentNumber(),myDay.getCurrentNumber());
	}
	private boolean AddOne_Date() throws Exception
	{
		myDay.AddOne();
		if(myDay.getCarry()==Carry.Positive)
		{
			myMonth.AddOne();
			if(myDay.getCarry()==Carry.Positive)
				myYear++;
		}
		return utils.CheckDate(myYear,myMonth.getCurrentNumber(),myDay.getCurrentNumber());
	}
	private boolean AddOne_Time() throws Exception
	{
		myMinute.AddOne();
		if(myMinute.getCarry()==Carry.Positive)
		{
			myHour.AddOne();
			if(myHour.getCarry()==Carry.Positive)
				return true;
		}
		return false;
	}
	
	private void setDate() throws Exception
	{
		Calendar cal=utils.GetDate();
		myYear=cal.get(Calendar.YEAR);
		Carry c=myMonth.setNumber(cal.get(Calendar.MONTH)+1);
		if(c==Carry.Negative)
		{
			//有借位
			myYear++;
			myDay.SetToMin();
			myHour.SetToMin();
			myMinute.SetToMin();			
		}
		else if(c==Carry.Positive)
		{
			myDay.SetToMax();
			myHour.SetToMax();
			myMinute.SetToMax();					
		}
		else 
		{
			c=myDay.setNumber(cal.get(Calendar.DAY_OF_MONTH));
			if(c==Carry.Negative)
			{
				AddOneMonth();
				myHour.SetToMin();
				myMinute.SetToMin();					
			}
			else if(c==Carry.Positive)
			{
				myHour.SetToMax();
				myMinute.SetToMax();					
			}
		}
		myCal_Date=utils.GetCalendar(myYear, myMonth.getCurrentNumber(), myDay.getCurrentNumber());
		
		int dn=cal.get(Calendar.DAY_OF_WEEK);
		if(dn==0)dn=7;
		c=myDayOfWeek.setNumber(dn);
		if(c==Carry.Negative)
		{
			cal.add(Calendar.DAY_OF_MONTH, myDayOfWeek.getCurrentNumber()+7-dn);
		}
		else if(c==Carry.Positive)
			cal.add(Calendar.DAY_OF_MONTH, dn-myDayOfWeek.getCurrentNumber());
		myCal_Week=cal;		

		c=myHour.setNumber(cal.get(Calendar.DAY_OF_MONTH));
		if(c==Carry.Negative)
		{
			AddOneDay();
			GetNext_Week();
			myMinute.SetToMin();				
		}
		else if(c==Carry.Positive)
		{
			myMinute.SetToMax();					
		}
		else
		{
			c=myMinute.setNumber(cal.get(Calendar.DAY_OF_MONTH));
			if(c==Carry.Negative)
			{
				myHour.AddOne();
				if(myHour.getCarry()==Carry.Positive)
				{
					AddOneDay();
					GetNext_Week();
				}
			}
		}

		if(myCal_Date.compareTo(myCal_Week)<=0)
			myCal=myCal_Date;
		else
			myCal=myCal_Week;
	}
	
	private void AddOneMonth() throws Exception
	{
		myMonth.AddOne();
		if(myMonth.getCarry()==Carry.Positive)
			myYear++;
	}
	private void AddOneDay() throws Exception
	{
		myDay.AddOne();
		if(myDay.getCarry()==Carry.Positive)
			AddOneMonth();
	}

	public void AppendMonth(int Month)
	{
		if(1<=Month&&Month<=12)
			myMonth.AppendNumber(Month);
		else
			for(int i=1;i<=12;i++)
				myMonth.AppendNumber(i);		
	}
	public void AppendDay(int Day)
	{
		if(1<=Day&&Day<=31)
			myDay.AppendNumber(Day);
		else
			for(int i=1;i<=31;i++)
				myDay.AppendNumber(i);		
	}
	public void AppendHour(int Hour)
	{
		if(0<=Hour&&Hour<=23)
			myHour.AppendNumber(Hour);
		else
			for(int i=0;i<=23;i++)
				myHour.AppendNumber(i);		
	}
	public void AppendMinute(int Minute)
	{
		if(0<=Minute&&Minute<=59)
			myMinute.AppendNumber(Minute);
		else
			for(int i=0;i<=59;i++)
				myMinute.AppendNumber(i);		
	}
	
	public void AppendDayOfWeek(int DayOfWeek)
	{
		if(1<=DayOfWeek&&DayOfWeek<=7)
			myDayOfWeek.AppendNumber(DayOfWeek);
		else
			for(int i=1;i<=7;i++)
				myDayOfWeek.AppendNumber(i);		
	}
	
	public jxCron(IDo toDo,Object param) throws Exception
	{
		setDate();
		Calendar cal=GetNext();
		jxTimer.DoAt(cal, toDo, param);
	}

}
