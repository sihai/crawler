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

import edu.uci.ics.crawler4j.util.URLUtil;

/**
 * 解析当当商品页面
 * 
 * @author sihai
 * 
 */
public class DangdangHtmlParser extends AbstractHtmlParser {

	private static final Log logger = LogFactory.getLog(DangdangHtmlParser.class);

	private static final String DANGDANG_ITEM_ID_XPATH = "";
	private static final String DANGDANG_PRODUCT_PARAMETER_ID = "product_id";
	private static final String DNAGDANG_ITEM_NAME_XPATH = "/xmlns:HTML/xmlns:BODY/xmlns:DIV[3]/xmlns:DIV[4]/xmlns:DIV[1]/xmlns:H1";
	private static final String DANGDANG_ITEM_PRICE_XPATH = "//*[@id='salePriceTag']";
	private static final String DANGDANG_ITEM_PHOTO_XPATH = "//*[@id='largePic']/@src";
	private static final String DANGDANG_ITEM_CATEGORY_XPATH = "/xmlns:HTML/xmlns:BODY/xmlns:DIV[3]/xmlns:DIV[3]/xmlns:A/text()";

	private static final Pattern DANGDANG_ITEM_URL_PATTERN = Pattern
			.compile("^http://product.dangdang.com/product.aspx\\?product_id=(\\S)*");

	@Override
	protected boolean accept(String strURL) {
		return DANGDANG_ITEM_URL_PATTERN.matcher(strURL).matches();
	}

	@Override
	protected ItemDO doParse(String strURL, String html) {
		return parseDangdangItem(strURL, html);
	}
	
	/**
	 * 
	 * @param strURL
	 * @param html
	 * @return
	 */
	public ItemDO parseDangdangItem(String strURL, String html) {
		try {
			ItemDO item = new ItemDO();
			item.setPlatform(PlatformEnum.PLATFORM_DANGDANG.getValue());
			item.setShop(MatrixBridge
					.getFixedShop(PlatformEnum.PLATFORM_DANGDANG));
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
			item.setItemId(URLUtil.getParameter(strURL,
					DANGDANG_PRODUCT_PARAMETER_ID));

			// itemName
			XPath xpath = new DefaultXPath(DNAGDANG_ITEM_NAME_XPATH);
			xpath.setNamespaceContext(context);
			Object node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setName(((org.dom4j.Node) node).getText());
			}

			// item category
			List<String> categoryPath = new ArrayList<String>(3);
			xpath = new DefaultXPath(DANGDANG_ITEM_CATEGORY_XPATH);
			xpath.setNamespaceContext(context);
			node = xpath.selectNodes(document);
			if (null != node) {
				int length = ((List<org.dom4j.Node>) node).size();
				int i = 0;
				for (org.dom4j.Node n : (List<org.dom4j.Node>) node) {
					if (++i == 1) {
						continue;
					} else {
						categoryPath.add(n.getText());
					}
				}
			}

			// 生成类目树
			CategoryDO category = generateCategoryTree(
					PlatformEnum.PLATFORM_DANGDANG, categoryPath);
			item.setCategory(category);

			// itemPrice
			xpath = new DefaultXPath(DANGDANG_ITEM_PRICE_XPATH);
			node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setPrice(Double.valueOf(((org.dom4j.Node) node).getText()
						.substring("￥".length()).replaceAll(",", "")));
			}

			// TODO 优惠价格

			// photo
			xpath = new DefaultXPath(DANGDANG_ITEM_PHOTO_XPATH);
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
