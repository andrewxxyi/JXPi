package bll;

import cn.ijingxi.Rest.httpServer.RES;
import cn.ijingxi.Rest.httpServer.jxHttpData;
import cn.ijingxi.Rest.httpServer.jxSession;
import cn.ijingxi.app.ActiveRight;
import cn.ijingxi.app.ObjTag;
import cn.ijingxi.app.People;
import cn.ijingxi.orm.ORMType;
import cn.ijingxi.orm.jxJson;
import cn.ijingxi.orm.jxORMobj;
import cn.ijingxi.util.Trans;
import cn.ijingxi.util.jxLog;
import cn.ijingxi.util.utils;
import dal.Mission;
import dal.MissionState;
import dal.MissionType;

import java.util.*;

public class coding {

	@ActiveRight(policy = ActiveRight.Policy.PeopleType,peopleType = Person.peopleType_student)
	@RES
	public jxHttpData listMyCoding(Map<String, Object> ps, jxJson Param) throws Exception {

		String sid = (String) ps.get("SessionID");
		UUID peopleID = jxSession.getPeopleID(sid);
		People p= (People) People.GetByID(People.class,peopleID);
		List<jxJson> list = Person.listTodayMission_WithType(p,MissionType.Coding.toString());

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		jxJson ol = rs.addJsonList(list);
		return rs;
	}

	@ActiveRight(policy = ActiveRight.Policy.Accept)
	@RES
	public jxHttpData getCoding(Map<String, Object> ps, jxJson Param) throws Exception {

		UUID id = Trans.TransToUUID((String) ps.get("CodingID"));
		Mission m = (Mission) Mission.GetByID(Mission.class,id);
		if(m==null)
			return new jxHttpData(401, "该题目为空！");
		m.addExtJsonAttr("Num",m.getExtendValue("Info","Num"));

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.addObj(m);
		return rs;
	}

	@ActiveRight(policy = ActiveRight.Policy.PeopleType,peopleType = Person.peopleType_teacher)
	@RES
	public jxHttpData issue(Map<String, Object> ps, jxJson Param) throws Exception {

		//String dip=Param.GetSubValue_String("DepID");
		//UUID depid=Trans.TransToUUID(dip);

		Integer num=Param.GetSubValue_Integer("Number");
		jxLog.logger.debug("Number:"+num);

		String day=Param.GetSubValue_String("Date");
		Date d=Trans.TransToDateNoTime(day);
		if(d==null){
			d=utils.today();
			day=Trans.TransToString_Date(d);
		}
		int tagid=ObjTag.getTagID("敲代码模板");
		Queue<jxORMobj> list = Mission.listMission(tagid, d);
		Integer order=list.size()+1;

		String name=Param.GetSubValue_String("Name");
		if(name==null||name=="")
			name=order.toString();

		Mission item = (Mission) Mission.Create(Mission.class);
		//item.ObjTypeID = ORMType.Department.ordinal();
		//item.ObjID = depid;
		item.TagID = tagid;
		item.Category = "敲代码模板";
		item.Name=name;
		//此处的题目说明是从前端送入的，应该是用base64编码过的
		item.Descr=Param.GetSubValue_String("Content");
		item.Time = d;
		item.TagOrder=order;
		item.setExtendValue("Info","Num",num);
		item.Insert();

		list = Person.list(Person.peopleType_student, 0, 0);
		for(jxORMobj obj:list){
			People p=(People)obj;
			Person.addMission(p,day,item.Name,MissionType.Coding,order,Trans.TransToString(item.ID));
		}
		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.setResult(true);
		return rs;
	}


	@ActiveRight(policy = ActiveRight.Policy.PeopleType,peopleType = Person.peopleType_student)
	@RES
	public jxHttpData close(Map<String, Object> ps, jxJson Param) throws Exception {

		String sid = (String) ps.get("SessionID");
		UUID peopleID = jxSession.getPeopleID(sid);
		People p= (People) People.GetByID(People.class,peopleID);


		String cid=Param.GetSubValue_String("CodingID");
		UUID id=Param.GetSubValue_UUID("CodingID");

		Integer order=Param.GetSubValue_Integer("Order");
		Mission m = (Mission) Mission.GetByID(Mission.class,id);

		Person.closeMission(p,MissionType.Coding.toString(),cid);

		int duration=Param.GetSubValue_Integer("Duration");

		Mission item = (Mission) Mission.Create(Mission.class);
		item.ObjTypeID = ORMType.People.ordinal();
		item.ObjID = peopleID;
		item.Name=m.Name;
		item.TagID = ObjTag.getTagID("敲代码");
		item.Category = "敲代码";
		item.TagState = MissionState.Over.ordinal();
		item.Time = utils.today();
		item.Number= Float.valueOf(duration);
		item.TagOrder=order;
		item.Insert();

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.setResult(true);
		return rs;
	}



}