
package cn.ijingxi.common.util;

public class LinkNode<TKey, TValue>
{
	private TKey Key;
	public TKey getKey()
    {
        return Key;
    }	
	TValue Value;
	public TValue getValue()
    {
        return Value;
    }	
    /// 兄弟节点，按从小到大的顺序
	LinkNode<TKey, TValue> Next = null;
	public LinkNode<TKey, TValue> getNext()
    {
        return Next;
    }	
	LinkNode<TKey, TValue> Prev = null;
	public LinkNode<TKey, TValue> getPrev()
    {
        return Prev;
    }	
	
	public LinkNode(TKey Key,TValue Value)
	{
		this.Key=Key;
		this.Value=Value;
	}
}