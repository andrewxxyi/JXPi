
package cn.ijingxi.common.util;

import cn.ijingxi.common.orm.jxORMobj;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 只在全局中有，
 * 
 * 统计分析在lanserver中每天晚上运行
 * 本机上只统计分析自己的
 * 
 * @author andrew
 *
 */
public class jxLog extends jxORMobj {
	public static Boolean TraceDebug = true;
	public static Logger logger = LogManager.getLogger("common.log");

	public static void debug(String msg) {
		if (TraceDebug)
			logger.debug(msg);
	}

	public static void warn(String msg) {
		logger.warn(msg);
	}

	public static void error(String msg) {
		logger.error(msg);
	}

	public static void error(Exception e) {
		logger.error(getStackTrace(e));
	}

	public static String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		try {
			t.printStackTrace(pw);
			return sw.toString();
		} finally {
			pw.close();
		}
	}

}