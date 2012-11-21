package com.ihome.matrix.parser.html;

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

import edu.uci.ics.crawler4j.util.URLUtil;

/**
 * 解析当当商品页面
 * 
 * @author sihai
 * 
 */
public class DangdangHtmlParser extends AbstractHtmlParser {

	private static final Log logger = LogFactory.getLog(DangdangHtmlParser.class);

	private static final String DANGDANG_PRODUCT_PARAMETER_ID = "product_id";

	private static final Pattern DANGDANG_ITEM_URL_PATTERN = Pattern.compile("^http://product.dangdang.com/product.aspx\\?product_id=(\\S)*");

	@Override
	protected boolean accept(String strURL) {
		return DANGDANG_ITEM_URL_PATTERN.matcher(strURL).matches();
	}

	@Override
	protected ItemDO doParse(String strURL, String html, String charset) {
		return parseDangdangItem(strURL, html, charset);
	}
	
	/**
	 * 
	 * @param strURL
	 * @param html
	 * @param charset
	 * @return
	 */
	public ItemDO parseDangdangItem(String strURL, String html, String charset) {
			ItemDO item = new ItemDO();
			item.setPlatform(PlatformEnum.PLATFORM_DANGDANG.getValue());
			item.setShop(MatrixBridge.getFixedShop(PlatformEnum.PLATFORM_DANGDANG));
			item.setDetailURL(strURL);
			item.setStuffStatus(StuffStatusEnum.STUFF_NEW.getValue());
			item.setNumber(-1L);
			item.setStatus(ItemStatusEnum.ITEM_STATUS_ON_SALE.getValue());
			item.setFreightFeePayer(FreightFeePayerEnum.FREIGHT_FEE_PALYER_SELLER.getValue());
			item.setIsDeleted(false);
			item.setGmtCreate(new Date());
			item.setGmtModified(item.getGmtCreate());
			
			//write2File("/home/sihai/ihome/dangdang.html", html, charset);
			
			Document document = Jsoup.parse(html);
			
			Elements es = null;
			
			// itemId
			item.setItemId(URLUtil.getParameter(strURL, DANGDANG_PRODUCT_PARAMETER_ID));
			
			// itemName
			es = document.select("div.h1_title > h1");
			if(!es.isEmpty()) {
				item.setName(es.first().html());
			}
			
			// item category
			List<String> categoryPath = new ArrayList<String>(3);
			es = document.select("[name=__Breadcrumb_b2c] > a");
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
			CategoryDO category = generateCategoryTree(PlatformEnum.PLATFORM_DANGDANG, categoryPath);
			item.setCategory(category);
			
			// itemPrice
			es = document.select("#salePriceTag");
			if (!es.isEmpty()) {
				item.setPrice(Double.valueOf(es.first().html().substring("￥".length()).replaceAll(",", "")));
			}

			// TODO 优惠价格
			
			// photo
			es = document.select("#largePic");
			if (!es.isEmpty()) {
				item.setLogoURL(generatePhoto(es.first().attr("src")));
			}
			return item;
	}
}
