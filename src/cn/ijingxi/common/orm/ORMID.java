
package cn.ijingxi.common.orm;

public class ORMID implements Comparable<ORMID>
{
	String Name;
	public String getName()
    {
        return Name;
    }
	Integer ID;
	public Integer getID()
    {
        return ID;
    }
	@Override
	public int compareTo(ORMID o) 
	{
		if(o==null||o.Name==null||o.Name=="")
			return 1;
		if(Name==null||Name=="")
			return -1;
		int sn=Name.compareTo(o.Name);
		if(sn==0)
			return ID-o.ID;
		return sn;
	}	
	ORMID(String Name,Integer ID)
	{
		this.Name=Name;
		this.ID=ID;
	}
}