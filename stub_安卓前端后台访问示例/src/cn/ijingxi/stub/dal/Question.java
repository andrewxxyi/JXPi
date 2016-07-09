package cn.ijingxi.stub.dal;

import cn.ijingxi.stub.general.jxJson;

/**
 * Created by andrew on 16-6-8.
 */
public class Question {

    public String ID;
    public String Name;
    public String Descr;
    public String State;
    public String CStartTime;
    public String CendTime;

    public Question(jxJson json) throws Exception {
        this.ID= (String) json.getSubObjectValue("ID");
        this.Name= (String) json.getSubObjectValue("Name");
        this.Descr= (String) json.getSubObjectValue("Descr");
        this.State= (String) json.getSubObjectValue("State");
        this.CStartTime= (String) json.getSubObjectValue("CStartTime");
        this.CendTime= (String) json.getSubObjectValue("CendTime");
    }
}
