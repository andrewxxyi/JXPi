package bll;

import cn.ijingxi.Rest.httpServer.REST;
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

/**
 * REST接口类
 * 前端javascript的访问可看下./web_manager/js/app.js中的函数controller_codeList中的访问方法
 *
 * 敲代码功能的业务实现
 *
 */
public class coding {

	//访问权限是基于用户身份：只能是学生才能执行
	@ActiveRight(policy = ActiveRight.Policy.PeopleType, peopleType = Person.peopleType_student)
	//自定义的资源访问点必须用RES进行标注
	@REST
	//REST接口必须按如下的函数签名方式进行定义：
	//public jxHttpData myFuncName(Map<String, Object> ps, jxJson Param) throws Exception {
	/**
	 *
	 * @param ps 在url中送入的参数及/test?name=xxy&id=2，中的{name:xxy,id:2}
	 * @param Param post、put送入的参数表，在javascript中是参数对象：{x=5,y=6}
	 * @return 需要返回一个jxHttpData，执行成功需指定200的返回码，否则前端可能不能正确处理
	 * @throws Exception
	 */
	public jxHttpData listMyCoding(Map<String, Object> ps, jxJson Param) throws Exception {

		//ps中还送入了当前的session号，其中存有当前的登陆用户的信息；session具有15分钟的时效，如果用户在
		//15分钟内有操作，则自动顺延15分钟；否则超时系统会自动删除该session，大多数情况下，用户再用该sessionID
		//进行访问，就会自动提示超时了
		String sid = (String) ps.get("SessionID");
		//从session中获取登陆用户的id
		UUID peopleID = jxSession.getPeopleID(sid);
		//根据id查找该用户，GetByID是具有缓存能力的，即如果之前用GetByID查过，则后面就不需要再读数据库了
		People p = (People) People.GetByID(People.class, peopleID);
		List<jxJson> list = Person.listTodayMission_WithType(p, MissionType.Coding.toString());

		//准备返回
		jxHttpData rs = new jxHttpData(200, "处理完毕");
		//将查询到的数据添加到返回结果中，数据是一组数据
		jxJson ol = rs.addJsonList(list);
		return rs;
	}

	/**
	 * 获取某个敲代码任务的细节
	 *
	 * @param ps
	 * @param Param
	 * @return
	 * @throws Exception
	 */
	@ActiveRight(policy = ActiveRight.Policy.Accept)
	@REST
	public jxHttpData getCoding(Map<String, Object> ps, jxJson Param) throws Exception {

		UUID id = Trans.TransToUUID((String) ps.get("CodingID"));
		Mission m = (Mission) Mission.GetByID(Mission.class, id);
		if (m == null)
			return new jxHttpData(401, "该题目为空！");
		m.addExtJsonAttr("Num", m.getExtendValue("Info", "Num"));

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		//将查询到的数据添加到返回结果中，一个对象
		rs.addObj(m);
		return rs;
	}

	/**
	 * 发布敲代码的任务
	 *
	 * @param ps
	 * @param Param
	 * @return
	 * @throws Exception
	 */
	@ActiveRight(policy = ActiveRight.Policy.PeopleType, peopleType = Person.peopleType_teacher)
	@REST
	public jxHttpData issue(Map<String, Object> ps, jxJson Param) throws Exception {

		//String dip=Param.GetSubValue_String("DepID");
		//UUID depid=Trans.TransToUUID(dip);

		//需要敲入的次数
		Integer num = Param.GetSubValue_Integer("Number");
		jxLog.logger.debug("Number:" + num);

		//任务执行的日期，如未设置则为当天
		String day = Param.GetSubValue_String("Date");
		Date d = Trans.TransToDateNoTime(day);
		if (d == null) {
			d = utils.today();
			day = Trans.TransToString_Date(d);
		}
		int tagid = ObjTag.getTagID("敲代码模板");
		Queue<jxORMobj> list = Mission.listMission(tagid, d);
		Integer order = list.size() + 1;

		String name = Param.GetSubValue_String("Name");
		if (name == null || name == "")
			name = order.toString();

		Mission item = (Mission) Mission.Create(Mission.class);
		//item.ObjTypeID = ORMType.Department.ordinal();
		//item.ObjID = depid;
		item.TagID = tagid;
		item.Category = "敲代码模板";
		item.Name = name;
		//此处的题目说明是从前端送入的，应该是用base64编码过的
		item.Descr = Param.GetSubValue_String("Content");
		item.Time = d;
		item.TagOrder = order;
		item.setExtendValue("Info", "Num", num);
		//将新的活动保存到数据库中
		item.Insert();

		list = Person.list(Person.peopleType_student, 0, 0);
		for (jxORMobj obj : list) {
			People p = (People) obj;
			Person.addMission(p, day, item.Name, MissionType.Coding, order, Trans.TransToString(item.ID));
		}
		jxHttpData rs = new jxHttpData(200, "处理完毕");
		//没有数据需要返回，简单通知下做完了
		rs.setResult(true);
		return rs;
	}


	//执行完毕，只能学生来执行
	@ActiveRight(policy = ActiveRight.Policy.PeopleType, peopleType = Person.peopleType_student)
	@REST
	public jxHttpData close(Map<String, Object> ps, jxJson Param) throws Exception {

		String sid = (String) ps.get("SessionID");
		UUID peopleID = jxSession.getPeopleID(sid);
		People p = (People) People.GetByID(People.class, peopleID);

		String cid = Param.GetSubValue_String("CodingID");
		UUID id = Param.GetSubValue_UUID("CodingID");
		Mission m = (Mission) Mission.GetByID(Mission.class, id);

		Integer order = Param.GetSubValue_Integer("Order");

		Person.closeMission(p, MissionType.Coding.toString(), cid);

		int duration = Param.GetSubValue_Integer("Duration");

		Mission item = (Mission) Mission.Create(Mission.class);
		item.ObjTypeID = ORMType.People.ordinal();
		item.ObjID = peopleID;
		item.Name = m.Name;
		item.TagID = ObjTag.getTagID("敲代码");
		item.Category = "敲代码";
		item.TagState = MissionState.Over.ordinal();
		item.Time = utils.today();
		item.Number = Float.valueOf(duration);
		item.TagOrder = order;
		item.Insert();

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.setResult(true);
		return rs;
	}


}