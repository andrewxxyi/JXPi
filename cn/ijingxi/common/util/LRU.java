  
package cn.ijingxi.common.util;   

import java.util.HashMap;
import java.util.Map;

import cn.ijingxi.common.orm.*;
   
/**  
 * B+树  
 * @author Andrew Xu  
 */   
public class LRU
{
	//默认存储1万个对象
	private int MaxSize=10000;
	public void setMaxSize(int size)
    {
		MaxSize=size;
    }
	private jxLink<ORMID,jxORMobj> link=new jxLink<ORMID,jxORMobj>();
	private Map<ORMID,jxORMobj> tree=new HashMap<ORMID,jxORMobj>();
	
	public void add(jxORMobj obj) throws Exception
	{		
		if(obj!=null)
			synchronized (this)
			{
				link.offer(obj.getORMID(),obj);
				tree.put(obj.getORMID(), obj);
				if(link.getCount()>MaxSize)
				{
					jxORMobj o=link.poll();
					tree.remove(o.getORMID());
				}
			}
	}
	public void delete(ORMID id)
	{
		synchronized (this)
		{
			tree.remove(id);
			link.delete(id);
		}
	}
	public jxORMobj get(ORMID id) throws Exception
	{
		synchronized (this)
		{
			jxORMobj obj=tree.get(id);
			if(obj!=null)
			{
				LinkNode<ORMID, jxORMobj> node = link.searchNode(id);
				link.delete(node);
				link.offer(obj.getORMID(),obj);			
			}
			return obj;
		}
	}

}