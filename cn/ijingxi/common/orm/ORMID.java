
package cn.ijingxi.common.orm;

public class ORMID implements Comparable<ORMID>
{
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
}