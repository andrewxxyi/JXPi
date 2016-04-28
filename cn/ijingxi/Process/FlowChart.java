package cn.ijingxi.Process;

import cn.ijingxi.app.People;
import cn.ijingxi.app.Role;
import cn.ijingxi.app.SerialNumber;
import cn.ijingxi.orm.*;
import cn.ijingxi.util.*;

import java.util.*;

/**
 * 这里的流程图，都是从一个start节点出发，全部收敛于一个end节点！
 * 业务流转是事情在节点上，迁移代表了业务流转；而生产，节点代表了中间状态，迁移则代表了实际动作
 *
 * Created by andrew on 15-10-2.
 */
public class FlowChart extends jxORMobj {

    public static final String Node_Start="开始";
    public static final String Node_End="结束";
    public static final String Node_Accept="接受";
    public static final String Node_Reject="拒绝";

    //private Map<String,Map<String,FlowPath>> allPath=null;
    private Map<String,flowNode> nodes=new HashMap<>();
    public Map<String,flowNode> getNodes(){return nodes;}

    protected void addTrans(String From, String To,Float cost) throws Exception {
        utils.Check(cost<=0,"路径代价需大于0："+cost);
        flowNode fn=nodes.get(From);
        if(fn==null){
            fn=new flowNode();
            nodes.put(From,fn);
        }
        flowNode tn=nodes.get(To);
        if(tn==null){
            tn=new flowNode();
            nodes.put(To,tn);
        }
        fn.toNodes.put(To,cost);
        tn.fromNodes.put(From,cost);
    }

    /**
     * 返回生成树
     * @param From
     * @return
     */
    public Map<String,FlowPath> getSourceTree(String From){
        Map<String,FlowPath> all=computePath_Dijkstra(From);
        Map<String,FlowPath> rs=new HashMap<>();

        jxLink<String,Boolean> T=new jxLink<>();
        for(String n:nodes.keySet())
            if(n.compareTo(From)!=0){
                int i=0;
                for(Map.Entry<String, FlowPath> entry : all.entrySet()){
                    //最远端的将只出现一次
                    if(entry.getValue().path.Exist(n))
                        i++;
                }
                if(i==1)
                    T.offer(n,true);
            }
        for(LinkNode<String,Boolean> node:T)
            rs.put(node.getKey(),all.get(node.getKey()));
        return rs;
    }

    /*
    public Map<String,Map<String,FlowPath>> computePath(boolean isMin){
        if(allPath==null){
            allPath=new HashMap<>();
            for (String fn:nodes.keySet()){
                Map<String,FlowPath> map=computePath_Dijkstra(fn,isMin);
                allPath.put(fn,map);
            }
        }
        return allPath;
    }
    */
    /**
     * 计算from到各个节点的最短路径
     * @param From
     */
    private Map<String,FlowPath> computePath_Dijkstra(String From){
        //from到每个节点的最短路径
        Map<String,FlowPath> dest=new HashMap<>();
        jxLink<String,Boolean> S=new jxLink<>();
        jxLink<String,Boolean> T=new jxLink<>();
        //以S中为顶点，所有未达的最短路径
        jxLink<Float,trans> ts=new jxLink<>();
        ts.permitKeyEqual=true;
        //初始化最短节点和待确定集合
        S.offer(From, true);
        for(String n:nodes.keySet())
            if(n.compareTo(From)!=0)
                T.offer(n,true);
        //初始化以S中为顶点，所有未达的最短路径
        flowNode fn=nodes.get(From);
        for(Map.Entry<String, Float> entry : fn.toNodes.entrySet()){
            trans t=new trans();
            t.cost=entry.getValue();
            t.From=From;
            t.To=entry.getKey();
            ts.addByRise(entry.getValue(),t);
        }

        trans mint=ts.poll();
        while (mint!=null){
            //如果所挑选出来的最短边的终点已在S中则忽略
            if(!S.Exist(mint.To)){
                //有新的最短节点出现
                T.delete(mint.To);
                S.offer(mint.To,true);
                FlowPath pf=dest.get(mint.From);
                FlowPath tf=clonePath(pf);
                tf.cost+=mint.cost;
                tf.path.offer(mint.To, true);
                dest.put(mint.To, tf);
                //将该终点所有的边加入到新的最短边待选
                flowNode tn=nodes.get(mint.To);
                for(Map.Entry<String, Float> entry : tn.toNodes.entrySet()){
                    if(!S.Exist(entry.getKey())){
                        trans t=new trans();
                        t.cost=entry.getValue();
                        t.From=mint.To;
                        t.To=entry.getKey();
                        ts.addByRise(entry.getValue(),t);
                    }
                }
            }
            mint=ts.poll();
        }
        return dest;
    }
    private FlowPath clonePath(FlowPath old){
        FlowPath n=new FlowPath();
        n.cost=old.cost;
        for(LinkNode<String, Boolean> sn:old.path)
            n.path.offer(sn.getKey(),true);
        return n;
    }


    public FlowChartInstance Start(DB db,People caller,String desc) throws Exception {
        FlowChartInstance fi= (FlowChartInstance) FlowChartInstance.Create(FlowChartInstance.class);
        fi.FlowChartID=ID;
        fi.Name=Name;
        fi.Descr=desc;
        SerialNumber sn = getSN();
        if(sn!=null)
            fi.SNCode= sn.next(db,caller,fi);
        fi.start(caller);
        return fi;
    }















    public static ORMID GetORMID (UUID ID)
    {
        return new ORMID(ORMType.FlowChart.ordinal(), ID);
    }

    public static void Init() throws Exception {
        InitClass(ORMType.FlowChart.ordinal(), FlowChart.class,"流程");
    }

    public static void CreateDB() throws Exception {
        CreateTableInDB(FlowChart.class);
    }

    @Override
    protected void Init_Create(DB db) throws Exception {
        ID = UUID.randomUUID();
        CreateTime = new Date();
    }
    @Override
    protected void myInit(DB db) throws Exception{
        Map<String, String> ks = new HashMap<String, String>();
        List<jxJson> ls = getExtendArrayList(Nodes, ks);
        List<String> rs = new LinkedList<String>();
        for (jxJson js : ls)
            nodes.put(((String) js.GetSubValue("NodeName")),null);
        List<jxJson> jl = getExtendArrayList("NodeTrans", ks);
        for (jxJson j : jl)
            addTrans((String) j.getSubObjectValue("From"),(String) j.getSubObjectValue("To"),Trans.TransToFloat(j.getSubObjectValue("cost")));
    }


    @ORM(Index = 1)
    public int TypeID;

    @ORM(keyType = ORM.KeyType.PrimaryKey)
    public UUID ID;

    @ORM(Index = 2)
    public String Name;

    @ORM(Descr="同名流程的区别")
    public Integer Ver;
    public String getVerName(){
        return Ver==0?Name:Name+Ver;
    }

    @ORM(Descr = "说明信息")
    public String Descr;

    @ORM(Descr="流水号模板,应通过setSNModel设置")
    public String SNModel;

    @ORM(Index = 3)
    public Date CreateTime;

    @ORM(Descr = "json格式的所有节点名")
    public String Nodes;

    @ORM(Descr = "json格式的所有流转以")
    public String NodeTrans;

    @ORM(Descr = "json格式的自动执行条件，只有设为auto的才可以")
    public String TranContion;

    @ORM(Descr = "json格式的数据定义")
    public String DataDef;

    @ORM(Descr = "json格式的权限设置，即每个节点能看到哪些数据")
    public String Right;

    @ORM
    public Boolean NoUsed;

    public void setSNModel(String model) throws Exception {
        SNModel=model;
        setSN(Name,model);
    }

    public void setDataItemDef(String ItemName,jxDataType DataType,String DispFormat,boolean DefaultRight) throws Exception {
        Map<String, String> ks = new HashMap<String, String>();
        ks.put("ItemName", ItemName);
        setExtendArrayValue("DataDef", ks, "DataType", DataType.ordinal());
        setExtendArrayValue("DataDef", ks, "DispFormat", DispFormat);
        setExtendArrayValue("DataDef", ks, "DefaultRight", DefaultRight);
    }
    public jxDataType getDataItemDef_DataType(String ItemName) throws Exception {
        Map<String, String> ks = new HashMap<String, String>();
        ks.put("ItemName", ItemName);
        return (jxDataType) Trans.TransTojxEunm(jxDataType.class,Trans.TransToInteger(getExtendArrayValue("DataDef", ks, "DataType")));
    }
    public String getDataItemDef_DispFormat(String ItemName) throws Exception {
        Map<String, String> ks = new HashMap<String, String>();
        ks.put("ItemName", ItemName);
        return getExtendArrayValue("DataDef", ks, "DispFormat");
    }
    public boolean getDataItemDef_DefaultRight(String ItemName) throws Exception {
        Map<String, String> ks = new HashMap<String, String>();
        ks.put("ItemName", ItemName);
        return Trans.TransToBoolean(getExtendArrayValue("DataDef", ks, "DefaultRight"));
    }
    public void setDataAccessRight(String ItemName,String NodeName,boolean right) throws Exception {
        Map<String, String> ks = new HashMap<String, String>();
        ks.put("ItemName", ItemName);
        setExtendArrayValue("Right", ks, NodeName, right);
    }
    public boolean getDataAccessRight(String ItemName,String NodeName) throws Exception {
        Map<String, String> ks = new HashMap<String, String>();
        ks.put("ItemName", ItemName);
        String v=getExtendArrayValue("Right", ks, NodeName);
        if(v==null)
            return getDataItemDef_DefaultRight(ItemName);
        return Trans.TransToBoolean(v);
    }

    /**
     * 修改也是这个函数
     *
     * @param NodeName
     * @param InputTypeIsAnd
     * @param OutputTypeIsAnd
     * @throws Exception
     */
    public void setNode(String NodeName, boolean InputTypeIsAnd, boolean OutputTypeIsAnd) throws Exception {
        nodes.put(NodeName,null);
        Map<String, String> ks = new HashMap<String, String>();
        ks.put("NodeName", NodeName);
        setExtendArrayValue("Nodes", ks, "InputType", InputTypeIsAnd);
        setExtendArrayValue("Nodes", ks, "OutputType", OutputTypeIsAnd);
    }
    public void setNodeAttr(String NodeName,String AttrName,Object AttrValue) throws Exception {
        Map<String, String> ks = new HashMap<String, String>();
        ks.put("NodeName", NodeName);
        setExtendArrayValue("Nodes", ks, AttrName, AttrValue);
    }
    public String getNodeAttr(String NodeName,String AttrName) throws Exception {
        Map<String, String> ks = new HashMap<String, String>();
        ks.put("NodeName", NodeName);
        return getExtendArrayValue("Nodes", ks, AttrName);
    }

    public List<String> ListNodes() throws Exception {
        Map<String, String> ks = new HashMap<String, String>();
        List<jxJson> ls = getExtendArrayList(Nodes, ks);
        List<String> rs = new LinkedList<String>();
        for (jxJson js : ls)
            rs.add((String) js.GetSubValue("NodeName"));
        return rs;
    }

    /**
     * true：代表and输入
     * @param NodeName
     * @return
     * @throws Exception
     */
    public boolean getNode_InputType(String NodeName) throws Exception {
        String rs = getNodeAttr(NodeName, "InputType");
        if (rs != null)
            return Trans.TransToBoolean(rs);
        return false;
    }

    public void setNode_InputType(String NodeName, boolean InputTypeIsAnd) throws Exception {
        setNodeAttr(NodeName, "InputType", InputTypeIsAnd);
    }

    /**
     * true：代表and输出
     * @param NodeName
     * @return
     * @throws Exception
     */
    public boolean getNode_OutputType(String NodeName) throws Exception {
        String rs = getNodeAttr(NodeName, "OutputType");
        if (rs != null)
            return Trans.TransToBoolean(rs);
        return false;
    }

    public void setNode_OutputType(String NodeName, boolean OutputTypeIsAnd) throws Exception {
        setNodeAttr(NodeName, "OutputType", OutputTypeIsAnd);
    }

    /**
     * 输出是或输出，才有必要对输出分支进行选择
     * @param NodeName
     * @return
     * @throws Exception
     */
    public boolean getNode_Auto(String NodeName) throws Exception {
        String rs = getNodeAttr(NodeName, "Auto");
        if (rs != null)
            return Trans.TransToBoolean(rs);
        return false;
    }

    public void setNode_Auto(String NodeName, boolean Auto) throws Exception {
        setNodeAttr(NodeName, "Auto", Auto);
    }
    public String getNode_AutoByExecer(String NodeName) throws Exception
    {
        return getNodeAttr(NodeName, "AutoByExecer");
    }
    public void setNode_AutoByExecer(String NodeName,String toNodeName) throws Exception
    {
        setNodeAttr(NodeName, "AutoByExecer", toNodeName);
    }
    public People getNode_RealExecer(String NodeName) throws Exception
    {
        Map<String,String> ks=new HashMap<String,String>();
        ks.put("NodeName", NodeName);
        jxJson rs=getExtendArrayNode("Nodes",ks);
        if(rs==null)return null;
        ORMID id=ORMID.GetFromJSON(rs.GetSubObject("ExecerID"));
        if(id==null)return null;
        if(id.getTypeID()==ORMType.People.ordinal())
            return (People) GetByID(People.class,id.getID());
        if(id.getTypeID()==ORMType.Role.ordinal())
        {
            Role r=(Role) GetByID(Role.class,id.getID());
            if(r!=null)
                return r.GetMapTo();
        }
        return null;
    }

    public void setTrans(String From, String ExportName, String To,Float cost) throws Exception {
        addTrans(From,To,cost);
        Map<String, String> ks = new HashMap<String, String>();
        ks.put("From", From);
        ks.put("Export", ExportName);
        setExtendArrayValue("NodeTrans", ks, "To", To);
        setExtendArrayValue("NodeTrans", ks, "cost", cost);
    }
    public Float getCost(String From,String To) throws Exception {
        Map<String, String> ks = new HashMap<String, String>();
        ks.put("From", From);
        ks.put("To", To);
        return Trans.TransToFloat(getExtendArrayValue("NodeTrans", ks, "cost"));
    }

    public String getTrans_To(String From, String ExportName) throws Exception {
        Map<String, String> ks = new HashMap<String, String>();
        ks.put("From", From);
        ks.put("Export", ExportName);
        return getExtendArrayValue("NodeTrans", ks, "To");
    }

    public String getTrans_Export(String From, String To) throws Exception {
        Map<String, String> ks = new HashMap<String, String>();
        ks.put("From", From);
        ks.put("To", To);
        return getExtendArrayValue("NodeTrans", ks, "Export");
    }

    /**
     * 对于生成，事情是在trans上
     * @param From
     * @param ExportName
     * @param attrName
     * @param value
     * @throws Exception
     */
    public void setTransAttr(String From, String ExportName, String attrName,Object value) throws Exception {
        Map<String, String> ks = new HashMap<String, String>();
        ks.put("From", From);
        ks.put("Export", ExportName);
        setExtendArrayValue("NodeTrans", ks, attrName, value);
    }
    public String getTransAttr(String From, String ExportName, String attrName) throws Exception {
        Map<String, String> ks = new HashMap<String, String>();
        ks.put("From", From);
        ks.put("Export", ExportName);
        return getExtendArrayValue("NodeTrans", ks, attrName);
    }

    public List<String> ListFrom(String To) throws Exception {
        List<String> rs = new LinkedList<String>();
        Map<String, String> ks = new HashMap<String, String>();
        ks.put("To", To);
        List<jxJson> jl = getExtendArrayList("NodeTrans", ks);
        if (jl.size() > 0)
            for (jxJson j : jl)
                rs.add((String) j.getSubObjectValue("From"));
        return rs;
    }

    public List<String> ListExport(String From) throws Exception {
        List<String> rs = new LinkedList<String>();
        Map<String, String> ks = new HashMap<String, String>();
        ks.put("From", From);
        List<jxJson> jl = getExtendArrayList("NodeTrans", ks);
        if (jl.size() == 0) return null;
        for (jxJson j : jl)
            rs.add((String) j.getSubObjectValue("Export"));
        return rs;
    }
    public List<String> ListTo(String From) throws Exception {
        List<String> rs = new LinkedList<String>();
        Map<String, String> ks = new HashMap<String, String>();
        ks.put("From", From);
        List<jxJson> jl = getExtendArrayList("NodeTrans", ks);
        if (jl.size() == 0) return null;
        for (jxJson j : jl)
            rs.add((String) j.getSubObjectValue("To"));
        return rs;
    }


    /**
     * 条件测试，通过对Info中某项值的检测，
     * @param From
     * @param ExportName
     * @param cl
     * @throws Exception
     */
    public void setTranContion(String From,String ExportName,ContionLink cl) throws Exception
    {
        Map<String,String> ks=new HashMap<String,String>();
        ks.put("From", From);
        ks.put("Export", ExportName);
        setExtendArraySubNode("TranContion",ks,cl.TojxJson());
    }
    public ContionLink getTranContion(String From,String ExportName) throws Exception
    {
        Map<String,String> ks=new HashMap<String,String>();
        ks.put("From", From);
        ks.put("Export", ExportName);
        List<jxJson> rs=getExtendArrayList("TranContion",ks);
        if(rs!=null&&rs.size()==1)
            return new ContionLink(rs.get(0).GetSubObject("ContionLink"));
        return null;
    }


    public class flowNode{
        //to,代价
        public Map<String,Float> toNodes=new HashMap<>();
        //from,代价
        public Map<String,Float> fromNodes=new HashMap<>();
    }

    public class FlowPath{
        public Float cost;
        public jxLink<String,Boolean> path=new jxLink<>();
    }

    private class trans{
        Float cost=0f;
        String From=null;
        String To=null;
    }

}
