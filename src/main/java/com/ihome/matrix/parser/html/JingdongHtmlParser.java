package com.ihome.matrix.parser.html;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;
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
 * 解析京东商品页面
 * 
 * @author sihai
 * 
 */
public class JingdongHtmlParser extends AbstractHtmlParser {

	private static final Log logger = LogFactory.getLog(JingdongHtmlParser.class);

	private static final Pattern JINGDONG_ITEM_URL_PATTERN = Pattern.compile("^http://www.360buy.com/product/(\\S)*\\.html(\\S)*");

	@Override
	protected boolean accept(String strURL) {
		return JINGDONG_ITEM_URL_PATTERN.matcher(strURL).matches();
	}

	@Override
	protected ItemDO doParse(String strURL, String html, String charset) {
		return parseJingdongItem(strURL, html, charset);
	}

	/**
	 * 
	 * @param content
	 * @return
	 */
	public ItemDO parseJingdongItem(String strURL, String html, String charset) {
		ItemDO item = new ItemDO();
		item.setPlatform(PlatformEnum.PLATFORM_360_BUY.getValue());
		item.setShop(MatrixBridge.getFixedShop(PlatformEnum.PLATFORM_360_BUY));
		item.setDetailURL(strURL);
		item.setStuffStatus(StuffStatusEnum.STUFF_NEW.getValue());
		item.setNumber(-1L);
		item.setStatus(ItemStatusEnum.ITEM_STATUS_ON_SALE.getValue());
		item.setFreightFeePayer(FreightFeePayerEnum.FREIGHT_FEE_PALYER_SELLER.getValue());
		item.setIsDeleted(false);
		item.setGmtCreate(new Date());
		item.setGmtModified(item.getGmtCreate());

		//write2File("/home/sihai/ihome/jingdong.html", html, charset);
		
		Document document = Jsoup.parse(html);
		Elements es = null;
		
		// ItemId
		es = document.select("#summary-market > div.dd > span");
		if(!es.isEmpty()) {
			item.setItemId(es.first().html());
		}
		
		// category and name
		List<String> categoryPath = new ArrayList<String>(3);
		es = document.select("div.breadcrumb > strong > a");
		if(!es.isEmpty()) {
			item.setName(es.first().html());
		}
		es = document.select("div.breadcrumb > span > a");
		if(!es.isEmpty()) {
			int length = es.size();
			int i = 0;
			for (Element e : es) {
				if (++i == length) {
					item.setName(e.html());
				} else {
					categoryPath.add(e.html());
				}
			}
		}
		
		// 生成类目树
		CategoryDO category = generateCategoryTree(PlatformEnum.PLATFORM_360_BUY, categoryPath);
		item.setCategory(category);

		// price
		es = document.select("li#summary-price > div.dd > strong.p-price > img");
		if(!es.isEmpty()) {
			item.setPrice(discernJingdongPrice(es.first().attr("src")));
		}

		// photo
		es = document.select("div#spec-n1 > img");
		if(!es.isEmpty()) {
			item.setLogoURL(generatePhoto(es.first().attr("src")));
		}
		
		// gifts
		// 取自 http://jprice.360buy.com/pageadword/itemId-1-1.html
		/*es = document.select("li#summary-gifts > div.dd > div.li-img");
		if(!es.isEmpty()) {
			List<Map<String, Object>> gifts = new ArrayList<Map<String, Object>>();
			for(Element e : es) {
				Map<String, Object> gift = new HashMap<String, Object>();
				gift.put("name", e.child(0).child(0).html());
				gift.put("photo", e.child(0).child(0).attr("src"));
				gift.put("number", e.child(1).html());
				gifts.add(gift);
			}
			item.setGifts(JSONObject.fromObject(gifts).toString());
		}*/

		return item;
	}

	private Double discernJingdongPrice(String photoURL) {
		System.out.println(String.format("Price photo url:%s", photoURL));
		File tmpFile = null;
		try {
			tmpFile = URLUtil.fetchFile(photoURL, ".png");
			String result = instance.doOCR(tmpFile);
			if (result.startsWith("Y")) {
				return Double.valueOf(result.substring("Y".length()));
			} else if (result.startsWith("51")) {
				return Double.valueOf(result.substring("51".length()));
			} else {
				return Double.valueOf(result);
			}
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