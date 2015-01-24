  
package cn.ijingxi.common.util;   

import java.util.*;

/**  
 * B+树  
 * @author Andrew Xu  
 */   
public class jxSparseTable<TKey1 extends Comparable<TKey1>, TKey2 extends Comparable<TKey2>, TValue>
{
	//当前状态
    private Map<TKey1,Map<TKey2,TValue>> m_List=new HashMap<TKey1,Map<TKey2,TValue>>();
    private int m_Count = 0;
	public int getCount()
    {
        return m_Count;
    }
    
    public void Add(TKey1 k1,TKey2 k2,TValue value)
    {
    	synchronized (this)
        {
    		Map<TKey2,TValue> tree=m_List.get(k1);
	    	if(tree==null)
	    	{
	    		tree=new HashMap<TKey2,TValue>();
	    		m_List.put(k1, tree);
	    	}
	    	if(!tree.containsKey(k2))
	    	{
	    		tree.put(k2, value);
	    		m_Count++;    	
	    	}    
	    }
    }

    public void Delete(TKey1 k1,TKey2 k2)
    {
    	synchronized (this)
        {
    		Map<TKey2,TValue> tree=m_List.get(k1);
	    	if(tree!=null&&tree.containsKey(k2))
	    	{
	    		tree.remove(k2);
	    		if(tree.size()==0)
	    			m_List.remove(k1);
	    		m_Count--;
	    	}
	    }
    }
    
    public TValue Search(TKey1 k1,TKey2 k2)
    {
    	Map<TKey2,TValue> tree=m_List.get(k1);
    	if(tree!=null)
    		return tree.get(k2);
    	return null;
    }
    
}