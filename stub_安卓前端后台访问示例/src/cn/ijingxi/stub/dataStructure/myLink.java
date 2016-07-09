package cn.ijingxi.stub.dataStructure;

import java.util.Iterator;

/**
 * 单链表，泛型即可以支持任意类型
 * 使用方法：
 * 如想使用一个键是整形，值是字符串的链表
 * myLink<Integer,String> myLink=new myLink<Integer,String>();
 *
 * TKey extends Comparable<TKey>表示TKey是可以比较的，也就是
 *
 * Created by andrew on 16-6-14.
 */
public class myLink<TKey extends Comparable<TKey>,TValue> implements Iterable{
    //链中的节点
    public class linkNode{
        TKey key;
        TValue value;
        linkNode next;
    }

    //默认无限容量

    //当前队列中保存的对象数量
    int count;

    public int getCount() {
        return count;
    }

    //链头
    linkNode list;

    /**
     * 初始化
     */
    public myLink() {
        count = 0;
        list=null;
    }
    public myLink(int i){
        int j=0;
        long l=0;
        if(j==l)
        {

        }

    }

    /**
     * 根据指定的键值从链中查找该键值所在的节点
     * @param key
     * @return
     */
    private linkNode searchNode(TKey key){
        if(list==null)return null;
        //list永远指向链头，不能动，所以需要一个临时指针
        linkNode temp = list;
        //从链头开始逐一检查每个链中的节点，看看其键值是否等于给出的key
        while (temp!=null){
            //找到了
            if(temp.key.compareTo(key)==0)
                return temp;
            //没找到，继续准备检查下一个节点
            temp=temp.next;
        }
        //就是没找到
        return null;
    }

    /**
     * 存入数据
     * @param key
     * @param value
     * @throws Exception
     */
    public void put(TKey key,TValue value) throws Exception {
        //昨天没这一句，加上了就能多线程并发来用了
        synchronized (this) {
            linkNode node = searchNode(key);
            if (node != null)
                throw new Exception("键值已存在，不允许插入" + key.toString());
            //或者允许修改已存在节点的值
            //if(node!=null){
            //    node.value=value;
            //    retrun;
            //}
            //准备插入新的节点
            node = new linkNode();
            node.key = key;
            node.value = value;
            //将新节点插入到原先的链表之前
            node.next = list;
            //修正list的值，使之正确的指向新的表头
            list = node;

            //早上老师都忘了这条，不要学老师，要严谨！！
            count++;
        }
    }

    /**
     * 取出数据
     * @param key
     * @return
     */
    public TValue get(TKey key) {
        //昨天没这一句，加上了就能多线程并发来用了
        synchronized (this) {
            //找到key在链中的存放位置
            linkNode node = searchNode(key);
            if (node != null) {
                //如果key在链中则将对应的值返回给用户
                count--;
                return node.value;
            }
            //没有就没有呗
            return null;
        }
    }

    /**
     * 根据指定的键值从链中查找该键值所在的节点的父节点
     * @param key
     * @return
     */
    private linkNode searchParentNode(TKey key){
        if(list==null)return null;
        linkNode pn=list;
        //如果key就是链表，那么key就是没有父节点的
        if(pn.key.compareTo(key)==0)return null;
        //son是pn的儿子
        linkNode son = list.next;
        //检查每一个儿子的节点，看看其键值是否等于给出的key
        while (son!=null){
            //找到了
            if(son.key.compareTo(key)==0)
                return pn;
            //没找到，继续准备检查下一个节点
            pn=son;
            son=pn.next;
        }
        return null;
    }

    /**
     *
     * @param key
     * @return
     */
    public TValue remove(TKey key) {
        //昨天没这一句，加上了就能多线程并发来用了
        synchronized (this) {
            //边界检查
            if (list == null) return null;
            //如果链头就是则从链头进行移除
            if (list.key.compareTo(key) == 0) {
                TValue temp = list.value;
                list = list.next;
                count--;
                return temp;
            }

            //找到key在链中的存放位置父亲
            linkNode pn = searchParentNode(key);
            if (pn != null) {
                //pn是key的父节点，所以pn.next就是key所在的节点，一定不为空
                TValue temp = pn.next.value;
                //将指向父节点儿子（也就是自己）的指针修改为指向父节点的孙子（也就是自己的儿子）
                pn.next = pn.next.next;
                count--;
                return temp;
            }
            //没有就没有呗
            return null;
        }
    }

    /**
     * 将链表转换为数组
     * @return
     */
    Object[] TransToArray(){
        //如果链表为空则返回空数组
        if(count==0)
            return null;
        //转换后的数组，其容量恰好是链表中对象的数量
        Object[] rs = new Object[count];
        //数组的索引:【0，count-1】即取值范围是从0到count-1
        int i=0;
        //链表是从头到尾顺序进行扫描，但表头不能动，所以找一个临时变量代替表头从头开始扫描
        linkNode temp = list;
        while (temp!=null){
            //将链中的值保存到目标数组中
            rs[i]=temp.value;
            //增加数组索引值
            i++;
            //准备扫描链中的下一个对象
            temp=temp.next;
        }
        return rs;
    }

    /**
     * 遍历
     *
     *
     * 实现Iterable接口中要求实现的方法
     * 实现该接口后即可以使用for(linkNode node:mylink)来遍历该链表了
     *
     * 枚举接口的工作原理：
     * java其实会按如下形式来进行枚举：

       Iterator iterator = list.iterator();
       while(iterator.hasNext()){
         String string = iterator.next();
         //do something
       }


     //如果并发操作，会怎么样呢？
     *
     *
     */
    @Override
    public Iterator iterator() {
        return new MyIterator();//返回一个MyIterator实例对象
    }
    /**
     * MyIterator是内部类，实现了Iterator<E>接口的类
     */
    class MyIterator implements Iterator<linkNode> {
        //初始化遍历指针
        private linkNode myNode = list;

        //判断是否有当前值可供使用
        @Override
        public boolean hasNext() {
            return myNode != null;
        }

        //获取当前值，同时将遍历指针前进一
        //node=iterator.next();
        //每个node该如何使用呢？！下面的行不行呢？！
        //node.key
        //node.value
        @Override
        public linkNode next() {
            linkNode n = myNode;
            myNode = myNode.next;
            return n;
        }

        @Override
        public void remove() {
            //未实现这个方法
        }
    }

}
