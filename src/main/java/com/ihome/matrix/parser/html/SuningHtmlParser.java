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
 * 解析苏宁商品页面
 * 
 * @author sihai
 * 
 */
public class SuningHtmlParser extends AbstractHtmlParser {

	private static final Log logger = LogFactory.getLog(SuningHtmlParser.class);

	private static final String SUNING_ITEM_NAME_XPATH = "/xmlns:HTML/xmlns:BODY/xmlns:DIV[5]/xmlns:SPAN";
	private static final String SUNING_ITEM_CATEGORY_XPATH = "/xmlns:HTML/xmlns:BODY/xmlns:DIV[5]/xmlns:A";
	private static final String SUNING_ITEM_PRICE_XPATH = "//*[@id='tellMe']/xmlns:A[1]/@href";
	private static final String SUNING_ITEM_PHOTO_XPATH = "//*[@id='preView_box']/xmlns:DIV/xmlns:UL/xmlns:LI[1]/xmlns:IMG/@src2";

	private static final Pattern SUNING_ITEM_URL_PATTERN = Pattern
			.compile("^http://www.suning.com/emall/prd_(\\S)+_.html(\\S)*");

	@Override
	protected boolean accept(String strURL) {
		return SUNING_ITEM_URL_PATTERN.matcher(strURL).matches();
	}

	@Override
	protected ItemDO doParse(String strURL, String html) {
		return parseSuningItem(strURL, html);
	}

	/**
	 * 
	 * @param strURL
	 * @param html
	 * @return
	 */
	public ItemDO parseSuningItem(String strURL, String html) {
		try {
			ItemDO item = new ItemDO();
			item.setPlatform(PlatformEnum.PLATFORM_SUNING.getValue());
			item.setShop(MatrixBridge
					.getFixedShop(PlatformEnum.PLATFORM_SUNING));
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
			// itemId
			int index0 = strURL.indexOf("/emall/");
			if (-1 != index0) {
				int index1 = strURL.indexOf(".html");
				if (-1 != index1) {
					item.setItemId(strURL.substring(
							index0 + "/emall/".length(), index1));
				}
			}

			// itemName
			XPath xpath = new DefaultXPath(SUNING_ITEM_NAME_XPATH);
			xpath.setNamespaceContext(context);
			Object node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setName(((org.dom4j.Node) node).getText());
			}

			// category
			List<String> categoryPath = new ArrayList<String>(3);
			xpath = new DefaultXPath(SUNING_ITEM_CATEGORY_XPATH);
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
					PlatformEnum.PLATFORM_SUNING, categoryPath);
			item.setCategory(category);

			// itemPrice
			xpath = new DefaultXPath(SUNING_ITEM_PRICE_XPATH);
			xpath.setNamespaceContext(context);
			node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setPrice(getSuningPrice(((org.dom4j.Node) node).getText()));
			}

			// TODO 优惠价格

			// photo
			xpath = new DefaultXPath(SUNING_ITEM_PHOTO_XPATH);
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

	/**
	 * 
	 * @param html
	 * @return
	 */
	public static Double getSuningPrice(String html) {
		String[] kvs = html.split("&");
		String[] kv = null;
		for (String s : kvs) {
			if (s.contains("currPrice")) {
				kv = s.split("=");
				return Double.valueOf(kv[1]);
			}
		}

		return null;
	}
}