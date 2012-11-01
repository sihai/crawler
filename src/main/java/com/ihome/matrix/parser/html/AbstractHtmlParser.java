/**
 * 
 */
package com.ihome.matrix.parser.html;

import java.util.List;

import net.sourceforge.tess4j.Tesseract;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.matrix.bridge.MatrixBridge;
import com.ihome.matrix.domain.CategoryDO;
import com.ihome.matrix.domain.ItemDO;
import com.ihome.matrix.enums.PlatformEnum;

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
	public void parse(String strURL, String html) {
		if(accept(strURL)) {
			ItemDO item = doParse(strURL, html);
			System.out.println(item);
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
	 * @param content
	 */
	protected abstract ItemDO doParse(String strURL, String html);
	
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
			  cat.setPlatform(platform.getValue());
			  cat.setParent(parent);
			  parent = cat;
		  }
		  return cat;
	  }
}
