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
 * 解析一号店商品页面
 * 
 * @author sihai
 * 
 */
public class No1ShopHtmlParser extends AbstractHtmlParser {

	private static final Log logger = LogFactory
			.getLog(No1ShopHtmlParser.class);

	private static final String NO1_SHOP_ITEM_NAME_XPATH = "//*[@id='productMainName']";
	private static final String NO1_SHOP_ITEM_CATEGORY_XPATH = "/xmlns:HTML/xmlns:BODY/xmlns:DIV[5]/xmlns:DIV[1]/xmlns:SPAN/xmlns:A";
	private static final String NO1_SHOP_ITEM_PHOTO_XPATH = "//*[@id='productImg']/@src";
	private static final String NO1_SHOP_ITEM_PRICE_XPATH = "//*[@id='nonMemberPrice']/xmlns:STRONG";
	private static final String NO1_SHOP_ITEM_PROMOTION_PRICE_XPATH = "//*[@id='proMainInfo']/xmlns:DIV[2]/xmlns:DIV[1]/xmlns:DL/xmlns:DD[5]/xmlns:p[1]/xmlns:IMG/@src";

	private static final Pattern NO1_SHOP_ITEM_URL_PATTERN = Pattern
			.compile("^http://www.yihaodian.com/product/(\\S)*");

	@Override
	protected boolean accept(String strURL) {
		return NO1_SHOP_ITEM_URL_PATTERN.matcher(strURL).matches();
	}

	@Override
	protected ItemDO doParse(String strURL, String html) {
		return parseNo1ShopItem(strURL, html);
	}

	/**
	 * 
	 * @param strURL
	 * @param html
	 * @return
	 */
	public ItemDO parseNo1ShopItem(String strURL, String html) {
		try {
			ItemDO item = new ItemDO();
			item.setPlatform(PlatformEnum.PLATFORM_NO_1_SHOP.getValue());
			item.setShop(MatrixBridge
					.getFixedShop(PlatformEnum.PLATFORM_NO_1_SHOP));
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
			/*
			 * XPath xpath = new DefaultXPath(SUNING_ITEM_ID_XPATH);
			 * xpath.setNamespaceContext(context); Object node =
			 * xpath.selectSingleNode(document); if(null != node) {
			 * item.setName(((org.dom4j.Node)node).getText()); }
			 */

			int index0 = strURL.indexOf("/product/");
			if (-1 != index0) {
				item.setItemId(strURL.substring(index0 + "/product/".length(),
						strURL.length()));
			}

			// itemName
			XPath xpath = new DefaultXPath(NO1_SHOP_ITEM_NAME_XPATH);
			xpath.setNamespaceContext(context);
			Object node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setName(((org.dom4j.Node) node).getText());
			}

			// category
			List<String> categoryPath = new ArrayList<String>(3);
			xpath = new DefaultXPath(NO1_SHOP_ITEM_CATEGORY_XPATH);
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
					PlatformEnum.PLATFORM_NO_1_SHOP, categoryPath);
			item.setCategory(category);

			// itemPrice
			xpath = new DefaultXPath(NO1_SHOP_ITEM_PRICE_XPATH);
			xpath.setNamespaceContext(context);
			node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setPrice(Double.valueOf((((org.dom4j.Node) node).getText())));
			}

			// TODO 优惠价格

			// photo
			xpath = new DefaultXPath(NO1_SHOP_ITEM_PHOTO_XPATH);
			xpath.setNamespaceContext(context);
			node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setLogoURL(generatePhoto(((org.dom4j.Attribute) node)
						.getValue()));
			}

			return item;
		} catch (IOException e) {
			logger.error(e);
		} catch (SAXException e) {
			logger.error(e);
		}

		return null;
	}
}