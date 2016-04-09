package cn.ijingxi.ServerCommon.httpServer;

import java.io.File;

public interface IGetFile {
	//public InputStream getFileStream(String FileName);
	public File getFile(String FileName);
}
