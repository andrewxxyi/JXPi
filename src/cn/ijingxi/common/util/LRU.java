  
package cn.ijingxi.common.util;   

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
	private jxBTree<ORMID,jxORMobj> tree=new jxBTree<ORMID,jxORMobj>();
	
	public void add(jxORMobj obj)
	{		
		if(obj!=null)
			synchronized (this)
			{
				link.offer(obj.getORMID(),obj);
				tree.Insert(obj.getORMID(), obj);
				if(link.getCount()>MaxSize)
				{
					jxORMobj o=link.poll();
					tree.Remove(o.getORMID());
				}
			}
	}
	
	public jxORMobj get(ORMID id)
	{
		synchronized (this)
		{
			jxORMobj obj=tree.Search(id);
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