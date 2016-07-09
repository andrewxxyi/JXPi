package cn.ijingxi.stub.dal;

import cn.ijingxi.stub.general.jxJson;

/**
 * Created by andrew on 15-9-19.
 */
public class Team {

    public String ID;
    public String Name;
    public String Descr;
    public String CreateTime;

    public Team(jxJson json) throws Exception {
        this.ID= (String) json.getSubObjectValue("ID");
        this.Name= (String) json.getSubObjectValue("Name");
        this.Descr= (String) json.getSubObjectValue("Descr");
        this.CreateTime= (String) json.getSubObjectValue("CreateTime");
    }

}
