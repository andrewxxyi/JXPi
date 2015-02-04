
package cn.ijingxi.common.orm;

import cn.ijingxi.common.util.Trans;

public class ORMID implements Comparable<ORMID>
{
	public static final ORMID SystemID=new ORMID(0,0);
	
	Integer TypeID;
	public Integer getTypeID(){return TypeID;}
	//public String getName(){return Name;}
	Integer ID;
	public Integer getID()
    {
        return ID;
    }
	@Override
	public int compareTo(ORMID o) 
	{
		if(o==null)return 1;
		if(TypeID<o.TypeID)return -1;
		if(TypeID>o.TypeID)return 1;
		return ID-o.ID;
	}	
	public ORMID(Integer TypeID,Integer ID)
	{
		this.TypeID=TypeID;
		this.ID=ID;
	}

	public boolean Equal(ORMID id)
	{
		if(id!=null)
			return this.compareTo(id)==0;
		return false;
	}
	

	public jxJson ToJSON(String Name) throws Exception
	{
		jxJson js=jxJson.GetObjectNode(Name!=null?Name:"ORMID");
		js.AddValue("tyid", TypeID);
		js.AddValue("id", ID);
		return js;
	}
	public static ORMID GetFromJSON(jxJson js) throws Exception
	{
		if(js==null)return null;
		int tyid=Trans.TransToInteger(js.getSubObjectValue("tyid"));
		int id=Trans.TransToInteger(js.getSubObjectValue("id"));
		return new ORMID(tyid,id);
	}
	
	
}