   
package cn.ijingxi.common.util;   

import java.util.*; 
   
/**  
 * B+树  
 * @author Andrew Xu  
 */   
public class jxBTree<TKey extends Comparable<TKey>, TValue> implements Iterable<BTreeNode<TKey, TValue>>
{
	//每节点可存储的子节点数
    private int m_Order = 3;
    private int m_HalfOrder = 2;

    private BTreeNode<TKey, TValue> m_Tree = null;
    //总节点数
    private int m_Count = 0;
	public int getCount()
    {
        return m_Count;
    }
	
	public TKey MinKey()
    {
		if(m_Tree!=null)
		{
			BTreeNode<TKey, TValue> node=GetMinNode(m_Tree);
			return node.Key;			
		}
        return null;
    }
	public TKey MaxKey()
    {
		if(m_Tree!=null)
		{
			BTreeNode<TKey, TValue> node=GetMaxNode(m_Tree);
			return node.Key;			
		}
        return null;
    }
       
	/**
	 * 查找key所在节点，如果key不在树中，则也返回可能插入的父节点
	 * @param Key
	 * @return 返回两个值：父节点，和可能存在的节点
	 */
    private ArrayList<BTreeNode<TKey, TValue>> Search_Node(TKey Key)
    {
    	BTreeNode<TKey, TValue> node = m_Tree,pn=null;
        synchronized (this)
        {
            while (node != null)
            {
               	pn=node;
               	node = pn.SearchNode(Key);
               	//叶节点
               	if(node!=null && node.SonNodeList==null)
               	{
               		if(node.Key.compareTo(Key)!=0)
               			//没找到
               			node=null;
               		break;
               	}
            }
            ArrayList<BTreeNode<TKey, TValue>> rs=new ArrayList<BTreeNode<TKey, TValue>>();
            rs.add(pn);
            rs.add(node);
            return rs;
        }
  }

    public boolean Exist(TKey Key)
    {
    	ArrayList<BTreeNode<TKey, TValue>> rs = Search_Node(Key);
    	BTreeNode<TKey, TValue> node=rs.get(1);
    	return node!=null;
    }
    public TValue Search(TKey Key)
    {
    	ArrayList<BTreeNode<TKey, TValue>> rs = Search_Node(Key);
    	BTreeNode<TKey, TValue> node=rs.get(1);
    	if(node!=null)
    		return node.Value;
    	return null;
    }
	/**
	 * 向树中进行插入
	 * @param Key
	 * @param Value
	 */
    public void Insert(TKey Key, TValue Value)
    {
        synchronized (this)
        {
            if (m_Tree == null)
            {
                m_Tree = new BTreeNode<TKey, TValue>();
                //m_Tree.m_Type = NodeType.Leaf;
                m_Tree.AddLeaf(Key, Value);
                m_Count++;
            }
            else
            {
                ArrayList<BTreeNode<TKey, TValue>> rs = Search_Node(Key);
                BTreeNode<TKey, TValue> pn=rs.get(0);
                BTreeNode<TKey, TValue> node=rs.get(1);
                if (node != null)
                	//已在树中，则更新其值
                    node.Value=Value;
                else
                {
                    pn.AddLeaf(Key, Value);
                    BTreeNode<TKey, TValue> parent = null;
                    while (pn.NodeNum > m_Order)
                    {
                    	BTreeNode<TKey, TValue> nn = pn.Split(m_HalfOrder);
                        parent = pn.Parent;
                        if (parent == null)
                        {
                            m_Tree = new BTreeNode<TKey, TValue>();
                            m_Tree.AddNode(pn);
                            m_Tree.AddNode(nn);
                            break;
                        }
                        else
                            parent.AddNode(nn);
                        pn = parent;
                    }
                    m_Count++;
                }
            }
        }
    }

    /**
     * 从树中删除
     * @param Key
     */
    public void Remove(TKey Key)
    {
    	synchronized (this)
        {
            if (m_Tree == null) return;
            ArrayList<BTreeNode<TKey, TValue>> rs = Search_Node(Key);
            BTreeNode<TKey, TValue> pn=rs.get(0);
            BTreeNode<TKey, TValue> node=rs.get(1);
            if (node != null)
            {
                if (m_Count == 1)
                    m_Tree = null;
                else
                    if (pn.NodeNum == 1)
                    {
                        BTreeNode<TKey, TValue> parent = pn.Parent;
                        //因为m_Count不等于一所以此时pn不可能是m_Tree，则其Parent不可能为null
                        parent.RemoveNode(pn);
                        //pn.DeleteNode(Key);
                        JoinNode(parent);
                    }
                    else
                    {
                        pn.RemoveNode(node);
                    }
                m_Count--;
            }
        }
    }

    private void JoinNode(BTreeNode<TKey, TValue> Branch)
    {
        if (Branch == null || Branch == m_Tree) return;
        if (Branch.NodeNum < m_HalfOrder)
        {
        	BTreeNode<TKey, TValue> parent = Branch.Parent;
        	BTreeNode<TKey, TValue> nn = Branch.Brother_Prev;
            if (nn != null)
            {
                if (nn.NodeNum > m_HalfOrder)
                {
                    //只需要将两节点中的混合一下即可
                    nn.MoveNodeTo(Branch, m_HalfOrder);
                }
                else
                {
                    //将两个节点合并
                    nn.Join(Branch);
                    parent.RemoveNode(Branch);
                    if (parent == m_Tree && parent.NodeNum == 1)
                    {
                        m_Tree = nn;
                        return;
                    }
                    JoinNode(parent);
                }
            }
            else
            {
                nn = Branch.Brother_Next;
                if (nn.NodeNum > m_HalfOrder)
                {
                    //只需要将两节点中的混合一下即可
                	Branch.MoveNodeFrom(nn, m_HalfOrder);
                }
                else
                {
                    //ln.Tree.NodeNum==m_HalfOrder，将两个节点合并
                    Branch.Join(nn);
                    parent.RemoveNode(nn);
                    if (parent == m_Tree && parent.NodeNum == 1)
                    {
                        m_Tree = Branch;
                        return;
                    }
                    JoinNode(parent);
                }
            }
        }
    }

    public void PrintTree()
    {
    	String retraction="";
    	System.out.println("========Print BPlusTree========");
    	PrintTree(m_Tree,retraction);
    	System.out.println("========Print Tree End========");
    }
    
    private void PrintTree(BTreeNode<TKey, TValue> node,String retraction)
    {
    	if(node==null) return;
    	System.out.println(retraction+node.Key);
    	String myretraction=retraction+"    ";
    	BTreeNode<TKey, TValue> tn=node.SonNodeList;
    	while(tn!=null)
    	{
    		PrintTree(tn,myretraction);
    		tn=tn.Brother_Next;
    	}    
    }

    private BTreeNode<TKey, TValue> GetMinNode(BTreeNode<TKey, TValue> Node)
    {
    	BTreeNode<TKey, TValue> tn=Node,pn=null;
    	while(tn!=null)
    	{
    		pn=tn;
    		tn=tn.SonNodeList;
    	}
    	return pn;
    }

    private BTreeNode<TKey, TValue> GetMaxNode(BTreeNode<TKey, TValue> Node)
    {
    	BTreeNode<TKey, TValue> tn=Node,pn=null;
    	while(tn!=null)
    	{
    		pn=tn;
    		tn=tn.LastSon;
    	}
    	return pn;
    }
    
    private BTreeNode<TKey, TValue> GetNextNode(BTreeNode<TKey, TValue> Node)
    {
    	BTreeNode<TKey, TValue> pn=Node,node=null;
    	node=pn.Brother_Next;
    	while(node==null)
		{
			//寻找还没有遍历完的堂兄弟祖节点
			pn=pn.Parent;
			if(pn==null)return null;
			node=pn.Brother_Next;
		}
    	return GetMinNode(node);
    }
	
    /** 
     * 实现Iterable接口中要求实现的方法 
     */  
    @Override  
    public Iterator<BTreeNode<TKey, TValue>> iterator() 
    {  
    	//返回一个MyIterator实例对象 
    	return new MyIterator();
    }        
    /** 
     * MyIterator是内部类，实现了Iterator<E>接口的类 
     */  
    class MyIterator implements Iterator<BTreeNode<TKey, TValue>>
    {
        private BTreeNode<TKey, TValue> currentNode =GetMinNode(m_Tree);
        @Override  
        public boolean hasNext() {
            //只要在调用next()后，index自加，确保index不等于person的长度  
            return currentNode!=null;  
        }
        @Override  
        public BTreeNode<TKey, TValue> next() {  
            //调用后指针需后移
        	BTreeNode<TKey, TValue> n= currentNode;
        	currentNode=GetNextNode(currentNode);
        	return n;
        } 
        @Override  
        public void remove() {  
            //未实现这个方法  
        }            
    }	

}