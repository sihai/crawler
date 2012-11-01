package com.ihome.matrix.parser.html;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.sourceforge.tess4j.TesseractException;

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
 * 解析新蛋商品页面
 * 
 * @author sihai
 * 
 */
public class NewEggHtmlParser extends AbstractHtmlParser {

	private static final Log logger = LogFactory.getLog(NewEggHtmlParser.class);

	private static final String NEW_EGG_ITEM_NAME_XPATH = "//*[@id='proCtner']/xmlns:DIV[1]/xmlns:H1";
	private static final String NEW_EGG_ITEM_CATEGORY_XPATH = "//*[@id='crumb']/xmlns:DIV/xmlns:A";
	private static final String NEW_EGG_ITEM_PHOTO_XPATH = "//*[@id='thumbnails1']/xmlns:DIV/xmlns:UL/xmlns:LI[1]/xmlns:A/xmlns:IMG/@ref2";
	// *[@id="proMainInfo"]/div[2]/div[1]/dl/dd[4]/p[1]/img
	private static final String NEW_EGG_ITEM_PRICE_XPATH = "//*[@id='proMainInfo']/xmlns:DIV[2]/xmlns:DIV[1]/xmlns:DL/xmlns:DD[4]/xmlns:P[1]/xmlns:IMG/@src";
	private static final String NEW_EGG_ITEM_PROMOTION_PRICE_XPATH = "//*[@id='proMainInfo']/xmlns:DIV[2]/xmlns:DIV[1]/xmlns:DL/xmlns:DD[5]/xmlns:P[1]/xmlns:IMG/@src";

	private static final Pattern NEW_EGG_ITEM_URL_PATTERN = Pattern
			.compile("^http://www.newegg.com.cn/Product/(\\S)*.htm(\\S)*");

	@Override
	protected boolean accept(String strURL) {
		return NEW_EGG_ITEM_URL_PATTERN.matcher(strURL).matches();
	}

	@Override
	protected ItemDO doParse(String strURL, String html) {
		return parseNewEggItem(strURL, html);
	}

	/**
	 * 
	 * @param content
	 * @return
	 */
	public ItemDO parseNewEggItem(String strURL, String html) {
		try {
			ItemDO item = new ItemDO();
			item.setPlatform(PlatformEnum.PLATFORM_NEW_EGG.getValue());
			item.setShop(MatrixBridge
					.getFixedShop(PlatformEnum.PLATFORM_NEW_EGG));
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

			int index0 = strURL.indexOf("/Product/");
			if (-1 != index0) {
				int index1 = strURL.indexOf(".htm",
						index0 + "/Product/".length());
				if (-1 != index1) {
					item.setItemId(strURL.substring(
							index0 + "/Product/".length(), index1));
				}
			}

			// itemName
			XPath xpath = new DefaultXPath(NEW_EGG_ITEM_NAME_XPATH);
			xpath.setNamespaceContext(context);
			Object node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setName(((org.dom4j.Node) node).getText());
			}

			// category
			List<String> categoryPath = new ArrayList<String>(3);
			xpath = new DefaultXPath(NEW_EGG_ITEM_CATEGORY_XPATH);
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
					PlatformEnum.PLATFORM_NEW_EGG, categoryPath);
			item.setCategory(category);

			// itemPrice
			/*
			 * xpath = new DefaultXPath(NEW_EGG_ITEM_PRICE_XPATH);
			 * xpath.setNamespaceContext(context); node =
			 * xpath.selectSingleNode(document); if(null != node) {
			 * item.setPrice
			 * (DOMContentUtils.discernNewEggPrice((((org.dom4j.Node
			 * )node).getText()))); }
			 */

			// TODO 优惠价格
			xpath = new DefaultXPath(NEW_EGG_ITEM_PROMOTION_PRICE_XPATH);
			xpath.setNamespaceContext(context);
			node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setPrice(discernNewEggPrice((((org.dom4j.Node) node)
						.getText())));
			} else {
				// 拿新蛋价格
				xpath = new DefaultXPath(NEW_EGG_ITEM_PRICE_XPATH);
				xpath.setNamespaceContext(context);
				node = xpath.selectSingleNode(document);
				if (null != node) {
					item.setPrice(discernNewEggPrice((((org.dom4j.Node) node)
							.getText())));
				}
			}
			// photo
			xpath = new DefaultXPath(NEW_EGG_ITEM_PHOTO_XPATH);
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
	 * @param photoURL
	 * @return
	 */
	private Double discernNewEggPrice(String photoURL) {
		System.out.println(String.format("Price photo url:%s", photoURL));
		File tmpFile = null;
		try {
			tmpFile = URLUtil.fetchFile(photoURL, ".gif");
			String result = instance.doOCR(tmpFile);
			return Double.valueOf(result);
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