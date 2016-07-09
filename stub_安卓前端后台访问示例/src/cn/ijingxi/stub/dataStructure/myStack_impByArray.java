package cn.ijingxi.stub.dataStructure;

/**
 * 用数组方式实现的栈，泛型即可以支持任意类型
 * 使用方法：
 * 如想使用一个100字符串的栈
 * myStack_impByArray<String> stack=new myStack_impByArray<String>(100);
 *
 * 或想使用10整数的栈
 * myStack_impByArray<Integer> stack=new myStack_impByArray<Integer>(10);
 *
 *
 *
 * Created by andrew on 16-6-14.
 */
public class myStack_impByArray<TValue> {

    //最大容量
    int maxLength = 0;
    //当前已压入到栈中的对象数量
    int count;
    public int getCount(){return count;}
    //栈顶位置，也就是当前数据存放位置
    int top;
    //实际存储数据的数组
    Object[] arr;

    /**
     * 初始化
     * @param maxLength 需指定最大容量
     */
    public myStack_impByArray(int maxLength) {
        this.maxLength = maxLength;
        arr = new Object[maxLength];
        top = -1;
        count = 0;
    }

    /**
     * 压栈，将数据压入栈中
     * @param v
     * @throws Exception
     */
    public void push(TValue v) throws Exception {
        //容量已满，则掷出异常
        if (count >= maxLength)
            throw new Exception("已达到最大容量，无法继续添加");
        //对top指针进行修正
        //可适当考虑如何优化，但前提是你必须明确的理解top=-1的意思
        if (count == 0)
            top = 0;
        else
            top++;
        //将数据保存到栈顶
        arr[top] = v;
        count++;
    }

    /**
     * 弹出数据
     * @return
     */
    public TValue pop() {
        //没有数据则返回空值，但如果当初就压入了null，则此处是无法分辨的，应当如何处理呢？
        if (count == 0)
            return null;
        //后面会修正top的值，所以预先取出栈顶的值临时保存一下
        Object temp = arr[top];
        count--;
        //修正top的值，和push一样可以进行优化
        if (count == 0)
            top = -1;
        else
            top--;
        return (TValue) temp;
    }


}
