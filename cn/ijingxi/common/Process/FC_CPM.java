package cn.ijingxi.common.Process;

import cn.ijingxi.common.util.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * 节点一定都是与输入、与输出
 *
 * Created by andrew on 15-10-4.
 */
public class FC_CPM {

    //最早开始时间，key为节点名
    public Map<String,Float> Earliest=null;
    //最晚开始时间
    public Map<String,Float> Latest=null;
    //从start到end的最长路径
    public Queue<String> rs=null;

    private Map<String,nodeInput> inputes=null;
    private Map<String,FlowChart.flowNode> nodes=null;
    private String startNode=null;
    private String endNode=null;


    public Float getSlack(String nodeName){
        return Latest.get(nodeName)-Earliest.get(nodeName);
    }

    private jxSyncResult<Queue<String>> getrs=null;
    public String getErrorMsg(){
        if(getrs!=null)
            return getrs.errorMsg;
        return null;
    }

    public FC_CPM(Map<String,FlowChart.flowNode> allNode,String startNode,String endNode) throws Exception {
        nodes=allNode;
        this.startNode=startNode;
        this.endNode=endNode;
    }

    public Queue<String> start() throws Exception {
        IDo dual=new IDo() {
            @Override
            public void Do(Object param) throws Exception {
                //启动start
                nodeInput ns = inputes.get(startNode);
                CallParam cp=new CallParam(null,null,null);
                cp.addParam("cmp",this);
                cp.addParam("nodename", startNode);
                cpmSM_E.Happen(ns.state, InstanceEvent.Trigger, cp);
            }
        };

        getrs=new jxSyncResult();
        Earliest=new HashMap<>();
        Latest=new HashMap<>();
        inputes=new HashMap<>();
        for(Map.Entry<String, FlowChart.flowNode> entry : nodes.entrySet()) {
            String key=entry.getKey();
            nodeInput ni=new nodeInput();
            inputes.put(key,ni);
        }
        Earliest.put(startNode, 0f);
        return getrs.exec(dual,null);
    }

    //E计算完毕
    private void computedE() throws Exception {
        for(Map.Entry<String, nodeInput> entry : inputes.entrySet()) {
            entry.getValue().state=InstanceState.Waiting;
            entry.getValue().input=new HashMap<>();
        }
        //对于终点，其肯定是CPM的终点，所以e=l
        Latest.put(endNode, Earliest.get(endNode));

        //从终点倒排计算
        nodeInput ne = inputes.get(endNode);
        CallParam cp=new CallParam(null,null,null);
        cp.addParam("cmp",this);
        cp.addParam("nodename",endNode);
        cpmSM_L.Happen(ne.state, InstanceEvent.Trigger, cp);
    }

    //L计算完毕，根据e=l从起点开始获取cpm
    private void computedL() throws Exception {
        rs=new LinkedList<>();
        getTo_EL(startNode);
    }
    private  void getTo_EL(String nodename) throws Exception {
        rs.offer(nodename);
        if(nodename.compareTo(endNode)==0)
            getrs.setRS(rs);
        else {
            FlowChart.flowNode fn = nodes.get(nodename);
            for (Map.Entry<String, Float> entry : fn.toNodes.entrySet()) {
                if (Earliest.get(entry.getKey()) == Latest.get(entry.getKey())) {
                    getTo_EL(entry.getKey());
                    break;
                }
            }
        }
    }


    static jxStateMachine<InstanceState,InstanceEvent> cpmSM_E=null;
    static jxStateMachine<InstanceState,InstanceEvent> cpmSM_L=null;
    static {

        IDo2 setState= (param1, param2) -> {
            CallParam cp = (CallParam) param1;
            FC_CPM cpm= (FC_CPM) cp.getParam("cmp");
            String nodename= (String) cp.getParam("nodename");
            InstanceState state = (InstanceState) param2;
            nodeInput ni = cpm.inputes.get(nodename);
            ni.state = state;
        };

        IDoSomething checkInput_E= param -> {
            FC_CPM cpm= (FC_CPM) param.getParam("cmp");
            String nodename= (String) param.getParam("nodename");
            String fn = (String) param.getParam("from");
            Float cost= (Float) param.getParam("cost");
            nodeInput ni=cpm.inputes.get(nodename);
            ni.input.put(fn, cost);
            if(ni.input.size()==cpm.nodes.get(nodename).fromNodes.size())
                cpmSM_E.Happen(ni.state, InstanceEvent.Trigger, param);
        };
        IDoSomething dual_E= param -> {
            FC_CPM cpm= (FC_CPM) param.getParam("cmp");
            String nodename= (String) param.getParam("nodename");
            nodeInput ni=cpm.inputes.get(nodename);
            Float max=0f;
            //E是从起点到本节点的最大路径
            for(Map.Entry<String, Float> entry : ni.input.entrySet()){
                if(entry.getValue()>max)
                    max=entry.getValue();
            }
            cpm.Earliest.put(nodename,max);
            if(nodename.compareTo(cpm.endNode)==0)
                //已计算到终点
                cpm.computedE();
            else
                for(Map.Entry<String, Float> entry : cpm.nodes.get(nodename).toNodes.entrySet()){
                    //触发所有的后继节点
                    CallParam cp=new CallParam(null,null,null);
                    cp.addParam("cmp", cpm);
                    cp.addParam("nodename", entry.getKey());
                    cp.addParam("from", nodename);
                    cp.addParam("cost", max + entry.getValue());
                    nodeInput nt=cpm.inputes.get(entry.getKey());
                    cpmSM_E.Happen(nt.state, InstanceEvent.Touch, cp);
                }
        };

        IDoSomething checkInput_L= param -> {
            FC_CPM cpm= (FC_CPM) param.getParam("cmp");
            String nodename= (String) param.getParam("nodename");
            String tn = (String) param.getParam("to");
            Float cost= (Float) param.getParam("cost");
            nodeInput ni=cpm.inputes.get(nodename);
            ni.input.put(tn, cost);
            if(ni.input.size()==cpm.nodes.get(nodename).toNodes.size())
                cpmSM_L.Happen(ni.state, InstanceEvent.Trigger, param);
        };
        IDoSomething dual_L= param -> {
            FC_CPM cpm= (FC_CPM) param.getParam("cmp");
            String nodename= (String) param.getParam("nodename");
            nodeInput ni=cpm.inputes.get(nodename);
            Float min=Float.MAX_VALUE;
            for(Map.Entry<String, Float> entry : ni.input.entrySet()){
                if(entry.getValue()<min)
                    min=entry.getValue();
            }
            cpm.Latest.put(nodename, min);
            if(nodename.compareTo(cpm.startNode)==0)
                cpm.computedL();
            else
                for(Map.Entry<String, Float> entry : cpm.nodes.get(nodename).fromNodes.entrySet()){
                    CallParam cp=new CallParam(null,null,null);
                    cp.addParam("cmp", cpm);
                    cp.addParam("nodename", entry.getKey());
                    cp.addParam("to", nodename);
                    cp.addParam("cost", min - entry.getValue());
                    nodeInput nf=cpm.inputes.get(entry.getKey());
                    cpmSM_L.Happen(nf.state, InstanceEvent.Touch, cp);
                }
        };


        cpmSM_E = new jxStateMachine<InstanceState, InstanceEvent>();
        cpmSM_E.setStateFunc(setState);
        cpmSM_E.AddTrans(InstanceState.Waiting, InstanceEvent.Touch, InstanceState.Waiting, checkInput_E);
        cpmSM_E.AddTrans(InstanceState.Waiting, InstanceEvent.Trigger, InstanceState.Doing, dual_E);

        cpmSM_L = new jxStateMachine<InstanceState, InstanceEvent>();
        cpmSM_L.setStateFunc(setState);
        cpmSM_L.AddTrans(InstanceState.Waiting, InstanceEvent.Touch, InstanceState.Waiting, checkInput_L);
        cpmSM_L.AddTrans(InstanceState.Waiting, InstanceEvent.Trigger, InstanceState.Doing, dual_L);
    }

    static class nodeInput{
        InstanceState state=InstanceState.Waiting;
        Map<String,Float> input=new HashMap<>();
    }

}
