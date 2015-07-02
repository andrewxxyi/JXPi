
package cn.ijingxi.common.orm;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.ijingxi.common.util.LinkNode;
import cn.ijingxi.common.util.Trans;
import cn.ijingxi.common.util.jxLink;
import cn.ijingxi.common.util.utils;

public class jxJson implements Iterable<jxJson>
{
    //==============================================================================
    //常量
    //==============================================================================
    public enum NodeType{Undefined,Array,Object,Original}
    
    //==============================================================================
    //成员变量
    //==============================================================================
    //空节点
    static jxJson NullJsonNode = new jxJson();	
    
    private NodeType Type=NodeType.Undefined;
    
    private String Name=null;
    
    //如果父节点为数组，则保存本对象在父节点中的索引
    private int ArrayIndex=0;
    /// <summary>
    /// 如果节点为数组，则保存各子元素
    /// </summary>
    ArrayList< jxJson> Array = null;
	public ArrayList< jxJson> SubEl()
    {
        return Array;
    }
    /// <summary>
    /// 如果节点为对象，则保存各子对象
    /// </summary>
    private  jxLink<String,jxJson> SubObjectList = null;
	public jxLink<String,jxJson> SubObjects()
    {
        return SubObjectList;
    }
    /// <summary>
    /// 如果节点为原始值，则为值
    /// </summary>
    private Object Value = null;
    //
    //
    //将JSON字符串转换成Json对象
    //
    //
    /// {0}是开头字符，{1}是结束字符；是匹配{0}{1}成对出现的字符串
    //以%1$s开头跟随任意多的非这两个字符，然后两者必须同时出现，最后以%2$s结尾，
    private static String regTxt = "(%1$s[^%1$s%2$s]*((%1$s[^%1$s%2$s]*)(%2$s[^%1$s%2$s]*))*%2$s)";
    
    /// 键以及值，判断是否包含单,双引号
    //.对应浮点数，+-对应数值以及指数，空白符、:和-对应日期转成的字符串
    private static String regKeyValue = "(%1$s([\\u4E00-\\u9FA5]|[\\w\\.\\+\\-\\s:]|[\\uFE30-\\uFFA0])%2$s%1$s)";
    
    /// 匹配元数据(不包含对象,数组)
    //.对应浮点数，+-对应数值以及指数，引号和单引号采用*是因为有可能是空值
    //"([\\u4E00-\\u9FA5]|[\\w\\.\\+\\-\\s:]|[\\uFE30-\\uFFA0])+"分别是中文字符、英文字符等、中文标点符号
    private static String regOriginalValue = String.format("(%1$s|%2$s|%3$s)", String.format(regKeyValue, "'", "*"), 
    		String.format(regKeyValue, "\"", "*"), "([\\u4E00-\\u9FA5]|[\\w\\.\\+\\-\\s:]|[\\uFE30-\\uFFA0])+");

    /// 匹配value以及对象、数组
    private static String regValue = String.format("(%1$s|%2$s|%3$s)", regOriginalValue,
    		String.format(regTxt, "\\[", "\\]"), String.format(regTxt, "\\{", "\\}"));

    /// 匹配键值对
    //注意：key是group（1），因为%1$s、%2$s、%3$s都带园括号，所以value是group（7）
    private static String regKeyValuePair = String.format("\\s*(%1$s|%2$s|%3$s)\\s*:\\s*(%4$s)\\s*",
    		String.format(regKeyValue, "'", "+"), String.format(regKeyValue, "\"", "+"), "(\\w+)", regValue);

    /// 判断是否是对象
    //即用花括号括起来的键值对组
    private static Pattern RegJsonStrack1 = Pattern.compile(String.format("^%1$s(%3$s(,(%3$s))*)%2$s", "\\{", "\\}", regKeyValuePair));

    /// 判断是否是数组
    //即用方括号括起来的值组
    private static Pattern RegJsonStrack2 = Pattern.compile(String.format("^\\[((%1$s)(,(%1$s))*)\\]", regValue));

    /// 判断是否是键值对
    private static Pattern RegJsonStrack3 = Pattern.compile(regKeyValuePair);

    /// 判断是否是value
    private static Pattern RegJsonStrack4 = Pattern.compile(regValue);

    /// 判断是否是元数据
    private static Pattern RegJsonStrack6 = Pattern.compile(String.format("^%1$s", regOriginalValue));

    /// 移除两端[]、{}、引号及单引号
    private static String RegJsonRemoveBlank = "(^\\s*[\\[\\{'\"]\\s*)|(\\s*[\\]\\}'\"]\\s*$)";

    //==============================================================================
    //构造函数
    //==============================================================================
    jxJson(){}

    //==============================================================================
    //属性
    //==============================================================================
	public NodeType getType()
    {
        return Type;
    }
	public String getName()
    {
        return Name;
    }
	public boolean IsNull() { return Type == NodeType.Undefined; }
    
	public Object getValue()
	{
		if(Type == NodeType.Original)
                return Value;
		return null;
	}
	public void setValue(Object value)
	{
		if (Type == NodeType.Original)
			Value = value;
	}

    //==============================================================================
    //方法
    //==============================================================================
	/**
	 * 如果是对象，则获取子节点SubName的值
	 * @param SubName
	 * @return
	 * @throws Exception 
	 */
	public Object GetSubValue(String SubName) throws Exception
	{
		utils.Check(SubName==null, "需要给出子对象的名字");
		utils.Check(Type!=NodeType.Object, "只有对象才可能直接读取子节点的值");
		if(SubObjectList==null)
			return null;
		jxJson node=SubObjectList.search(SubName);
		if(node!=null&&node.Type==NodeType.Original)
			return node.Value;
		return null;
	}
	public jxJson GetSubObject(String SubName) throws Exception
	{
		utils.Check(SubName==null, "需要给出子对象的名字");
		utils.Check(Type!=NodeType.Object, "只有对象才能读取子对象");
		if(SubObjectList==null)
			return null;
		jxJson node=SubObjectList.search(SubName);
		if(node!=null)
			return node;
		return null;
	}
	public ArrayList<jxJson> GetSubEL() throws Exception
	{
		utils.Check(Type!=NodeType.Array, "只有数组才能读取子元素");
		return Array;
	}
	
	public static jxJson GetObjectNode(String Name)
	{
		jxJson j=new jxJson();
		j.Name=Name;
		j.Type=NodeType.Object;
		j.SubObjectList=new jxLink<String,jxJson>();
		return j;
	}
	public static jxJson GetArrayNode(String Name)
	{
		jxJson j=new jxJson();
		j.Name=Name;
		j.Type=NodeType.Array;
		j.Array=new ArrayList<jxJson>();
		return j;
	}
	public static jxJson GetValueNode(String Name,Object value)
	{
		jxJson j=new jxJson();
		j.Name=Name;
		j.Type=NodeType.Original;
		j.Value=value;
		return j;
	}

	public  void AddValue(String Name,Object value) throws Exception
	{
		jxJson j=new jxJson();
		j.Name=Name;
		j.Type=NodeType.Original;
		j.Value=value;
		if(Type==NodeType.Array)
			AddArrayElement(j);
		else if(Type==NodeType.Object)
			AddSubObjNode(j);
		else
			throw new Exception("只有数组或对象节点才能添加值元素");
	}
	public  Object getSubObjectValue(String SubName) throws Exception
	{
		jxJson js=GetSubObject(SubName);
		if(js!=null)
			return js.getValue();
		return null;		
	}
	public void setSubObjectValue(String SubName,Object value) throws Exception 
	{
		jxJson sub=GetSubObject(SubName);
		if(sub!=null)
		{
			sub.Value=value;
			return;
		}
		AddValue(SubName,value);
	}
    /**
     * 作为对象添加子对象
     * @param sub
     * @throws Exception 
     */
	public void AddSubObjNode(jxJson sub) throws Exception
    {
    	utils.Check(sub==null, "不能添加空的子对象");
    	utils.Check(Type!=NodeType.Object, "只有对象节点才能添加子对象");
    	SubObjectList.addByRise(sub.Name, sub);
    }
	/**
	 * 作为数组添加子元素
	 * @param sub
	 * @throws Exception 
	 */
    public void AddArrayElement(jxJson el) throws Exception
    {
    	utils.Check(el==null, "不能添加空的子元素");
    	utils.Check(Type!=NodeType.Array, "只有数组节点才能添加子元素");
		synchronized (Array)
        {
	    	el.ArrayIndex=Array.size();
	    	Array.add(el);    	
        }
    }
    public void RemoveArrayElement(jxJson el) throws Exception
    {
		synchronized (Array)
        {
	    	Array.remove(el.ArrayIndex);    	
        }
    }

    public String TransToStringWithName()
    {
        StringBuilder jxJson = new StringBuilder();
        jxJson.append("{\""+Name+"\":");
        jxJson.append(TransToString());
        return jxJson.toString() + "}";
    }


    public String TransToString()
    {
        StringBuilder jxJson = new StringBuilder();
        if(Type==NodeType.Object)
            {
                jxJson.append("{");
                int i=1;
                if (SubObjectList != null)
                	for(LinkNode<String,jxJson> n :SubObjectList)
                	{
                		jxJson.append("\""+n.getKey()+"\":");
                		String s=n.getValue().TransToString();
                        jxJson.append(s);
                        if(i<SubObjectList.getCount())
                        	jxJson.append(",");       
                        i++;
                	}
                return jxJson.toString() + "}";
            }
        else if(Type==NodeType.Array)
        {
                jxJson.append("[");
                int i=1;
                if (Array != null)
                	for(jxJson n :Array)
                	{
                		String s=n.TransToString();
                        jxJson.append(s);
                        if(i<Array.size())
                        	jxJson.append(",");            
                        i++;    		
                	}
                return jxJson.toString() + "]";
        }
        else if(Type==NodeType.Original)
        {
            jxJson.append(TransValueToJSONString(Value));
            return jxJson.toString();
        }
        return null;
    }

    public static NodeType MeasureType(String jxJson)
    {    	
        if (RegJsonStrack1.matcher(jxJson).matches())
            return NodeType.Object;
        if (RegJsonStrack2.matcher(jxJson).matches())
            return NodeType.Array;
        if (RegJsonStrack6.matcher(jxJson).matches())
            return NodeType.Original;
        return NodeType.Undefined;
    }

        /// <summary>
        /// 将从前端接收到的json字符串转换成JsonNode
        /// </summary>
        /// <param name="jxJson">用$.jxPostJSON传送进来的json对象会被转换成json字符串</param>
        /// <returns></returns>
        public static jxJson JsonToObject(String jxJson)
        {
            if (jxJson == null) return null;
            //utils.P("JsonToObject", jxJson);
            jxJson = jxJson.trim();
            jxJson = jxJson.replaceAll("[\r\n]", "");
            NodeType nodetype = MeasureType(jxJson);
            //utils.P("nodetype", nodetype.toString());
            if (nodetype == NodeType.Undefined)
                return NullJsonNode;
            jxJson newNode = new jxJson();
            newNode.Type = nodetype;
        	jxJson = jxJson.replaceAll(RegJsonRemoveBlank, "");
            if (nodetype == NodeType.Array)
            {
                Matcher m = RegJsonStrack4.matcher(jxJson);
                newNode.Array = new ArrayList<jxJson>();
                while(m.find())
                {
                	String v=m.group(0);
                	//utils.P("value:"+v);
                	newNode.Array.add(JsonToObject(v));
                }
            }
            else if (nodetype == NodeType.Object)
            {
            	Matcher m = RegJsonStrack3.matcher(jxJson);                
                newNode.SubObjectList = new jxLink<String, jxJson>();
                while(m.find())
                {                
                	//PrintMatcheroup(m);
                	String key = m.group(1).replaceAll(RegJsonRemoveBlank, "");
                	//utils.P("key",key);
                	//从左到右去数该模式是所在的第几个“(”
                	//for(int i=0;i<m.groupCount();i++)
                    //	utils.P("group "+i,m.group(i));
                	String v=m.group(7);
                	//utils.P("value",v);
                    jxJson subnode = JsonToObject(v);
                    subnode.Name = key;
                    newNode.SubObjectList.addByRise(key, subnode);                
                }
            }
            else if (nodetype == NodeType.Original)
                newNode.Value = jxJson.replaceAll("\\r\\n", "\r\n");
            return newNode;
        }

        // 过滤特殊字符  
        static String String2Json(String s)
        {
            if (s == null) return null;
            StringBuilder sb = new StringBuilder();
            char[] ca=s.toCharArray();
            for(char c:ca)
                switch (c)
                {
                    case '\"':
                        sb.append("\\\"");
                        break;
                    case '\\':
                        sb.append("\\\\"); 
                        break;
                    case '/':
                        sb.append("\\/"); 
                        break;
                    case '\b':
                        sb.append("\\b"); 
                        break;
                    case '\f':
                        sb.append("\\f"); 
                        break;
                    case '\n':
                        sb.append("\\n"); 
                        break;
                    case '\r':
                        sb.append("\\r"); 
                        break;
                    case '\t':
                        sb.append("\\t"); 
                        break;
                    case '\0':
                        sb.append("\\0"); 
                        break;
                    default:
                        sb.append(c); 
                        break;
                }
            return sb.toString();
        }
        static String TransValueToJSONString(Object obj)
        {
            if (obj == null) return "\"\"";
            String str=obj.toString();
            String tn=utils.GetClassName(obj.getClass());
            switch(tn)
        	{
        	case "String":
                str = String2Json(str);
                return "\"" + str + "\"";
        	case "UUID":
                str = Trans.TransToString((UUID) obj);
                return "\"" + str + "\"";
        	case "Date":
        		return "\"" + Trans.TransToInteger((Date)obj) + "\"";
        	case "boolean":
        	case "Boolean":
        		return str.toLowerCase();
        	default:
        		return str;        		
        	}
        }

        static void PrintMatcheroup(Matcher m)
        {
        	int num=m.groupCount();
        	for(int i=1;i<=num;i++)
        		utils.P("Json group",String.format("Group(%1$s):%2$s",i, m.group(i)));	
        }
        /** 
         * 实现Iterable接口中要求实现的方法 
         */  
        @Override  
        public Iterator<jxJson> iterator() 
        {  
            return new MyIterator();//返回一个MyIterator实例对象  
        }        
        /** 
         * MyIterator是内部类，实现了Iterator<E>接口的类 
         */  
        class MyIterator implements Iterator<jxJson>
        {
        	private NodeType myType=Type;
        	private int arrIndex=0;
        	private LinkNode<String, jxJson> objSubNode=null;

        	MyIterator()
        	{
        		if(SubObjectList!=null)
        			objSubNode=SubObjectList.getFirst();
        	}
        	
            @Override  
            public boolean hasNext()
            {  
            	if(myType==NodeType.Array)
            		return arrIndex<Array.size();
            	else if(myType==NodeType.Object)
            		return objSubNode!=null;
            	return false;
            }
            @Override  
            public jxJson next() 
            {  
            	if(myType==NodeType.Array)
            		return Array.get(arrIndex++);
            	else if(myType==NodeType.Object)
            	{
            		jxJson j=objSubNode.getValue();
            		objSubNode=objSubNode.getNext();
            		return j;
            	}
				return null;
            } 
            @Override  
            public void remove() {  
                //未实现这个方法  
            }
        }
}
