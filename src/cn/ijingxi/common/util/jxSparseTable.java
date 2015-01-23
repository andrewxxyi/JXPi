  
package cn.ijingxi.common.util;   
   
/**  
 * B+树  
 * @author Andrew Xu  
 */   
public class jxSparseTable<TKey1 extends Comparable<TKey1>, TKey2 extends Comparable<TKey2>, TValue>
{
	//当前状态
    private jxBTree<TKey1,jxBTree<TKey2,TValue>> m_List=new jxBTree<TKey1,jxBTree<TKey2,TValue>>();
    private int m_Count = 0;
	public int getCount()
    {
        return m_Count;
    }
    
    public void Add(TKey1 k1,TKey2 k2,TValue value)
    {
    	synchronized (this)
        {
	    	jxBTree<TKey2,TValue> tree=m_List.Search(k1);
	    	if(tree==null)
	    	{
	    		tree=new jxBTree<TKey2,TValue>();
	    		m_List.Insert(k1, tree);
	    	}
	    	if(!tree.Exist(k2))
	    	{
	    		tree.Insert(k2, value);
	    		m_Count++;    	
	    	}    
	    }
    }

    public void Delete(TKey1 k1,TKey2 k2)
    {
    	synchronized (this)
        {
	    	jxBTree<TKey2,TValue> tree=m_List.Search(k1);
	    	if(tree!=null&&tree.Exist(k2))
	    	{
	    		tree.Remove(k2);
	    		if(tree.getCount()==0)
	    			m_List.Remove(k1);
	    		m_Count--;
	    	}
	    }
    }
    
    public TValue Search(TKey1 k1,TKey2 k2)
    {
    	jxBTree<TKey2,TValue> tree=m_List.Search(k1);
    	if(tree!=null)
    		return tree.Search(k2);
    	return null;
    }
    
}