package com.example.stub.dal;

import com.example.myapp.jxJson;

/**
 * Created by andrew on 16-6-8.
 */
public class Role {

    public String ID;
    public String Name;
    public String Descr;
    public String CreateTime;

    public Role(jxJson json) throws Exception {
        this.ID= (String) json.getSubObjectValue("ID");
        this.Name= (String) json.getSubObjectValue("Category");
        this.Descr= (String) json.getSubObjectValue("Descr");
        this.CreateTime= (String) json.getSubObjectValue("CreateTime");
    }
}
