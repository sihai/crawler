/**
 * 
 */
package com.ihome.matrix.parser.html;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Date;
import java.util.List;

import net.sourceforge.tess4j.Tesseract;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.io.HTMLWriter;
import org.dom4j.io.OutputFormat;

import com.ihome.matrix.bridge.MatrixBridge;
import com.ihome.matrix.domain.CategoryDO;
import com.ihome.matrix.domain.ItemDO;
import com.ihome.matrix.enums.CategoryStatusEnum;
import com.ihome.matrix.enums.PlatformEnum;

import edu.uci.ics.crawler4j.util.URLUtil;

/**
 * 
 * @author sihai
 *
 */
public abstract class AbstractHtmlParser implements HtmlParser {

	public static final Log logger = LogFactory.getLog(AbstractHtmlParser.class);
	
	// OCR
	protected static Tesseract instance;
	
	static {
		  instance = Tesseract.getInstance();  // JNA Interface Mapping
		  //instance.setLanguage("chi_sim");
		  //instance.setHocr(true);
		  instance.setPageSegMode(7);
	}

	@Override
	public void parse(String strURL, String html, String charset) {
		if(accept(strURL)) {
			ItemDO item = doParse(strURL, html, charset);
			//System.out.println(item);
			if(null != item) {
				MatrixBridge.sync(item);
			}
		}
	}
	
	/**
	 * 
	 * @param strURL
	 * @return
	 */
	protected abstract boolean accept(String strURL);
	
	/**
	 * 
	 * @param strURL
	 * @param html
	 * @param charset
	 * @return
	 */
	protected abstract ItemDO doParse(String strURL, String html, String charset);
	
	/**
	 * 
	 * @param src
	 * @return
	 */
	protected String generatePhoto(String src) {
		// Do nothing
		return src;
	}
	
	/**
	 * 
	 * @param platform
	 * @param categoryPath
	 * @return
	 */
	protected static CategoryDO generateCategoryTree(PlatformEnum platform, List<String> categoryPath) {
		  CategoryDO parent = null;
		  CategoryDO cat = null;
		  for(String catName : categoryPath) {
			  cat = new CategoryDO();
			  cat.setName(catName);
			  cat.setDescription(cat.getName());
			  cat.setPlatform(platform.getValue());
			  cat.setStatus(CategoryStatusEnum.NORMAL.getValue());
			  cat.setRank(0);
			  cat.setIsDeleted(false);
			  cat.setGmtCreate(new Date());
			  cat.setGmtModified(cat.getGmtCreate());
			  cat.setParent(parent);
			  parent = cat;
		  }
		  return cat;
	}
	
	/**
	 * 
	 * @param fileName
	 * @param html
	 */
	protected static void write2File(String fileName, String html) {
		write2File(fileName, html, URLUtil.DEFAULT_CHARSET);
	}
	
	/**
	 * 
	 * @param fileName
	 * @param html
	 * @param charset
	 */
	protected static void write2File(String fileName, String html, String charset) {
		 Writer writer = null;
		  try {
			  writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), charset));
			  writer.write(html);
			  writer.flush();
		  } catch (IOException e) {
			  e.printStackTrace();
			  logger.error(e);
		  } finally{
			  if(null != writer) {
				  try {
					  writer.close();
				  } catch (IOException e) {
					  e.printStackTrace();
					  logger.error(e);
				  }
			  }
		  }
	}
	
	/**
	 * 
	 * @param fileName
	 * @param document
	 * @param charset
	 */
	protected static void write2File(String fileName, Document document, String charset) {
		
		try {
			OutputFormat format = new OutputFormat("  ", true, charset);
			format.setXHTML(true);
			format.setExpandEmptyElements(true);
			//format.setOmitEncoding(true);
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			HTMLWriter htmlWriter = new HTMLWriter(bout, format);
			htmlWriter.setEscapeText(false);
			htmlWriter.write(document);
			
			write2File(fileName, bout.toString(), charset);
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	 /** 
     * 判断字符串的编码 
     * 
     * @param str 
     * @return 
     */  
    public static String getEncoding(String str) {  
        String encode = "GB2312";  
        try {  
            if (str.equals(new String(str.getBytes(encode), encode))) {  
                String s = encode;  
                return s;  
            }  
        } catch (Exception exception) {  
        }  
        encode = "ISO-8859-1";  
        try {  
            if (str.equals(new String(str.getBytes(encode), encode))) {  
                String s1 = encode;  
                return s1;  
            }  
        } catch (Exception exception1) {  
        }  
        encode = "UTF-8";  
        try {  
            if (str.equals(new String(str.getBytes(encode), encode))) {  
                String s2 = encode;  
                return s2;  
            }  
        } catch (Exception exception2) {  
        }  
        encode = "GBK";  
        try {  
            if (str.equals(new String(str.getBytes(encode), encode))) {  
                String s3 = encode;  
                return s3;  
            }  
        } catch (Exception exception3) {  
        }  
        return "";  
    }
}
