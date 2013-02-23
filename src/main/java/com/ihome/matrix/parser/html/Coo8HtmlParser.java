package com.ihome.matrix.parser.html;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import net.sourceforge.tess4j.TesseractException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ihome.matrix.bridge.MatrixBridge;
import com.ihome.matrix.domain.CategoryDO;
import com.ihome.matrix.domain.ItemDO;
import com.ihome.matrix.enums.FreightFeePayerEnum;
import com.ihome.matrix.enums.ItemStatusEnum;
import com.ihome.matrix.enums.PlatformEnum;
import com.ihome.matrix.enums.StuffStatusEnum;

import edu.uci.ics.crawler4j.util.URLUtil;

/**
 * 解析库巴商品页面
 * @author sihai
 *
 */
public class Coo8HtmlParser extends AbstractHtmlParser {
	
	private static final Log logger = LogFactory.getLog(Coo8HtmlParser.class);
	
	private static final Pattern COO8_ITEM_URL_PATTERN= Pattern.compile("^http://www.coo8.com/product/(\\S)*\\.html(\\S)*");
	
	@Override
	protected boolean accept(String strURL) {
		return COO8_ITEM_URL_PATTERN.matcher(strURL).matches();
	}

	@Override
	protected ItemDO doParse(String strURL, byte[] content, String charset) {
		return parseItem(strURL, content, charset);
	}
	
	
	private ItemDO parseItem(String strURL, byte[] content, String charset) {
		ItemDO item = new ItemDO();
		item.setPlatform(PlatformEnum.PLATFORM_COO8.getValue());
		item.setShop(MatrixBridge.getFixedShop(PlatformEnum.PLATFORM_COO8));
		item.setDetailURL(strURL);
		item.setStuffStatus(StuffStatusEnum.STUFF_NEW.getValue());
		item.setNumber(-1L);
		item.setStatus(ItemStatusEnum.ITEM_STATUS_ON_SALE.getValue());
		item.setFreightFeePayer(FreightFeePayerEnum.FREIGHT_FEE_PALYER_SELLER.getValue());
		item.setIsDeleted(false);
		item.setGmtCreate(new Date());
		item.setGmtModified(item.getGmtCreate());
		
		String html = null;
		try {
			html = new String(content, charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(String.format("Please make sure the charset of url:%s, try to use charset:%s", strURL, charset));
		}
		//write2File("/home/sihai/ihome/coo8.html", html, charset);
		  
		Document document = Jsoup.parse(html);
		  
		Elements es = null;
		// itemId
		es = document.select("#prod-markprice > dd");
		if(!es.isEmpty()) {
			item.setItemId(es.first().html());
		}
		  
		// category and name
		List<String> categoryPath = new ArrayList<String>(3);
		es = document.select("div.crumb > a");
		if(!es.isEmpty()) {
			int i = 0;
			int length = es.size();
			for(Element e : es) {
				if(++i == length) {
					item.setName(e.html());
				} else {
					categoryPath.add(e.html());
				}
			}
		}
	      
		CategoryDO category = generateCategoryTree(PlatformEnum.PLATFORM_COO8.getValue(), categoryPath);
		item.setCategory(category);
		  
		// price
		es = document.select("#itemimg");
		if(!es.isEmpty()) {
			item.setPrice(discernCoo8Price(es.first().attr("src")));
		}
		  
		// photo
		es = document.select(".thumbItem.thumbCur > a");
		if(!es.isEmpty()) {
			item.setLogoURL(generatePhoto(strURL, es.first().attr("href")));
		}
		  
		return item;
	  }
	  
	  /**
	   * 
	   * @param photoURL
	   * @return
	   */
	  public static Double discernCoo8Price(String photoURL) {
		  System.out.println(String.format("Price photo url:%s", photoURL));
		  File tmpFile = null;
	      try {
	    	  tmpFile = URLUtil.fetchFile(photoURL, ".png");
	          String result = instance.doOCR(tmpFile);
	          return Double.valueOf(result.substring("¥".length()));
	      } catch (TesseractException e) {
	    	  logger.error(e);
	      } finally {
	    	  if(null != tmpFile) {
	    		  tmpFile.delete();
	    	  }
	      }
		  return null;
	  }
}
