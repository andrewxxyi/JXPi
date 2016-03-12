package cn.ijingxi.common.Process;

import cn.ijingxi.common.app.FileDesc;
import cn.ijingxi.common.app.Group;
import cn.ijingxi.common.app.People;
import cn.ijingxi.common.msg.Message;
import cn.ijingxi.common.msg.MessageType;
import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.util.*;

import java.util.*;

import static cn.ijingxi.common.util.Trans.TransTojxEunm;

/**
 * 任务分发还没有做完
 *
 * Created by andrew on 15-10-2.
 */
public class FlowChartInstance extends jxORMobj {

    @ORM(keyType= ORM.KeyType.PrimaryKey)
    public UUID ID;

    @ORM(Index=1)
    public String Name;

    @ORM(Descr="说明信息")
    public String Descr;

    @ORM(Descr="流水号")
    public String SNCode;

    @ORM(Index=2,Descr="用于两者之间的同步")
    public Date CreateTime;

    @ORM(Index=3)
    public UUID FlowChartID;
    public FlowChart getFlowChart() throws Exception {return (FlowChart) GetByID(FlowChart.class,FlowChartID);}

    @ORM(Index=4,Descr="流程的当前状态")
    public InstanceState State;

    @ORM(Descr="json格式的各节点的输入token,NodeName,From,TokenNum")
    public String InputToken;

    @ORM(Descr="json格式的各节点的运行状态")
    public String NodeState;

    @ORM(Descr="json格式的流程数据，其定义在FlowChart中")
    public String Data;

    @ORM(Index=5,Descr="json格式的流程数据，其定义在FlowChart中")
    public UUID FileGroupID;

    @ORM(Descr="json格式的信息")
    public String Info;

    public void start(People caller) throws Exception {
        CallParam param=new CallParam(caller,null,null);
        param.addParam("FI",this);
        param.addParam("NodeName",FlowChart.Node_Start);
        InstanceState state=getNodeState(FlowChart.Node_Start);
        if(state==InstanceState.None){
            state=InstanceState.Waiting;
            setNodeState(FlowChart.Node_Start,state);
            DelaySave();
        }
        NodeSM.Happen(state, InstanceEvent.Trigger, param);
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("sn", SNCode);
        //jxLog.log(LogPriority.info, ORMType.FlowChartInstance.ordinal(), ID, Name, "启动", map);
    }

    private void close() throws Exception {
        State=InstanceState.Closed;
        Update();
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("sn", SNCode);
        //jxLog.log(LogPriority.info, ORMType.FlowChartInstance.ordinal(), ID, Name, "关闭", map);
    }

    public void setData(String ItemName,Object value) throws Exception {
        setExtendValue("Data",ItemName,value);
    }

    public String getData(String ItemName,String NodeName) throws Exception {
        if(getFlowChart().getDataAccessRight(ItemName,NodeName)){
            String v = getExtendValue("Data", ItemName);
            jxDataType ty = getFlowChart().getDataItemDef_DataType(ItemName);
            String df = getFlowChart().getDataItemDef_DispFormat(ItemName);
            if(df!=null)
                return String.format(df,Trans.TransTo(ty,v));
            return v;
        }
        return "无权查看";
    }

    public void addFile(String fileName,String desc,String storePath) throws Exception {
        Group g=null;
        if(FileGroupID==null){
            g=(Group)Group.Create(Group.class);
            FileGroupID=g.ID;
            g.setClsType(FileDesc.class);
            g.setCategory("流程文件");
            g.Insert();
        }
        else
            g= (Group) Group.GetByID(Group.class,FileGroupID);
        FileDesc fd=FileDesc.add("流程文件",fileName,desc,storePath);
        fd.Insert();
        g.addMember(FileDesc.class,fd.ID);
    }

    public void touchNode(IExecutor Caller,String FromName,String NodeName,String Msg) throws Exception
    {
        CallParam param=new CallParam(Caller,null,Msg);
        param.addParam("FI",this);
        param.addParam("From",FromName);
        param.addParam("NodeName",NodeName);

        //因为节点可能是第一次被触发，其状态还没有设置，所以如果没有要代为初始化
        InstanceState state=getNodeState(NodeName);
        if(state==InstanceState.None){
            state=InstanceState.Waiting;
            setNodeState(NodeName,state);
        }
        NodeSM.Happen(state, InstanceEvent.Touch, param);
        DelaySave();
    }
    public void closeNode(IExecutor Caller,String NodeName,String ExportName,String Msg) throws Exception
    {
        CallParam param=new CallParam(Caller,null,Msg);
        param.addParam("FI",this);
        param.addParam("NodeName",NodeName);
        param.addParam("ExportName",ExportName);
        InstanceState state=getNodeState(NodeName);
        NodeSM.Happen(state, InstanceEvent.Close, param);
        DelaySave();
    }


    void setNodeState(String NodeName,InstanceState state) throws Exception
    {
        Map<String,String> ks=new HashMap<String,String>();
        ks.put("n", NodeName);
        setExtendArrayValue(NodeState, ks, "s", state.ordinal());
    }
    InstanceState getNodeState(String NodeName) throws Exception
    {
        Map<String,String> ks=new HashMap<String,String>();
        ks.put("n", NodeName);
        return (InstanceState) TransTojxEunm(InstanceState.class, Trans.TransToInstanceState(getExtendArrayValue(InputToken, ks, "s")));
    }

    void HasInput(String NodeName,String From) throws Exception
    {
        if(From==null)return;
        int num=getInputToken(NodeName,From);
        num++;
        setInputToken(NodeName, From, num);
    }
    void setInputToken(String NodeName,String From,Integer TokenNum) throws Exception
    {
        Map<String,String> ks=new HashMap<String,String>();
        ks.put("n", NodeName);
        ks.put("f", From);
        setExtendArrayValue(InputToken, ks, "t", TokenNum);
    }
    int getInputToken(String NodeName,String From) throws Exception
    {
        Map<String,String> ks=new HashMap<String,String>();
        ks.put("n", NodeName);
        ks.put("f", From);
        String v=getExtendArrayValue(InputToken,ks,"t");
        return Trans.TransToInteger(v);
    }
    boolean checkInputToken(String NodeName) throws Exception
    {
        FlowChart p=getFlowChart();
        int num=0;
        List<String> fl=p.ListFrom(Name);
        Map<String,Integer> itl=new HashMap<String,Integer>();
        for(String f:fl)
        {
            num=getInputToken(NodeName,f);
            if(num<=0)
                return false;
            itl.put(f, num);
        }
        for(String f:fl)
        {
            num=itl.get(f);
            num--;
            setInputToken(NodeName, f, num);
        }
        return true;
    }


    static jxStateMachine<InstanceState,InstanceEvent> NodeSM=null;
    static
    {
        NodeSM=new jxStateMachine<InstanceState,InstanceEvent>();
        NodeSM.setStateFunc((param1, param2) -> {
            CallParam param=(CallParam)param1;
            InstanceState state=(InstanceState)param2;
            FlowChartInstance fi = (FlowChartInstance) param.getParam("FI");
            String nodename = (String) param.getParam("NodeName");
            fi.setNodeState(nodename,state);
        });

        IDoSomething checkInput=new IDoSomething() {
            @Override
            public void Do(CallParam param) throws Exception {
                FlowChartInstance fi = (FlowChartInstance) param.getParam("FI");
                String nodename = (String) param.getParam("NodeName");
                String fn = (String) param.getParam("From");
                InstanceState state=fi.getNodeState(nodename);
                FlowChart process = fi.getFlowChart();
                utils.Check(process == null, "本节点所关联的流程没有定义！");
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("node", nodename);
                map.put("state", state);
                map.put("event", InstanceEvent.Touch);
                //jxLog.log(LogPriority.info, ORMType.FlowChartInstance.ordinal(), fi.ID, fi.Name, "触发", map);
                if (process.getNode_InputType(nodename)) {
                    fi.HasInput(nodename, fn);
                    if (fi.checkInputToken(nodename))
                        NodeSM.Happen(state, InstanceEvent.Trigger, param);
                } else
                    //直接触发
                    NodeSM.Happen(state, InstanceEvent.Trigger, param);
            }
        };
        IDoSomething dual=new IDoSomething() {
            @Override
            public void Do(CallParam param) throws Exception {
                FlowChartInstance fi = (FlowChartInstance) param.getParam("FI");
                String nodename = (String) param.getParam("NodeName");
                InstanceState state=fi.getNodeState(nodename);

                Map<String,Object> map=new HashMap<String, Object>();
                map.put("node", nodename);
                map.put("state", state);
                map.put("event", InstanceEvent.Touch);
                //jxLog.log(LogPriority.info, ORMType.FlowChartInstance.ordinal(), fi.ID, fi.Name, "执行", map);

                if(nodename==FlowChart.Node_End)
                {
                    //执行完毕，通知PI结束
                    fi.close();
                }
                //else if(node.getProcess().getNode_OutputType(node.Name)||node.getProcess().getNode_Auto(node.Name)){
                else if(fi.getFlowChart().getNode_Auto(nodename)){
                    //如果是与输出则相当于自动执行，不再等待Execer的动作
                    Map<String,IDoSomething> dm=autoDual.get(fi.getFlowChart().getVerName());
                    if(dm!=null){
                        IDoSomething dual = dm.get(nodename);
                        if(dual!=null)
                            dual.Do(param);
                    }
                    NodeSM.Happen(state, InstanceEvent.Close, param);
                }
                //else if(execer!=null)
                {
                    //没有执行者，要掷出警告
                }
            }
        };
        IDoSomething close=new IDoSomething() {
            @Override
            public void Do(CallParam param) throws Exception {
                FlowChartInstance fi = (FlowChartInstance) param.getParam("FI");
                String nodename = (String) param.getParam("NodeName");
                InstanceState state=fi.getNodeState(nodename);
                FlowChart fc = fi.getFlowChart();

                Map<String,Object> map=new HashMap<String, Object>();
                map.put("node", nodename);
                map.put("state", state);
                map.put("event", InstanceEvent.Touch);
                //jxLog.log(LogPriority.info, ORMType.FlowChartInstance.ordinal(), fi.ID, fi.Name, "执行完毕", map);

                if(fc.getNode_OutputType(nodename))
                {
                    //每一个后继节点都被触发
                    List<String> el = fc.ListExport(nodename);
                    for(String e:el)
                    {
                        String tn=fc.getTrans_To(nodename, e);
                        fi.touchNode(param.Caller,nodename,tn,null);
                    }
                }
                else if(fc.getNode_AutoByExecer(nodename)!=null){
                    String aetn=fc.getNode_AutoByExecer(nodename);
                    //指定了某后继分支如有则导向之，没有则选择其它分支中的第一个导向之
                    String tnfirst=null;
                    List<String> el = fc.ListExport(nodename);
                    for(String e:el)
                    {
                        String tn=fc.getTrans_To(nodename, e);
                        if(tn==aetn){
                            People execer=fc.getNode_RealExecer(tn);
                            if(execer!=null){
                                fi.touchNode(param.Caller, nodename, tn, null);
                                return;
                            }
                        }
                        else if(tnfirst==null)
                            tnfirst=tn;
                    }
                    fi.touchNode(param.Caller, nodename, tnfirst, null);
                }
                else if(fc.getNode_Auto(nodename))
                {
                    //后继分支自动进行条件检测后选择
                    String tnfirst=null;
                    List<String> el = fc.ListExport(nodename);
                    for(String e:el)
                    {
                        ContionLink cl=fc.getTranContion(nodename, e);
                        String tn=fc.getTrans_To(nodename, e);
                        if(cl!=null&&cl.Judge(fi))
                        {
                            fi.touchNode(param.Caller, nodename, tn, null);
                            return;
                        }
                        else if(tnfirst==null)
                            tnfirst=tn;
                    }
                    fi.touchNode(param.Caller,nodename,tnfirst,null);
                }
                else
                {
                    //param.Trans是用户指定的
                    String exportname=(String)param.getParam("ExportName");
                    String tn=fc.getTrans_To(nodename, exportname);
                    fi.touchNode(param.Caller, nodename, tn, null);
                }
            }
        };
        //初始化节点的状态转换
        NodeSM.AddTrans(InstanceState.Waiting, InstanceEvent.Touch, InstanceState.Waiting, checkInput);
        //允许重复不停顿、反复执行的流程，即某节点处理完毕后还可继续执行；通常的流程是一个流程处理相同的一类事，一个实例处理一件事
        NodeSM.AddTrans(InstanceState.Closed, InstanceEvent.Touch, InstanceState.Waiting, checkInput);
        NodeSM.AddTrans(InstanceState.Waiting, InstanceEvent.Trigger, InstanceState.Doing, dual);
        NodeSM.AddTrans(InstanceState.Doing, InstanceEvent.Close, InstanceState.Closed, close);
        //NodeSM.AddTrans(InstanceState.Doing, InstanceEvent.Pause, InstanceState.Paused, new NodePause());
        //NodeSM.AddTrans(InstanceState.Paused, InstanceEvent.Trigger, InstanceState.Doing, new NodeDual());
    }
    //自动处理
    private static Map<String,Map<String,IDoSomething>> autoDual=new HashMap<>();
    public static void addAutoDual(String processVerName,String nodeName,IDoSomething dual){
        Map<String,IDoSomething> map=autoDual.get(processVerName);
        if(map==null){
            map=new HashMap<>();
            autoDual.put(processVerName,map);
        }
        map.put(nodeName,dual);
    }

    public void informExecr(int typeID,UUID objID) throws Exception {
        Message msg=Message.New(ORMType.FlowChartInstance.ordinal(),ID,typeID,objID, MessageType.Event,"有任务到达");

    }


    public static ORMID GetORMID(UUID ID)
    {
        return new ORMID(ORMType.FlowChartInstance.ordinal(),ID);
    }

    @Override
    protected void Init_Create(DB db) throws Exception
    {
        ID=UUID.randomUUID();
        CreateTime=new Date();
        State=InstanceState.Doing;
    }

    public static void Init() throws Exception
    {
        InitClass(ORMType.FlowChartInstance.ordinal(),FlowChartInstance.class,"流程实例");
    }
    public static void CreateDB() throws Exception
    {
        CreateTableInDB(FlowChartInstance.class);
    }
}
