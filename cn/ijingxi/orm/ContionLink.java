
package cn.ijingxi.orm;

import cn.ijingxi.util.Trans;
import cn.ijingxi.util.jxCompare;

import java.util.ArrayList;


//与连接的条件
public class ContionLink
{
	ArrayList<Contion> conList=new ArrayList<Contion>();

	public ContionLink(){}
	public ContionLink(jxJson js) throws Exception
	{
		if(js==null)throw new Exception("初始化字符串不能为空");
		for(jxJson j:js)
		{
			Contion c=new Contion(j);
			conList.add(c);
		}
	}
	
	public void AddContion(String Field,String Target,jxCompare cp,Float value) throws Exception
	{
		Contion c=new Contion(Field,Target,cp,value);
		conList.add(c);
	}
	public boolean  Judge(jxORMobj obj) throws Exception
	{
		for(Contion c:conList)
			if(!c.Judge(obj))
				return false;
		return true;
	}
	public jxJson TojxJson() throws Exception
	{
		jxJson js=jxJson.GetArrayNode("ContionLink");
		for(Contion c:conList)
			js.AddArrayElement(c.TojxJson());
		return js;
	}
	
	

	//是Field.Target的值 cp value
	class Contion
	{
		String Field=null;
		String Target=null;
		jxCompare cp=jxCompare.Equal;
		Float value=null;
		
		Contion(String Field,String Target,jxCompare cp,Float value) throws Exception
		{
			if(value==null)throw new Exception("待比较对象的值不能为空！！");
			this.Field=Field;
			this.Target=Target;
			this.cp=cp;
			this.value=value;
		}	
		Contion(jxJson js) throws Exception
		{
			if(js==null)throw new Exception("初始化字符串不能为空");
			Field=(String) js.GetSubValue("F");
			Target=(String) js.GetSubValue("T");
			cp=(jxCompare) Trans.TransTojxEunm(jxCompare.class,js.GetSubValue("cp"));
			value=Trans.TransToFloat(js.GetSubValue("v"));
		}

		boolean  Judge(jxORMobj obj) throws Exception
		{
			Float ov=Trans.TransToFloat(obj.getExtendValue(Field, Target));
			switch(cp)
			{
			case Equal:
				return ov==value;
			case NoEqual:
				return ov!=value;
			case Less:
				return ov<value;
			case LessEqual:
				return ov<=value;
			case Greate:
				return ov>value;
			case GreateEqual:
				return ov>=value;
			}
			return false;
		}
		jxJson TojxJson() throws Exception
		{
			jxJson js=jxJson.GetObjectNode("Contion");
			js.AddValue("F", Field);
			js.AddValue("T", Target);
			js.AddValue("cp", cp.ordinal());
			js.AddValue("v", value);
			return js;
		}	
	}
	
}
