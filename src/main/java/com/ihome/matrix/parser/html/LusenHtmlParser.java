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
 * 解析绿森商品页面
 * 
 * @author sihai
 * 
 */
public class LusenHtmlParser extends AbstractHtmlParser {

	private static final Log logger = LogFactory.getLog(LusenHtmlParser.class);
	
	private static final String LUSEN_ITEM_NAME_XPATH = "/xmlns:HTML/xmlns:BODY/xmlns:DIV[1]/xmlns:DIV[7]/xmlns:DIV[3]/xmlns:DIV[2]/xmlns:DIV[1]/xmlns:DIV[2]";
	private static final String LUSEN_ITEM_CATEGORY_XPATH = "/xmlns:HTML/xmlns:BODY/xmlns:DIV[1]/xmlns:DIV[7]/xmlns:DIV[1]/xmlns:DIV[2]/xmlns:A";
	private static final String LUSEN_ITEM_PHOTO_XPATH = "//*[@id='smallPic']/@lazy_src";
	private static final String LUSEN_ITEM_PRICE_XPATH = "//*[@id='DivProducInfo']/xmlns:DIV[2]/xmlns:SPAN[2]/xmlns:FONT";
	private static final String LUSEN_ITEM_PROMOTION_PRICE_XPATH = "//*[@id='DivPanicBuyOrComity']/xmlns:DIV[2]/xmlns:SPAN[2]/xml:FONT";
	private static final String LUSEN_ITEM_GIFT_XPATH = "";
	
	private static final Pattern LUSEN_ITEM_URL_PATTERN = Pattern
			.compile("^http://www.lusen.com/Product/ProductInfo.aspx\\?(\\S)*");

	@Override
	protected boolean accept(String strURL) {
		return LUSEN_ITEM_URL_PATTERN.matcher(strURL).matches();
	}

	@Override
	protected ItemDO doParse(String strURL, String html) {
		return parseLusenItem(strURL, html);
	}

	/**
	 * 
	 * @param content
	 * @return
	 */
	public ItemDO parseLusenItem(String strURL, String html) {
		try {
			ItemDO item = new ItemDO();
			item.setPlatform(PlatformEnum.PLATFORM_LUSEN.getValue());
			item.setShop(MatrixBridge.getFixedShop(PlatformEnum.PLATFORM_LUSEN));
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
			/*
			 * XPath xpath = new DefaultXPath(SUNING_ITEM_ID_XPATH);
			 * xpath.setNamespaceContext(context); Object node =
			 * xpath.selectSingleNode(document); if(null != node) {
			 * item.setName(((org.dom4j.Node)node).getText()); }
			 */
			item.setItemId(URLUtil.getParameter(strURL, "id"));

			// itemName
			XPath xpath = new DefaultXPath(LUSEN_ITEM_NAME_XPATH);
			xpath.setNamespaceContext(context);
			Object node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setName(((org.dom4j.Node) node).getText());
			}

			// category
			List<String> categoryPath = new ArrayList<String>(3);
			xpath = new DefaultXPath(LUSEN_ITEM_CATEGORY_XPATH);
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
					PlatformEnum.PLATFORM_LUSEN, categoryPath);
			item.setCategory(category);

			// itemPrice
			xpath = new DefaultXPath(LUSEN_ITEM_PRICE_XPATH);
			xpath.setNamespaceContext(context);
			node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setPrice(Double.valueOf(((org.dom4j.Node) node).getText()
						.substring("￥".length()).replaceAll(",", "")));
			}

			// TODO 优惠价格, 如果有优惠价格, 将会覆盖上面的价格
			xpath = new DefaultXPath(LUSEN_ITEM_PROMOTION_PRICE_XPATH);
			xpath.setNamespaceContext(context);
			node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setPrice(Double.valueOf(((org.dom4j.Node) node).getText()
						.substring("￥".length()).replaceAll(",", "")));
			}

			// TODO photo
			xpath = new DefaultXPath(LUSEN_ITEM_PHOTO_XPATH);
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
