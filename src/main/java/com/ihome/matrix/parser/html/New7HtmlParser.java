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
 * 解析新七天商品页面
 * 
 * @author sihai
 * 
 */
public class New7HtmlParser extends AbstractHtmlParser {

	private static final Log logger = LogFactory.getLog(New7HtmlParser.class);

	private static final String NEW7_ITEM_ID_XPATH = "/xmlns:HTML/xmlns:BODY/xmlns:DIV[4]/xmlns:DIV[2]/xmlns:DL[1]/xmlns:DD";
	private static final String NEW7_ITEM_NAME_XPATH = "/xmlns:HTML/xmlns:BODY/xmlns:DIV[3]/text()";
	private static final String NEW7_ITEM_CATEGORY_XPATH = "/xmlns:HTML/xmlns:BODY/xmlns:DIV[3]/xmlns:A";
	private static final String NEW7_ITEM_PHOTO_XPATH = "//*[@id='list']/xmlns:DIV[1]/xmlns:A/xmlns:IMG/@jqimg";
	private static final String NEW7_ITEM_PRICE_XPATH = "/xmlns:HTML/xmlns:BODY/xmlns:DIV[4]/xmlns:DIV[2]/xmlns:DL[2]/xmlns:DD/text()";
	private static final String NEW7_ITEM_PROMOTION_PRICE_XPATH = "//*[@id='dom_sale_price']";
	private static final String NEW7_ITEM_GIFT_XPATH = "";

	private static final Pattern NEW7_ITEM_URL_PATTERN = Pattern
			.compile("^http://www.new7.com/product/(\\S)*.html(\\S)*");

	@Override
	protected boolean accept(String strURL) {
		return NEW7_ITEM_URL_PATTERN.matcher(strURL).matches();
	}

	@Override
	protected ItemDO doParse(String strURL, String html) {
		return parseNew7Item(strURL, html);
	}

	/**
	 * 
	 * @param content
	 * @return
	 */
	public ItemDO parseNew7Item(String strURL, String html) {
		try {
			ItemDO item = new ItemDO();
			item.setPlatform(PlatformEnum.PLATFORM_NEW7.getValue());
			item.setShop(MatrixBridge.getFixedShop(PlatformEnum.PLATFORM_NEW7));
			item.setDetailURL(strURL);
			item.setStuffStatus(StuffStatusEnum.STUFF_NEW.getValue());
			item.setNumber(-1L);
			item.setStatus(ItemStatusEnum.ITEM_STATUS_ON_SALE.getValue());
			item.setFreightFeePayer(FreightFeePayerEnum.FREIGHT_FEE_PALYER_SELLER
					.getValue());
			item.setIsDeleted(false);

			// System.out.println(content.toString());
			/*
			 * Writer writer = null; try { writer = new BufferedWriter(new
			 * FileWriter("/home/sihai/test.html"));
			 * writer.write(content.toString()); writer.flush(); } catch
			 * (IOException e) { e.printStackTrace(); } finally{ if(null !=
			 * writer) { try { writer.close(); } catch (IOException e) {
			 * e.printStackTrace(); } } }
			 */

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
			XPath xpath = new DefaultXPath(NEW7_ITEM_ID_XPATH);
			xpath.setNamespaceContext(context);
			Object node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setItemId(((org.dom4j.Node) node).getText());
			}

			// itemName
			xpath = new DefaultXPath(NEW7_ITEM_NAME_XPATH);
			xpath.setNamespaceContext(context);
			node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setName(((org.dom4j.Node) node).getText());
			}

			// category
			List<String> categoryPath = new ArrayList<String>(3);
			xpath = new DefaultXPath(NEW7_ITEM_CATEGORY_XPATH);
			xpath.setNamespaceContext(context);
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
					PlatformEnum.PLATFORM_NEW7, categoryPath);
			item.setCategory(category);

			// itemPrice
			xpath = new DefaultXPath(NEW7_ITEM_PRICE_XPATH);
			xpath.setNamespaceContext(context);
			node = xpath.selectSingleNode(document);
			if (null != node) {
				String str = ((org.dom4j.Node) node).getText();
				item.setPrice(Double.valueOf(str.replaceAll(",", "")));
			}

			// TODO 优惠价格, 如果有优惠价格, 将会覆盖上面的价格 NO Need
			/*
			 * xpath = new DefaultXPath(EFEIHU_ITEM_PROMOTION_PRICE_XPATH);
			 * xpath.setNamespaceContext(context); node =
			 * xpath.selectSingleNode(document); if(null != node) {
			 * item.setPrice
			 * (Double.valueOf(((org.dom4j.Node)node).getText().substring
			 * ("￥".length()).replaceAll(",", ""))); }
			 */

			// TODO photo
			xpath = new DefaultXPath(NEW7_ITEM_PHOTO_XPATH);
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
}