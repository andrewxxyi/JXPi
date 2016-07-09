package cn.ijingxi.stub.dataStructure;

/**
 * 用单链表来实现栈
 *
 *
 * Created by andrew on 16-6-16.
 */
public class myStack_Link<TValue> {
    //定义常量
    class node{
        TValue value=null;
        node next=null;
    }

    //
    //定义变量
    //
    /**
     * 栈中已存放的对象数量
     *
     *
     */
    int count=0;
    public int get_Count(){return count;}
    /**
     * 栈顶，表头，栈中的第一个元素
     */
    node list=null;

    public void push(TValue v){
        node newNode=new node();
        newNode.value=v;
        newNode.next=list;
        list=newNode;
        count++;
    }

    public TValue pop(){
        if(count<=0)
            return null;
        //只要有数据，则list一定不为空

        //因为是对表头进行操作，所以用一个临时变量保存表头的数据
        node temp = list;
        list=list.next;
        count--;
        return temp.value;
    }

}
