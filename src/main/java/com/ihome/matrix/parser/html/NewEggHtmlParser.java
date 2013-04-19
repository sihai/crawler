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
 * 解析新蛋商品页面
 * 
 * @author sihai
 * 
 */
public class NewEggHtmlParser extends AbstractHtmlParser {

	private static final Log logger = LogFactory.getLog(NewEggHtmlParser.class);

	private static final Pattern NEW_EGG_ITEM_URL_PATTERN = Pattern.compile("^http://www.newegg.com.cn/Product/(\\S)*.htm(\\S)*");

	@Override
	protected boolean accept(String strURL) {
		return NEW_EGG_ITEM_URL_PATTERN.matcher(strURL).matches();
	}

	@Override
	protected ItemDO doParse(String strURL, byte[] content, String charset) {
		return parseItem(strURL, content, charset);
	}

	/**
	 * 
	 * @param strURL
	 * @param content
	 * @param charset
	 * @return
	 */
	public ItemDO parseItem(String strURL, byte[] content, String charset) {
		
		ItemDO item = new ItemDO();
		item.setPlatform(PlatformEnum.PLATFORM_NEW_EGG.getValue());
		item.setShop(MatrixBridge.getFixedShop(PlatformEnum.PLATFORM_NEW_EGG));
		item.setDetailURL(strURL);
		//item.setStuffStatus(StuffStatusEnum.STUFF_NEW.getValue());
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
		//write2File("/home/sihai/ihome/newegg.html", html, charset);
		
		Document document = Jsoup.parse(html);
		Elements es = null;
		
		// itemId
		int index0 = strURL.indexOf("/Product/");
		if (-1 != index0) {
			int index1 = strURL.indexOf(".htm",
					index0 + "/Product/".length());
			if (-1 != index1) {
				item.setItemId(strURL.substring(
						index0 + "/Product/".length(), index1));
			}
		}

		// itemName
		es = document.select("div#proCtner > div.proHeader > h1");
		if(!es.isEmpty()) {
			item.setName(es.first().html());
		}
		
		// category
		List<String> categoryPath = new ArrayList<String>(3);
		es = document.select("div#crumb > div.inner > a");
		if(!es.isEmpty()) {
			int length = es.size();
			int i = 0;
			for (Element e : es) {
				if (++i != 1 && i != length) {
					categoryPath.add(e.html());
				}
			}
		}

		// 生成类目树
		CategoryDO category = generateCategoryTree(PlatformEnum.PLATFORM_NEW_EGG.getValue(), categoryPath);
		item.setCategory(category);

		// itemPrice
		// 这就是最优价格
		es = document.select("dd.neweggPrice > p > img");
		if(!es.isEmpty()) {
			item.setPrice(discernNewEggPrice(es.first().attr("src")));
		}
		
		// photo
		es = document.select("dd#thumbnails1 > div.noExtra > ul.moveable > li > a");
		if (!es.isEmpty()) {
			item.setLogoURL(generatePhoto(strURL, es.first().attr("ref2")));
		}

		return item;
	}

	/**
	 * 
	 * @param photoURL
	 * @return
	 */
	private Double discernNewEggPrice(String photoURL) {
		System.out.println(String.format("Price photo url:%s", photoURL));
		File tmpFile = null;
		try {
			tmpFile = URLUtil.fetchFile(photoURL, ".gif");
			String result = instance.doOCR(tmpFile);
			return Double.valueOf(result);
		} catch (TesseractException e) {
			logger.error(e);
		} finally {
			if (null != tmpFile) {
				tmpFile.delete();
			}
		}
		return null;
	}
}