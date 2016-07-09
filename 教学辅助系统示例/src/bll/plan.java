package bll;

import cn.ijingxi.Rest.httpServer.REST;
import cn.ijingxi.Rest.httpServer.jxHttpData;
import cn.ijingxi.app.ActiveRight;
import cn.ijingxi.app.ObjTag;
import cn.ijingxi.orm.*;
import cn.ijingxi.util.Trans;
import cn.ijingxi.util.jxCompare;
import cn.ijingxi.util.jxLog;
import cn.ijingxi.util.utils;
import dal.CommonObjTypeID;
import dal.Mission;
import dal.Plan;

import java.util.Date;
import java.util.Map;
import java.util.Queue;

/**
 * 参考下coding的说明
 *
 * 每天需要执行的计划
 *
 */
public class plan {

	/**
	 * 一个计划是由5个活动模块所组成
	 * @param ps
	 * @param Param
	 * @return
	 * @throws Exception
     */
	@ActiveRight(policy = ActiveRight.Policy.Accept)
	@REST
	public jxHttpData disp(Map<String, Object> ps, jxJson Param) throws Exception {


		Date date = null;
		String c1 = Param.GetSubValue_String("Date");
		if (c1 == null)
			date = utils.Now_Date();
		else {
			String[] ss = c1.split("T");
			jxLog.logger.debug(ss[0]);
			date = Trans.TransToDateNoTime(ss[0]);
		}
		jxLog.logger.debug("get plan:" + date.toString());

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		Plan plan =getPlan(date);
		if(plan!=null) {
			Queue<jxORMobj> os = Mission.listMission(plan.ID);
			//jxLog.logger.debug("os size:"+os.size());
			jxJson ol = rs.addObjList(os);
			//jxLog.logger.debug("rs:"+rs.getString());

		}
		return rs;
	}

	/**
	 * 取的是当天的计划
	 * @param date
	 * @return
	 * @throws Exception
     */
	private Plan getPlan(Date date) throws Exception {

		Date d1=utils.addSecond(date,3600*24);

		SelectSql s = new SelectSql();
		s.AddTable("ObjTag");
		s.AddContion("ObjTag", "TagID", jxCompare.Equal, ObjTag.getTagID("课程计划"));
		s.AddContion("ObjTag", "Time", jxCompare.GreateEqual, date);
		s.AddContion("ObjTag", "Time", jxCompare.Less, d1);
		Plan plan = (Plan) Plan.Get(Plan.class, s);
		return plan;
	}

	/**
	 * 创建计划
	 * @param ps
	 * @param Param
	 * @return
	 * @throws Exception
     */
	@ActiveRight(policy = ActiveRight.Policy.NormalUser)
	public jxHttpData POST(Map<String, Object> ps, jxJson Param) throws Exception {

		String c1 = (String) Param.GetSubValue("content1");
		//jxLog.logger.debug("plan con1:"+c1);
		String c2 = (String) Param.GetSubValue("content2");
		//jxLog.logger.debug("plan con2:"+c2);
		String c3 = (String) Param.GetSubValue("content3");
		//jxLog.logger.debug("plan con3:"+c3);
		String c4 = (String) Param.GetSubValue("content4");
		//jxLog.logger.debug("plan con4:"+c4);
		String c5 = (String) Param.GetSubValue("content5");
		//jxLog.logger.debug("plan con5:"+c5);

		Date day=utils.Now_Date();
		//jxLog.logger.debug("day:"+day);
		//day=utils.addSecond(day,3600*24);
		//jxLog.logger.debug("day:"+day);
		Plan plan =getPlan(day);
		if(plan!=null)
			return new jxHttpData(401, "当前计划已创建");

		plan=Plan.New(day);
		Mission m1=Mission.New(CommonObjTypeID.Plan,plan.ID,utils.getTime(day,9,0,0),utils.getTime(day,10,30,0));
		m1.Name=Mission.mk1;
		m1.Descr=c1;
		Mission m2=Mission.New(CommonObjTypeID.Plan,plan.ID,utils.getTime(day,10,30,0),utils.getTime(day,11,30,0));
		m2.Name=Mission.mk2;
		m2.Descr=c2;
		Mission m3=Mission.New(CommonObjTypeID.Plan,plan.ID,utils.getTime(day,13,30,0),utils.getTime(day,16,30,0));
		m3.Name=Mission.mk3;
		m3.Descr=c3;
		Mission m4=Mission.New(CommonObjTypeID.Plan,plan.ID,utils.getTime(day,16,30,0),utils.getTime(day,17,0,0));
		m4.Name=Mission.mk4;
		m4.Descr=c4;
		Mission m5=Mission.New(CommonObjTypeID.Plan,plan.ID,utils.getTime(day,18,30,0),utils.getTime(day,20,0,0));
		m5.Name=Mission.mk5;
		m5.Descr=c5;

		//由于是一次需往数据库中插入多条数据，所以采用了事务功能
		DB db = JdbcUtils.GetDB(null, this);
		db.Trans_Begin();
		try {
			synchronized (db) {

				plan.Insert(db);
				m1.Insert(db);
				m2.Insert(db);
				m3.Insert(db);
				m4.Insert(db);
				m5.Insert(db);

			}
			//正确提交
			db.Trans_Commit();
		} catch (Exception e) {
			jxLog.error(e);
			//错误回退
			db.Trans_Cancel();
			jxHttpData rs = new jxHttpData(503, "内部错误：" + e.getMessage());
			return rs;
		}
		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.setResult(true);
		return rs;
	}


}