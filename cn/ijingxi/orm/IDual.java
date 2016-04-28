package cn.ijingxi.orm;

/**
 * 当从数据库中读出时就进行处理
 * @author andrew
 *
 */
public interface IDual {
	public void Do(jxORMobj obj,String key,Object v) throws Exception;
}
