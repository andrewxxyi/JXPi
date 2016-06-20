
package com.example.myapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class utils {

	private static Map<String, Object> objCache = new HashMap<String, Object>();

	public static void putCache(String key, Object obj) {
		objCache.put(key, obj);
	}

	public static Object getCache(String key) {
		return objCache.get(key);
	}


	private static String regSuffix = "\\.([^\\.]+)$";
	private static Pattern RegSuffix = Pattern.compile(regSuffix);

	/**
	 * 获取文件名的后缀
	 *
	 * @param filename
	 * @return
	 */
	public static String getSuffix(String filename) {
		Matcher m = RegSuffix.matcher(filename);
		//Matcher m = RegSuffix.matcher(filename);
		while (m.find())
			return m.group(1);
		return null;
	}

	public static Integer daysBetween(Date f, Date t) {
		Calendar cf = Calendar.getInstance();
		cf.setTime(f);
		Calendar cfd = utils.GetDate(cf);
		Calendar ct = Calendar.getInstance();
		ct.setTime(t);
		Calendar ctd = utils.GetDate(ct);
		return Trans.TransToInteger((ctd.getTimeInMillis() - cfd.getTimeInMillis()) / 1000 * 3600 * 24);
	}

	public static boolean checkLSB(byte b) {
		return (b & (byte) 0x1) == (byte) 0x1;
	}
	/**
	 * 无符号右移一位
	 * @param bs
	 * @return
     */
	public static byte[] rightShift_unsigned(byte[] bs) {
		byte[] rs = new byte[bs.length];
		boolean b = false;
		for (int i = 0; i < bs.length; i++) {
			rs[i] = (byte) (b ? 0x80 : 0);
			b = checkLSB(bs[i]);
			rs[i] |= bs[i] >>> 1;
		}
		return rs;
	}

	public static boolean checkByteArr(byte[] bs1,byte[] bs2) {
		if (bs1 == null && bs2 == null) return true;
		if (bs1 != null && bs2 != null) {
			for (int i = 0; i < bs1.length; i++) {
				if (bs1[i] != bs2[i])
					return false;
			}
			return true;
		}
		return false;
	}

	public static Integer secondsBetween(Date f, Date t) {
		Calendar cf = Calendar.getInstance();
		cf.setTime(f);
		Calendar ct = Calendar.getInstance();
		ct.setTime(t);
		return Trans.TransToInteger((ct.getTimeInMillis() - cf.getTimeInMillis()) / 1000);
	}

	public static Calendar GetDate(Calendar t) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, t.get(Calendar.YEAR));
		c.set(Calendar.MONTH, t.get(Calendar.MONTH));
		c.set(Calendar.DAY_OF_MONTH, t.get(Calendar.DAY_OF_MONTH));
		c.set(Calendar.HOUR,0);
		c.set(Calendar.MINUTE,0);
		c.set(Calendar.SECOND,0);
		return c;
	}

	/**
	 * 比较c1和c2的日期
	 *
	 * @param c1
	 * @param c2
	 * @return
	 */
	public static boolean checkCalendarDate(Calendar c1, Calendar c2) {
		if (c1.get(Calendar.YEAR) < c2.get(Calendar.YEAR))
			return true;
		if (c1.get(Calendar.YEAR) > c2.get(Calendar.YEAR))
			return false;
		if (c1.get(Calendar.MONTH) < c2.get(Calendar.MONTH))
			return true;
		if (c1.get(Calendar.MONTH) > c2.get(Calendar.MONTH))
			return false;
		if (c1.get(Calendar.DAY_OF_MONTH) < c2.get(Calendar.DAY_OF_MONTH))
			return true;
		if (c1.get(Calendar.DAY_OF_MONTH) > c2.get(Calendar.DAY_OF_MONTH))
			return false;
		return false;
	}

	public static Date addSecond(Date dt, int seconds) {
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		c.add(Calendar.SECOND, seconds);
		return c.getTime();
	}
	public static Date getTime(Date day, int hour, int minute, int second) {
		Calendar c = Calendar.getInstance();
		c.setTime(day);
		c.add(Calendar.HOUR, hour);
		c.add(Calendar.MINUTE, minute);
		c.add(Calendar.SECOND, second);
		return c.getTime();
	}

	public static boolean CheckDate(int year, int month, int day) {
		try {
			Calendar c = Calendar.getInstance();
			c.setLenient(false);
			c.set(Calendar.YEAR, year);
			c.set(Calendar.MONTH, month);
			c.set(Calendar.DATE, day);
			// 如果日期错误,执行该语句,必定抛出异常.
			c.get(Calendar.YEAR);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public static Calendar GetCalendar(int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(year, month - 1, day);
		return c;
	}

	public static Calendar Now() {
		Date d = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c;
	}
	public static Date Now_Date() {
		Date d = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		Calendar t = GetDate(c);
		return t.getTime();
	}

	public static boolean judgeIsSameClass(Class<?> cls, Class<?> cls1) {
		String cn = GetClassName(cls);
		return cn.compareTo(GetClassName(cls1)) == 0;
	}


	public static String GetClassName(Class<?> cls) {
		return GetClassName(cls.getName());
	}

	public static String GetClassName(String ClassName) {
		String[] ss = ClassName.split("\\.");
		if (ss.length == 0)
			return ClassName;
		else
			return ss[ss.length - 1];
	}

	public static boolean JudgeIsEnum(Class<?> cls) {
		Class<?> p = cls.getSuperclass();
		if (p == null) return false;
		return GetClassName(p).compareTo("Enum") == 0;
	}

	public static String StringAdd(String str, String split, String WantAdd) {
		if (str == null)
			return WantAdd;
		else
			return str + split + WantAdd;
	}

	public static String[] StringSplit(String str, String split) {
		String[] ss = str.split(split);
		Queue<String> q = new LinkedList<>();
		for (int i = 0; i < ss.length; i++) {
			if (ss[i].length() > 0)
				q.offer(ss[i]);
		}
		String[] rs = new String[q.size()];
		if (q.size() > 0) {

			int i = 0;
			String s = q.poll();
			while (s != null) {
				rs[i] = s;
				i++;
				s = q.poll();
			}
		}
		return rs;
	}

	public static void checkAssert(boolean b,String msg) throws Exception {
		if(!b)
			throw new Exception(msg);
	}

	public static void writeToFile(String filename, InputStream inStream) throws IOException {
		//jxLog.logger.debug("filename：" + filename);
		FileOutputStream fs = null;
		try {
			File f = new File(filename);
			if (f.exists()) {
				//jxLog.logger.debug("file exist,deleted:" + filename);
				f.delete();
			}
			fs = new FileOutputStream(filename);
			byte[] buffer = new byte[1024];
			int readnum = inStream.read(buffer);
			while (readnum > 0) {
				fs.write(buffer, 0, readnum);
				readnum = inStream.read(buffer);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (fs != null) {
					fs.flush();
					fs.close();
				}
				inStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void unzipFile(String targetPath, InputStream zipFileStream) {

		try {
			ZipInputStream zis = new ZipInputStream(zipFileStream);
			ZipEntry entry = null;
			//jxLog.logger.debug("开始解压到:" + targetPath + "...");
			while ((entry = zis.getNextEntry()) != null) {
				String zipPath = entry.getName();
				try {
					if (entry.isDirectory()) {
						File zipFolder = new File(targetPath + File.separator
								+ zipPath);
						if (!zipFolder.exists()) {
							zipFolder.mkdirs();
						}
					} else {
						File file = new File(targetPath + File.separator
								+ zipPath);
						if (!file.exists()) {
							File pathDir = file.getParentFile();
							pathDir.mkdirs();
							file.createNewFile();
						}

						FileOutputStream fos = new FileOutputStream(file);
						int bread;
						while ((bread = zis.read()) != -1) {
							fos.write(bread);
						}
						fos.close();

					}
					//jxLog.logger.debug("成功解压:" + zipPath);

				} catch (Exception e) {
					//jxLog.error(e);
					continue;
				}
			}
			zis.close();
			zipFileStream.close();
			System.out.println("解压结束");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void checkFolderExists(String path) {
		checkFolderExists(path, true);
	}

	public static void checkFolderExists(String path, boolean pathisdir) {
		File file = new File(path);
		if (!file.exists()) {
			//jxLog.logger.debug(path);
			String[] ds = path.split("/");
			int len = ds.length;
			if (len == 0) return;
			String dir = "";
			int dirlen = pathisdir ? len : len - 1;
			for (int i = 1; i < dirlen; i++) {
				String sd = ds[i];
				if (sd != null && sd != "") {
					dir += "/" + sd;
					//jxLog.logger.debug("Dir:" + dir);
					file = new File(dir);
					if (!file.exists()) {
						//jxLog.logger.debug("mkdir:" + dir);
						file.mkdirs();
					}
				}
			}
		}
	}

	public static String getIPAddress(String interfaceName) {
		//jxLog.logger.debug("getIPAddress");
		// 根据网卡取本机配置的IP
		Enumeration<NetworkInterface> netInterfaces = null;
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = netInterfaces.nextElement();
				//jxLog.logger.debug("interfaceName:"+ni.getName());
				if (interfaceName.compareTo(ni.getName()) == 0) {
					Enumeration<InetAddress> ips = ni.getInetAddresses();
					while (ips.hasMoreElements()) {
						String ip = ips.nextElement().getHostAddress();
						//jxLog.logger.debug("IP:"+ip);
						String[] ss = ip.split("\\.");
						//jxLog.logger.debug("length:"+ss.length);
						if (ss.length == 4)
							return ip;
					}
				}
			}
		} catch (Exception e) {
			//jxLog.error(e);
		}
		return null;
	}
}