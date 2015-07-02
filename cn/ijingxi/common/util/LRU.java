  
package cn.ijingxi.common.util;   

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cn.ijingxi.common.orm.*;
   
/**  
 * 对象缓存
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
	private jxLink<UUID,jxORMobj> link=new jxLink<UUID,jxORMobj>();
	private Map<UUID,jxORMobj> tree=new HashMap<UUID,jxORMobj>();
	
	public void add(jxORMobj obj) throws Exception
	{		
		if(obj!=null)
			synchronized (this)
			{
				link.offer(obj.getID(),obj);
				tree.put(obj.getID(), obj);
				if(link.getCount()>MaxSize)
				{
					jxORMobj o=link.poll();
					tree.remove(o.getID());
				}
			}
	}
	public void delete(UUID id)
	{
		synchronized (this)
		{
			tree.remove(id);
			link.delete(id);
		}
	}
	public jxORMobj get(UUID id) throws Exception
	{
		synchronized (this)
		{
			jxORMobj obj=tree.get(id);
			if(obj!=null)
			{
				LinkNode<UUID, jxORMobj> node = link.searchNode(id);
				link.delete(node);
				link.offer(obj.getID(),obj);			
			}
			return obj;
		}
	}

}