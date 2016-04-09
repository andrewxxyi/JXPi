package cn.ijingxi.ServerCommon.httpServer;

import cn.ijingxi.common.util.jxLog;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.util.ByteSequence;

import java.io.*;
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class jxHttpMime {

	/*
	private static String exp_Boundary = ".+boundary=(.+)$";
	private static Pattern regBoundary = Pattern.compile(exp_Boundary);

	//private static String exp_MultiPart = "%1$s\n(.+)\n(.+)\n\n(.+)\n%1$s";
	private static String exp_MultiPart = "(%1$s)(.+)(%1$s)";


	*/

	private static String exp_fileName = ".+filename=\"(.+)\"$";
	private static Pattern regFileName = Pattern.compile(exp_fileName);

	private static String contentType = "Content-Type";

	public String fileName=null;
	public String fullFileName=null;
	private String uploadDir=null;

	public jxHttpMime(final HttpRequest request,String Dir) throws IOException, MimeException {
		uploadDir=Dir;
		if (request instanceof HttpEntityEnclosingRequest) {
			jxLog.logger.debug("request instanceof HttpEntityEnclosingRequest");
			HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();

			//String boundary=null;
			String ct=null;
			Header[] hs = request.getAllHeaders();
			for(Header h:hs){
				if(h.getName().equals(contentType)) {
					ct = h.getValue();
					break;
				}
				/*
				String v=h.getValue();
				utils.P("head:"+h.getName(), v);
				Matcher mb = regBoundary.matcher(v);
				if(mb.find()) {
					boundary=mb.group(1);
					ct=v;
					utils.P("boundary", boundary);
					break;
				}
				*/
			}
/*
			DefaultBodyDescriptorBuilder dbb=new DefaultBodyDescriptorBuilder();
			dbb.addField(new RawField(FieldName.CONTENT_TYPE, ct));
			BodyDescriptor bb = dbb.build();
			utils.P("Build boundary", bb.getBoundary());
*/


			//MimeConfig cfg=new MimeConfig();
			//cfg.setStrictParsing(true);
			MimeStreamParser parser=new MimeStreamParser();
			//parser.setContentDecoding(true);
			parser.setContentHandler(new ContentHandler() {
				@Override
				public void startMessage() throws MimeException {
					jxLog.logger.debug("parser startMessage");
				}

				@Override
				public void endMessage() throws MimeException {
					jxLog.logger.debug("parser endMessage");
				}

				@Override
				public void startBodyPart() throws MimeException {
					jxLog.logger.debug("parser startBodyPart");
				}

				@Override
				public void endBodyPart() throws MimeException {
					jxLog.logger.debug("parser endBodyPart");
				}

				@Override
				public void startHeader() throws MimeException {
					jxLog.logger.debug("parser startHeader");
				}

				@Override
				public void field(Field field) throws MimeException {
					jxLog.logger.debug("parser field");
					ByteSequence bs = field.getRaw();
					byte[] ba = bs.toByteArray();
					try {
						String header = new String(ba, "utf-8");
						jxLog.logger.debug("Head:"+header);
						Matcher mfn = regFileName.matcher(header);
						if (mfn.find()) {
							fileName = mfn.group(1);
							jxLog.logger.debug("FileName:"+fileName);
						}
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void endHeader() throws MimeException {
					jxLog.logger.debug("parser endHeader");
				}

				@Override
				public void preamble(InputStream inputStream) throws MimeException, IOException {
					jxLog.logger.debug("parser preamble");
				}

				@Override
				public void epilogue(InputStream inputStream) throws MimeException, IOException {
					jxLog.logger.debug("parser epilogue");
				}

				@Override
				public void startMultipart(BodyDescriptor bodyDescriptor) throws MimeException {
					jxLog.logger.debug("parser startMultipart");
				}

				@Override
				public void endMultipart() throws MimeException {
					jxLog.logger.debug("parser endMultipart");
				}

				@Override
				public void body(BodyDescriptor bodyDescriptor, InputStream inputStream) throws MimeException, IOException {
					jxLog.logger.debug("parser body");
					jxLog.logger.debug("body:"+bodyDescriptor.getBoundary());
					fullFileName=uploadDir + fileName;
					FileOutputStream fos = new FileOutputStream(fullFileName);
					int byteCount = 0;
					byte[] b = new byte[1024];
					while ((byteCount = inputStream.read(b)) != -1) {
						fos.write(b, 0, byteCount);
					}
					inputStream.close();
					fos.close();
				}

				@Override
				public void raw(InputStream inputStream) throws MimeException, IOException {
					jxLog.logger.debug("parser raw");
				}
			});



			/*
			byte[] entityContent = EntityUtils.toByteArray(entity);
			InputStream ecbs=new ByteArrayInputStream(entityContent);
			parser.parse(ecbs);
			*/
			//重新生成一个头，用于消息分析
			String input="Content-Type:"+ct+"\r\n\r\n";
			InputStream ish = new ByteArrayInputStream(input.getBytes());
			InputStream isc = entity.getContent();
			Vector<InputStream> vec = new Vector<InputStream>();
			vec.add(ish);
			vec.add(isc);
			Enumeration en = vec.elements();
			SequenceInputStream sis = new SequenceInputStream(en);
			parser.parse(sis);

		}


	}
	
}
