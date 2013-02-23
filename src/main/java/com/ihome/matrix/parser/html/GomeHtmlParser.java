package com.ihome.matrix.parser.html;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

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

/**
 * 解析国美商品页面
 * 
 * @author sihai
 * 
 */
public class GomeHtmlParser extends AbstractHtmlParser {

	private static final Log logger = LogFactory.getLog(GomeHtmlParser.class);

	private static final Pattern GOME_ITEM_URL_PATTERN = Pattern.compile("^http://www.gome.com.cn/ec/homeus/jump/product/(\\S)*.html(\\S)*");

	@Override
	protected boolean accept(String strURL) {
		return GOME_ITEM_URL_PATTERN.matcher(strURL).matches();
	}

	@Override
	protected ItemDO doParse(String strURL, byte[] content, String charset) {
		return parseItem(strURL, content, charset);
	}
	
	/**
	 * 
	 * @param content
	 * @return
	 */
	public ItemDO parseItem(String strURL, byte[] content, String charset) {

		ItemDO item = new ItemDO();
		item.setPlatform(PlatformEnum.PLATFORM_GOME.getValue());
		item.setShop(MatrixBridge.getFixedShop(PlatformEnum.PLATFORM_GOME));
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
		
		//write2File("/home/sihai/ihome/gome.html", html, charset);
		
		Document document = Jsoup.parse(html);
		Elements es = null;
		// itemId
		es = document.select("#sprodNum");
		if(!es.isEmpty()) {
			item.setItemId(es.first().html());
		}

		// itemName
		es = document.select("title");
		if(!es.isEmpty()) {
			item.setName(es.first().html());
		}
		
		// item category
		List<String> categoryPath = new ArrayList<String>(3);
		es = document.select("div.location.bg > a");
		if(!es.isEmpty()) {
			int i = 0;
			for (Element e : es) {
				if (++i == 1) {
					continue;
				} else {
					categoryPath.add(e.html());
				}
			}
		}

		// 生成类目树
		CategoryDO category = generateCategoryTree(PlatformEnum.PLATFORM_GOME.getValue(), categoryPath);
		item.setCategory(category);

		// itemPrice
		es = document.select("div.price > b");
		if(!es.isEmpty()) {
			item.setPrice(Double.valueOf(es.first().html().trim()));
		}
		
		// TODO 优惠价格

		// photo
		es = document.select("img#pic_1");
		if (!es.isEmpty()) {
			item.setLogoURL(generatePhoto(strURL, es.first().attr("bgpic")));
		}

		return item;
	}
}