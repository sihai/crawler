package com.ihome.matrix.parser.html;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyberneko.html.parsers.DOMParser;
import org.dom4j.XPath;
import org.dom4j.io.DOMReader;
import org.dom4j.xpath.DefaultXPath;
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
 * 解析京东商品页面
 * 
 * @author sihai
 * 
 */
public class Five1BuyHtmlParser extends AbstractHtmlParser {

	private static final Log logger = LogFactory
			.getLog(Five1BuyHtmlParser.class);

	private static final String FIVE_I_BUY_ITEM_ID_XPATH = "//*[@id='container']/DIV[2]/DIV[2]/H1/SPAN";
	private static final String FIVE_1_BUY_ITEM_NAME_XPATH = "//*[@id='container']/DIV[2]/DIV[2]/H1";
	private static final String FIVE_1_BUY_ITEM_CATEGORY_XPATH = "//*[@id='container']/DIV[1]/A";
	private static final String FIVE_1_BUY_ITEM_PHOTO_XPATH = "//*[@id='smallImage']/@src";
	private static final String FIVE_1_BUY_ITEM_PRICE_XPATH = "//*[@id='goods_detail_mate']/LI[2]/STRONG";
	private static final String FIVE_1_BUY_ITEM_PROMOTION_PRICE_XPATH = "//*[@id='proMainInfo']/xmlns:DIV[2]/xmlns:DIV[1]/xmlns:DL/xmlns:DD[5]/xmlns:p[1]/xmlns:IMG/@src";
	private static final String FIVE_1_BUY_ITEM_GIFT_XPATH = "";

	private static final Pattern FIVE1_BUY_ITEM_URL_PATTERN = Pattern
			.compile("^http://item.51buy.com/item-(\\S)*.html(\\S)*");

	@Override
	protected boolean accept(String strURL) {
		return FIVE1_BUY_ITEM_URL_PATTERN.matcher(strURL).matches();
	}

	@Override
	protected ItemDO doParse(String strURL, String html) {
		return parseFive1BuyItem(strURL, html);
	}

	/**
	 * 
	 * @param strURL
	 * @param html
	 * @return
	 */
	public ItemDO parseFive1BuyItem(String strURL, String html) {
		try {
			ItemDO item = new ItemDO();
			item.setPlatform(PlatformEnum.PLATFORM_51_BUY.getValue());
			item.setShop(MatrixBridge
					.getFixedShop(PlatformEnum.PLATFORM_51_BUY));
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

			// itemId
			XPath xpath = new DefaultXPath(FIVE_I_BUY_ITEM_ID_XPATH);
			Object node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setItemId(((org.dom4j.Node) node).getText().substring(
						"产品编号：".length()));
			}

			// itemName
			xpath = new DefaultXPath(FIVE_1_BUY_ITEM_NAME_XPATH);
			node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setName(((org.dom4j.Node) node).getText());
			}

			// category
			List<String> categoryPath = new ArrayList<String>(3);
			xpath = new DefaultXPath(FIVE_1_BUY_ITEM_CATEGORY_XPATH);
			node = xpath.selectNodes(document);
			if (null != node) {
				int length = ((List<org.dom4j.Node>) node).size();
				int i = 0;
				for (org.dom4j.Node n : (List<org.dom4j.Node>) node) {
					if (++i != 1) {
						categoryPath.add(n.getText());
					}
				}
			}

			// 生成类目树
			CategoryDO category = generateCategoryTree(
					PlatformEnum.PLATFORM_51_BUY, categoryPath);
			item.setCategory(category);

			// itemPrice
			xpath = new DefaultXPath(FIVE_1_BUY_ITEM_PRICE_XPATH);
			node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setPrice(Double.valueOf((((org.dom4j.Node) node).getText())));
			}

			// TODO 优惠价格

			// TODO photo
			xpath = new DefaultXPath(FIVE_1_BUY_ITEM_PHOTO_XPATH);
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
}
