package com.ihome.matrix.parser.html;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyberneko.html.parsers.DOMParser;
import org.dom4j.XPath;
import org.dom4j.io.DOMReader;
import org.dom4j.xpath.DefaultXPath;
import org.jaxen.SimpleNamespaceContext;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ihome.matrix.bridge.MatrixBridge;
import com.ihome.matrix.domain.CategoryDO;
import com.ihome.matrix.domain.ItemDO;
import com.ihome.matrix.enums.FreightFeePayerEnum;
import com.ihome.matrix.enums.ItemStatusEnum;
import com.ihome.matrix.enums.PlatformEnum;
import com.ihome.matrix.enums.StuffStatusEnum;

/**
 * 解析红孩子商品页面
 * 
 * @author sihai
 * 
 */
public class RedBabyHtmlParser extends AbstractHtmlParser {

	private static final Log logger = LogFactory
			.getLog(RedBabyHtmlParser.class);

	private static final String RED_BABY_ITEM_ID_XPATH = "//*[@id='commonBasicInfo']/xmlns:UL/xmlns:LI[1]";
	private static final String RED_BABY_ITEM_NAME_XPATH = "//*[@id='pName']/xmlns:H1/text()";
	private static final String RED_BABY_ITEM_CATEGORY_XPATH = "/xmlns:HTML/xmlns:BODY/xmlns:DIV[2]/xmlns:DIV[1]/xmlns:DL/xmlns:DD/xmlns:A";
	private static final String RED_BABY_ITEM_PHOTO_XPATH = "//*[@id='jqzoomDiv']/xmlns:IMG/@jqimg";
	private static final String RED_BABY_ITEM_PRICE_XPATH = "//*[@id='price']";
	private static final String RED_BABY_ITEM_PROMOTION_PRICE_XPATH = "//*[@id='specP']/xmlns:DIV[2]/xmlns:SPAN[2]";
	private static final String RED_BABY_ITEM_GIFT_XPATH = "";
	
	private static final Pattern RED_BABY_ITEM_URL_PATTERN = Pattern
			.compile("^http://www.redbaby.com.cn/(\\S)+/(\\S)*.html(\\S)*");

	@Override
	protected boolean accept(String strURL) {
		return RED_BABY_ITEM_URL_PATTERN.matcher(strURL).matches();
	}

	@Override
	protected ItemDO doParse(String strURL, String html) {
		return parseRedBabyItem(strURL, html);
	}
	
	/**
	 * 
	 * @param strURL
	 * @param html
	 * @return
	 */
	public ItemDO parseRedBabyItem(String strURL, String html) {
		try {
			ItemDO item = new ItemDO();
			item.setPlatform(PlatformEnum.PLATFORM_RED_BABY.getValue());
			item.setShop(MatrixBridge
					.getFixedShop(PlatformEnum.PLATFORM_RED_BABY));
			item.setDetailURL(strURL);
			item.setStuffStatus(StuffStatusEnum.STUFF_NEW.getValue());
			item.setNumber(-1L);
			item.setStatus(ItemStatusEnum.ITEM_STATUS_ON_SALE.getValue());
			item.setFreightFeePayer(FreightFeePayerEnum.FREIGHT_FEE_PALYER_SELLER
					.getValue());
			item.setIsDeleted(false);

			// System.out.println(content.toString());
			InputSource input = new InputSource(new ByteArrayInputStream(
					html.getBytes()));
			DOMParser parser = new DOMParser();
			parser.parse(input);
			org.w3c.dom.Document w3cDoc = parser.getDocument();
			DOMReader domReader = new DOMReader();
			org.dom4j.Document document = domReader.read(w3cDoc);

			Map<String, String> nameSpaces = new HashMap<String, String>();
			nameSpaces.put("xmlns", "http://www.w3.org/1999/xhtml");
			SimpleNamespaceContext context = new SimpleNamespaceContext(
					nameSpaces);

			// itemId
			XPath xpath = new DefaultXPath(RED_BABY_ITEM_ID_XPATH);
			xpath.setNamespaceContext(context);
			Object node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setItemId(((org.dom4j.Node) node).getText());
			}

			// itemName
			xpath = new DefaultXPath(RED_BABY_ITEM_NAME_XPATH);
			xpath.setNamespaceContext(context);
			node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setName(((org.dom4j.Node) node).getText());
			}

			// category
			List<String> categoryPath = new ArrayList<String>(3);
			xpath = new DefaultXPath(RED_BABY_ITEM_CATEGORY_XPATH);
			xpath.setNamespaceContext(context);
			node = xpath.selectNodes(document);
			if (null != node) {
				int length = ((List<org.dom4j.Node>) node).size();
				int i = 0;
				for (org.dom4j.Node n : (List<org.dom4j.Node>) node) {
					if (++i != 1 && i != length) {
						categoryPath.add(n.getText());
					}
				}
			}

			// 生成类目树
			CategoryDO category = generateCategoryTree(
					PlatformEnum.PLATFORM_RED_BABY, categoryPath);
			item.setCategory(category);

			// itemPrice
			// get price
			item.setPrice(getRedBabyPrice(item.getItemId()));

			xpath = new DefaultXPath(RED_BABY_ITEM_PRICE_XPATH);
			xpath.setNamespaceContext(context);
			node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setPrice(Double.valueOf(((org.dom4j.Node) node).getText()
						.replaceAll(",", "")));
			}

			// TODO 优惠价格, 如果有优惠价格, 将会覆盖上面的价格 NO Need
			xpath = new DefaultXPath(RED_BABY_ITEM_PROMOTION_PRICE_XPATH);
			xpath.setNamespaceContext(context);
			node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setPrice(Double.valueOf(((org.dom4j.Node) node).getText()
						.replaceAll(",", "")));
			}

			// TODO photo
			xpath = new DefaultXPath(RED_BABY_ITEM_PHOTO_XPATH);
			xpath.setNamespaceContext(context);
			node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setLogoURL(generatePhoto(((org.dom4j.Attribute) node)
						.getValue()));
			}

			// TODO 赠品

			return item;
		} catch (IOException e) {
			logger.error(e);
		} catch (SAXException e) {
			logger.error(e);
		}

		return null;
	}

	public static Double getRedBabyPrice(String itemId) {
		/*
		 * try { Double price = null; String strURL =
		 * "http://plus.redbaby.com.cn/plus/product/getPriceInfo?pId=" + itemId;
		 * Protocol protocol = protocolFactory.getProtocol(strURL);
		 * ProtocolOutput output = protocol.getProtocolOutput(new Text(strURL),
		 * new CrawlDatum()); String html = output.getContent().toString();
		 * 
		 * // 尝试取特价(会员价和特价都有) int index1 =
		 * html.indexOf("<span class='font_red bigRed'>"); int index2 = -1;
		 * if(-1 != index1) { index2 = html.indexOf("</span>", index1 +
		 * "<span class='font_red bigRed'>".length()); if(-1 != index2) { price
		 * = Double.valueOf(html.substring(index1 +
		 * "<span class='font_red bigRed'>".length(), index2).replaceAll(",",
		 * "")); } }
		 * 
		 * // 只有会员价 if(null == price) { index1 =
		 * html.indexOf("<span class='font_red bigRed' id='price'>"); if(-1 !=
		 * index1) { index2 = html.indexOf("</span>", index1 +
		 * "<span class='font_red bigRed' id='price'>".length()); if(-1 !=
		 * index2) { price = Double.valueOf(html.substring(index1 +
		 * "<span class='font_red bigRed' id='price'>".length(),
		 * index2).replaceAll(",", "")); } } }
		 * 
		 * return price; } catch (ProtocolNotFound e) {
		 * logger.error("Not prossiable"); return null; }
		 */
		return null;
	}
}
