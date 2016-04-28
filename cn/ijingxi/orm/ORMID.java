
package cn.ijingxi.orm;

import cn.ijingxi.util.Trans;

import java.util.UUID;

public class ORMID implements Comparable<ORMID>
{
	//public static final ORMID SystemID=new ORMID(0,(UUID)null);
	
	Integer TypeID;
	public Integer getTypeID(){return TypeID;}
	//public String getName(){return Name;}
	UUID ID;
	public UUID getID()
    {
        return ID;
    }
	@Override
	public int compareTo(ORMID o) 
	{
		if(o==null)return 1;
		if(TypeID<o.TypeID)return -1;
		if(TypeID>o.TypeID)return 1;
		return ID.compareTo(o.ID);
	}	
	public ORMID(Integer TypeID,UUID ID)
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
		js.AddValue("id", Trans.TransToString(ID));
		return js;
	}
	public static ORMID GetFromJSON(jxJson js) throws Exception
	{
		if(js==null)return null;
		int tyid=Trans.TransToInteger(js.getSubObjectValue("tyid"));
		UUID id=Trans.TransToUUID((String)js.getSubObjectValue("id"));
		return new ORMID(tyid,id);
	}
	
	
}