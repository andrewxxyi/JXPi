package cn.ijingxi.stub.dal;

import cn.ijingxi.stub.general.jxJson;

/**
 * Created by andrew on 15-9-19.
 */
public class Task {

    public String ID;
    public String Name;
    public String Descr;
    public String CreateTimeC;
    public String Type;
    public String StarTime;
    public String EndTime;
    public String State;
    public String ObjID;
    public String ExecorName;

    public Task(jxJson json) throws Exception {
        this.ID= (String) json.getSubObjectValue("ID");
        this.Name= (String) json.getSubObjectValue("Name");
        this.Descr= (String) json.getSubObjectValue("Descr");
        this.CreateTimeC= (String) json.getSubObjectValue("CreateTimeC");
        this.Type= (String) json.getSubObjectValue("Type");
        this.StarTime= (String) json.getSubObjectValue("StarTime");
        this.EndTime= (String) json.getSubObjectValue("EndTime");
        this.State= (String) json.getSubObjectValue("State");
        this.ObjID= (String) json.getSubObjectValue("ObjID");
        this.ExecorName= (String) json.getSubObjectValue("ExecorName");
    }

}
