package bll;

import cn.ijingxi.Rest.httpServer.jxHttpData;
import cn.ijingxi.app.ActiveRight;
import cn.ijingxi.orm.jxJson;
import dal.Subject;

import java.util.Map;

/**
 * 参考下coding的说明
 *
 * 向题库中录入题目
 *
 */
public class subject {

	@ActiveRight(policy = ActiveRight.Policy.Manager)
	public jxHttpData POST(Map<String, Object> ps, jxJson Param) throws Exception {

		Subject.New(Param.GetSubValue_String("Category"), Param.GetSubValue_String("Descr"), Param.GetSubValue_Integer("answerTotal"), Param.GetSubValue_String("Answer"), Param.GetSubValue_Float("Difficulty"), Param.GetSubValue_Boolean("MultiSelect"));

		jxHttpData rs = new jxHttpData(200, "处理完毕");
		rs.setResult(true);
		return rs;
	}

}