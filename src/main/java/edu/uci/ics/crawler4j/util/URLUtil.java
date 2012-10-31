/**
 * 
 */
package edu.uci.ics.crawler4j.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.iacrqq.util.StringUtil;

/**
 * 
 * @author sihai
 * 
 */
public class URLUtil {

	private static final Log logger = LogFactory.getLog(URLUtil.class);

	public static String getParameter(String strURL, String parameter) {

		if (StringUtil.isEmpty(strURL)) {
			return null;
		}
		try {
			URL url = new URL(strURL);
			String queryString = url.getQuery();
			if (StringUtil.isEmpty(strURL)) {
				return null;
			}
			if (StringUtil.isEmpty(strURL)) {
				return null;
			}
			String[] kvs = queryString.split("&");
			String[] kv = null;
			for (String s : kvs) {
				if (StringUtil.isEmpty(s)) {
					continue;
				} else {
					kv = s.split("=");
					if (kv.length == 2) {
						if (kv[0].equals(parameter)) {
							return kv[1];
						}
					}
				}
			}
		} catch (MalformedURLException e) {
			logger.error(String.format("Wrong url:%s", strURL), e);
		}

		return null;
	}

	/**
	 * Fetch file from network, create tmp file for it, please delete it after
	 * no need it
	 * 
	 * @param url
	 * @param suffix
	 * @return
	 */
	public static File getFile(String strURL, String suffix) {

		if (StringUtil.isEmpty(strURL)) {
			return null;
		}

		HttpURLConnection connection = null;
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		try {
			URL url = new URL(strURL);
			String fileName = url.getFile();
			if (StringUtil.isEmpty(fileName)) {
				return null;
			}
			fileName = fileName.substring(fileName.lastIndexOf("/")
					+ "/".length());
			connection = (HttpURLConnection) url.openConnection();
			in = new BufferedInputStream(connection.getInputStream());
			File tmpFile = File.createTempFile(fileName, suffix);
			tmpFile.deleteOnExit();
			out = new BufferedOutputStream(new FileOutputStream(tmpFile));
			byte[] buffer = new byte[4096];
			int count = 0;
			while (-1 != (count = in.read(buffer, 0, buffer.length))) {
				out.write(buffer, 0, count);
			}
			buffer = null;
			out.flush();
			return tmpFile;
		} catch (MalformedURLException e) {
			logger.error(String.format("Wrong url:%s", strURL), e);
		} catch (IOException e) {
			logger.error(String.format(
					"Read url:%s or write content to file failed: ", strURL), e);
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
			if (null != connection) {
				connection.disconnect();
			}
			if (null != out) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}

		return null;
	}
}
