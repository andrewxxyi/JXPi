package bll;

import cn.ijingxi.Rest.httpServer.REST;
import cn.ijingxi.Rest.httpServer.jxHttpData;
import cn.ijingxi.Rest.httpServer.jxSession;
import cn.ijingxi.app.ActiveRight;
import cn.ijingxi.app.ObjTag;
import cn.ijingxi.orm.*;
import cn.ijingxi.util.Trans;
import cn.ijingxi.util.jxCompare;
import cn.ijingxi.util.jxLog;
import cn.ijingxi.util.utils;
import dal.CommonObjTypeID;
import dal.Mission;
import dal.Schedule;

import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

/**
 * 参考下coding的说明
 *
 * 计划完成情况的记录
 *
 */
public class schedule {

	@ActiveRight(policy = ActiveRight.Policy.Accept)
	@REST
	public jxHttpData disp(Map<String, Object> ps, jxJson Param) throws Exception {
		UUID id = Trans.TransToUUID((String) Param.GetSubValue("PeopleID"));
		if (id == null)
			return new jxHttpData(404, "PeopleID为空");
		Date date = null;
		String c1 = Param.GetSubValue_String("Date");
		if (c1 == null)
			date = utils.Now_Date();
		else {
			String[] ss = c1.split("T");
			jxLog.logger.debug(ss[0]);
			date = Trans.TransToDateNoTime(ss[0]);
		}
		jxLog.logger.debug("get schedule:" + date.toString());

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		Schedule schedule =getSchedule(id,date);
		if(schedule!=null) {
			Queue<jxORMobj> os = Mission.listMission(schedule.ID);

			jxJson ol = rs.addObjList(os);
		}
		return rs;
	}

	private Schedule getSchedule(UUID peopleID,Date date) throws Exception {

		Date d1=utils.addSecond(date,3600*24);

		SelectSql s = new SelectSql();
		s.AddTable("ObjTag");
		s.AddContion("ObjTag", "ObjID", jxCompare.Equal, peopleID);
		s.AddContion("ObjTag", "TagID", jxCompare.Equal, ObjTag.getTagID("日程"));
		s.AddContion("ObjTag", "Time", jxCompare.GreateEqual, date);
		s.AddContion("ObjTag", "Time", jxCompare.Less, d1);
		Schedule schedule = (Schedule) Schedule.Get(Schedule.class, s);
		return schedule;
	}

	@ActiveRight(policy = ActiveRight.Policy.NormalUser)
	public jxHttpData POST(Map<String, Object> ps, jxJson Param) throws Exception {

		String sid = (String) ps.get("SessionID");
		UUID pid = jxSession.getPeopleID(sid);

		String c1 = (String) Param.GetSubValue("content1");
		jxLog.logger.debug("plan con1:"+c1);
		String c2 = (String) Param.GetSubValue("content2");
		jxLog.logger.debug("plan con2:"+c2);
		String c3 = (String) Param.GetSubValue("content3");
		jxLog.logger.debug("plan con3:"+c3);
		String c4 = (String) Param.GetSubValue("content4");
		jxLog.logger.debug("plan con4:"+c4);
		String c5 = (String) Param.GetSubValue("content5");
		jxLog.logger.debug("plan con5:"+c5);

		Date day=utils.Now_Date();
		jxLog.logger.debug("day:"+day);
		//day=utils.addSecond(day,3600*24);
		//jxLog.logger.debug("day:"+day);
		Schedule schedule =getSchedule(pid,day);
		if(schedule!=null)
			return new jxHttpData(401, "当前日程已创建");

		schedule=Schedule.New(pid,day);
		Mission m1=Mission.New(CommonObjTypeID.Schedule,schedule.ID,utils.getTime(day,9,0,0),utils.getTime(day,10,30,0));
		m1.Name=Mission.mk1;
		m1.Descr=c1;
		Mission m2=Mission.New(CommonObjTypeID.Schedule,schedule.ID,utils.getTime(day,10,30,0),utils.getTime(day,11,30,0));
		m2.Name=Mission.mk2;
		m2.Descr=c2;
		Mission m3=Mission.New(CommonObjTypeID.Schedule,schedule.ID,utils.getTime(day,13,30,0),utils.getTime(day,16,30,0));
		m3.Name=Mission.mk3;
		m3.Descr=c3;
		Mission m4=Mission.New(CommonObjTypeID.Schedule,schedule.ID,utils.getTime(day,16,30,0),utils.getTime(day,17,0,0));
		m4.Name=Mission.mk4;
		m4.Descr=c4;
		Mission m5=Mission.New(CommonObjTypeID.Schedule,schedule.ID,utils.getTime(day,18,30,0),utils.getTime(day,20,0,0));
		m5.Name=Mission.mk5;
		m5.Descr=c5;

		DB db = JdbcUtils.GetDB(null, this);
		db.Trans_Begin();
		try {
			synchronized (db) {

				schedule.Insert(db);
				m1.Insert(db);
				m2.Insert(db);
				m3.Insert(db);
				m4.Insert(db);
				m5.Insert(db);

			}
			db.Trans_Commit();
		} catch (Exception e) {
			jxLog.error(e);
			db.Trans_Cancel();
			jxHttpData rs = new jxHttpData(503, "内部错误：" + e.getMessage());
			return rs;
		}
		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.setResult(true);
		return rs;
	}


}