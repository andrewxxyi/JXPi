
package cn.ijingxi.util;

public class jxNumber<TValue extends Comparable<TValue>>
{
	public enum Carry{None,Positive,Negative}
	private jxLink<Integer,TValue> Numbers=new jxLink<Integer,TValue>();
	private Carry myCarry=Carry.None;
	private Integer Index=0;
	public Carry getCarry()
	{
		Carry c=myCarry;
		myCarry=Carry.None;
		return c;
	}
	
	public void SetToMin(){Index=0;}
	public void SetToMax(){Index=Numbers.getCount()-1;}
	
	public Carry setNumber(TValue value)
	{
		for(LinkNode<Integer,TValue> node:Numbers)
			if(node.getValue().compareTo(value)==0)
			{
				//有相等才返回无进位
				Index=node.getKey();
				myCarry=Carry.None;
				return Carry.None;
			}
		LinkNode<Integer,TValue> n=Numbers.getFirst();
		if(n.getValue().compareTo(value)>0)
		{
			Index=0;
			return Carry.Positive;
		}
		n=Numbers.getLast();
		if(n.getValue().compareTo(value)<0)
		{
			Index=0;
			//最大的数还是小于value，所以需要借位
			return Carry.Negative;
		}
		for(LinkNode<Integer,TValue> node:Numbers)
			if(node.getValue().compareTo(value)>0)
			{
				Index=node.getKey()-1;
				return Carry.Positive;
			}
		return Carry.Positive;
	}
	public TValue getCurrentNumber()
	{
		return Numbers.search(Index);
	}
	public void AppendNumber(TValue value)
	{
		Numbers.addByRise(Numbers.getCount(),value);
	}
	public void AddOne() throws Exception
	{
		if(Numbers.getCount()==0)
			throw new Exception("尚未添加数值");
		if(Index<Numbers.getCount()-1)
			Index++;
		else
		{
			Index=0;
			myCarry=Carry.Positive;
		}
	}
	public void DelOne() throws Exception
	{
		if(Numbers.getCount()==0)
			throw new Exception("尚未添加数值");
		if(Index==0)
		{
			Index=Numbers.getCount()-1;
			myCarry=Carry.Negative;			
		}
		else
			Index--;
	}
	
}