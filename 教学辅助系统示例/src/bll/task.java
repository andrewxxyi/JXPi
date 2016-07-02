package bll;

import cn.ijingxi.Rest.httpServer.RES;
import cn.ijingxi.Rest.httpServer.jxHttpData;
import cn.ijingxi.app.ActiveRight;
import cn.ijingxi.app.ObjTag;
import cn.ijingxi.app.People;
import cn.ijingxi.orm.SelectSql;
import cn.ijingxi.orm.jxJson;
import cn.ijingxi.orm.jxORMobj;
import cn.ijingxi.util.Trans;
import cn.ijingxi.util.jxCompare;
import cn.ijingxi.util.jxLog;
import cn.ijingxi.util.utils;
import dal.Mission;
import dal.MissionState;
import dal.PrjTeam;

import java.util.*;

public class task {

	@ActiveRight(policy = ActiveRight.Policy.Accept)
	public jxHttpData GET(Map<String, Object> ps, jxJson Param) throws Exception {

		jxLog.logger.debug("TaskID:" + (String) ps.get("TaskID"));
		UUID qid = Trans.TransToUUID((String) ps.get("TaskID"));

		Mission q = (Mission) Mission.GetByID(Mission.class, qid);
		if (q == null)
			return new jxHttpData(404, "没找到指定的任务");
		q.addExtJsonAttr("StarTime", Trans.TransToChinese(q.Time));
		q.addExtJsonAttr("EndTime", Trans.TransToChinese(q.ToTime));
		q.addExtJsonAttr("Type", ObjTag.getTagName(q.TagID));
		q.addExtJsonAttr("CreatTimeC", Trans.TransToChinese(q.CreateTime));
		q.addExtJsonAttr("State", ((MissionState) Trans.TransTojxEunm(MissionState.class, q.TagState)).toChinese());
		q.addExtJsonAttr("ExecorID", q.ObjID);
		People p = (People) People.GetByID(People.class, q.ObjID);
		if (p != null)
			q.addExtJsonAttr("ExecorName", p.Name);

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.addObj(q);
		return rs;
	}

	@ActiveRight(policy = ActiveRight.Policy.NormalUser)
	public jxHttpData PUT(Map<String, Object> ps, jxJson Param) throws Exception {

		UUID qid = Trans.TransToUUID((String) ps.get("TaskID"));
		Mission q = (Mission) Mission.GetByID(Mission.class, qid);
		if(q==null)
			return new jxHttpData(404, "没找到指定的任务");
		q.Descr=Param.GetSubValue_String("Content");
		q.ToTime=new Date();
		q.TagState=MissionState.Over.ordinal();
		q.Update();

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.setResult(true);
		return rs;
	}

	@ActiveRight(policy = ActiveRight.Policy.NormalUser)
	public jxHttpData POST(Map<String, Object> ps, jxJson Param) throws Exception {

		Mission q= (Mission) Mission.Create(Mission.class);
		q.Name=Param.GetSubValue_String("Name");
		q.TagID=ObjTag.getTagID("任务");
		Date date = null;
		String c1 = Param.GetSubValue_String("StartTime");
		if (c1 == null)
			date = utils.Now_Date();
		else
			date = Trans.TransToDate(c1);
		q.Time=date;
		q.TagState=MissionState.Waiting.ordinal();
		q.ObjID=Trans.TransToUUID(Param.GetSubValue_String("PeopleID"));
		q.Insert();

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.setResult(true);
		return rs;
	}

	@ActiveRight(policy = ActiveRight.Policy.Accept)
	@RES
	public jxHttpData listMyTask(Map<String, Object> ps, jxJson Param) throws Exception {

		UUID peopleID = Trans.TransToUUID((String) Param.getSubObjectValue("PeopleID"));
		if (peopleID == null)
			return new jxHttpData(401, "请指定一个人员");

		Queue<jxORMobj> rl = listTask_People(peopleID,Param.GetSubValue_String("State"));

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		jxJson ol = rs.addObjList(rl);
		return rs;
	}

	private Queue<jxORMobj> listTask_People(UUID pid,String st) throws Exception {
		SelectSql s = new SelectSql();
		s.AddTable("ObjTag");
		s.AddContion("ObjTag", "TagID", jxCompare.Equal, ObjTag.getTagID("任务"));
		if (pid != null)
			s.AddContion("ObjTag", "ObjID", jxCompare.Equal, pid);
		int state = 0;
		switch (st) {
			case "Waiting":
				state = MissionState.Waiting.ordinal();
				break;
			case "Over":
				state = MissionState.Over.ordinal();
				break;
		}
		if (state > 0)
			s.AddContion("ObjTag", "TagState", jxCompare.Equal, state);

		return selectTask(s);
	}

	@ActiveRight(policy = ActiveRight.Policy.Accept)
	@RES
	public jxHttpData listTeamTask(Map<String, Object> ps, jxJson Param) throws Exception {

		UUID tID = Trans.TransToUUID((String) Param.getSubObjectValue("TeamID"));
		if (tID == null)
			return new jxHttpData(401, "请指定一个项目组");
		String state=Param.GetSubValue_String("State");
		Queue<jxORMobj> rl=new LinkedList<>();
		Queue<jxORMobj> list = PrjTeam.listMember(tID, null);
		for(jxORMobj obj:list){
			People p=(People)obj;
			Queue<jxORMobj> tl = listTask_People(p.ID, state);
			for(jxORMobj r:tl)
				rl.offer(r);
		}

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		jxJson ol = rs.addObjList(rl);
		return rs;
	}

	private Queue<jxORMobj> selectTask(SelectSql s) throws Exception {

		return Mission.Select(Mission.class,s,false, (obj, key, v) -> {
            if ("Time".compareTo(key) == 0)
                obj.addExtJsonAttr("StarTime", Trans.TransToChinese((Date) v));
            else if ("ToTime".compareTo(key) == 0)
                obj.addExtJsonAttr("EndTime", Trans.TransToChinese((Date) v));
            else if ("CreateTime".compareTo(key) == 0)
                obj.addExtJsonAttr("CreatTimeC", Trans.TransToChinese((Date) v));
            else if ("TagID".compareTo(key) == 0)
                obj.addExtJsonAttr("Type", ObjTag.getTagName((Integer) v));
            else if ("TagState".compareTo(key) == 0)
                obj.addExtJsonAttr("State", ((MissionState) Trans.TransTojxEunm(MissionState.class, v)).toChinese());
            else if ("ObjID".compareTo(key) == 0){
                People p = (People) People.GetByID(People.class, (UUID) v);
                if (p != null)
                    obj.addExtJsonAttr("ExecorName", p.Name);
            }
        });
	}


}