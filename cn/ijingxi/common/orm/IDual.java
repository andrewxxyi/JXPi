package cn.ijingxi.common.orm;

/**
 * 当从数据库中读出时就进行处理
 * @author andrew
 *
 */
public interface IDual {
	public jxJson Do(jxORMobj obj) throws Exception;
}
