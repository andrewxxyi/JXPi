
package cn.ijingxi.orm;

import cn.ijingxi.util.Trans;
import cn.ijingxi.util.jxOP;
import cn.ijingxi.util.utils;

import java.util.ArrayList;


//设置结果
public class OPLink
{
	ArrayList<OP> opList=new ArrayList<OP>();

	public OPLink(){}
	public OPLink(jxJson js) throws Exception
	{
		if(js==null)throw new Exception("初始化字符串不能为空");
		for(jxJson j:js)
		{
			OP c=new OP(j);
			opList.add(c);
		}
	}
	
	public void AddOP(String Field,String Target,jxOP op,Object value) throws Exception
	{
		OP o=new OP(Field,Target,op,value);
		opList.add(o);
	}
	public Object Exec(jxORMobj obj) throws Exception
	{
		Object rs=null;
		for(OP op:opList)
		{
			Object o=op.Exec(obj);
			if(o!=null)
				rs=o;
		}
		return rs;
	}
	public jxJson TojxJson() throws Exception
	{
		jxJson js=jxJson.GetArrayNode("OPLink");
		for(OP op:opList)
			js.AddArrayElement(op.TojxJson());
		return js;
	}
	
	

	//是Field.Target的值 cp value
	class OP
	{
		String Field=null;
		String Target=null;
		jxOP op=jxOP.Equal;
		String valueType=null;
		Object value=null;
		
		OP(String Field,String Target,jxOP op,Object value) throws Exception
		{
			if(value==null)throw new Exception("待操作的值不能为空！！");
			this.Field=Field;
			this.Target=Target;
			this.op=op;
			this.valueType=utils.GetClassName(value.getClass());
			this.value=value;
		}	
		OP(jxJson js) throws Exception
		{
			if(js==null)throw new Exception("初始化字符串不能为空");
			Field=(String) js.GetSubValue("F");
			Target=(String) js.GetSubValue("T");
			op=(jxOP) Trans.TransTojxEunm(jxOP.class,js.GetSubValue("op"));
			valueType=(String) js.GetSubValue("vt");
			value=Trans.TransFromJSONToJava(valueType, js.GetSubValue("v"));
		}

		Object  Exec(jxORMobj obj) throws Exception
		{
			switch(op)
			{
			case Equal:
				if(Target==null||Target=="")
				{
					jxORMobj.setFiledValue(obj, Field, value);
					return null;					
				}
			}
			return null;
		}
		jxJson TojxJson() throws Exception
		{
			jxJson js=jxJson.GetObjectNode("Contion");
			js.AddValue("F", Field);
			js.AddValue("T", Target);
			js.AddValue("op", op.ordinal());
			js.AddValue("vt", valueType);
			js.AddValue("v", value);
			return js;
		}	
	}
	
	
}
