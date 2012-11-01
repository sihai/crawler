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
 * 解析京东商品页面
 * 
 * @author sihai
 * 
 */
public class JingdongHtmlParser extends AbstractHtmlParser {

	private static final Log logger = LogFactory
			.getLog(JingdongHtmlParser.class);

	private static String JING_DONG_ITEM_ID_XPATH = "//*[@id='summary-market']/xmlns:DIV[2]/xmlns:SPAN/text()";
	// html/body/div[4]/div/span/a[1]
	// html/body/div[4]/div/span/a[1]
	// html/body/div[3]/div/span/a[1]
	private static String JING_DONG_ITEM_CATEGORY_AND_NAME_XPATH = "/xmlns:HTML/xmlns:BODY/xmlns:DIV[3]/xmlns:DIV/xmlns:SPAN/xmlns:A";
	private static String JING_DONG_ITEM_PRICE_XPATH = "//*[@id='summary-price']/xmlns:DIV[2]/xmlns:STRONG/xmlns:IMG/@src";
	private static String JING_DONG_ITEM_PHOTO_XPATH = "//*[@id='spec-n1']/xmlns:IMG/@src";

	private static final Pattern JINGDONG_ITEM_URL_PATTERN = Pattern
			.compile("^http://www.360buy.com/product/(\\S)*\\.html(\\S)*");

	@Override
	protected boolean accept(String strURL) {
		return JINGDONG_ITEM_URL_PATTERN.matcher(strURL).matches();
	}

	@Override
	protected ItemDO doParse(String strURL, String html) {
		return parseJingdongItem(strURL, html);
	}

	/**
	 * 
	 * @param content
	 * @return
	 */
	public ItemDO parseJingdongItem(String strURL, String html) {
		try {
			ItemDO item = new ItemDO();
			item.setPlatform(PlatformEnum.PLATFORM_360_BUY.getValue());
			item.setShop(MatrixBridge
					.getFixedShop(PlatformEnum.PLATFORM_360_BUY));
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

			XPath xpath = new DefaultXPath(JING_DONG_ITEM_ID_XPATH);
			xpath.setNamespaceContext(context);
			Object node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setItemId(((org.dom4j.Node) node).getText());
			}

			// category and name
			List<String> categoryPath = new ArrayList<String>(3);
			xpath = new DefaultXPath(JING_DONG_ITEM_CATEGORY_AND_NAME_XPATH);
			xpath.setNamespaceContext(context);
			node = xpath.selectNodes(document);
			if (null != node) {
				int length = ((List<org.dom4j.Node>) node).size();
				int i = 0;
				for (org.dom4j.Node n : (List<org.dom4j.Node>) node) {
					if (++i == length) {
						item.setName(n.getText());
					} else {
						categoryPath.add(n.getText());
					}
				}
			}

			// 生成类目树
			CategoryDO category = generateCategoryTree(
					PlatformEnum.PLATFORM_360_BUY, categoryPath);
			item.setCategory(category);

			// price
			xpath = new DefaultXPath(JING_DONG_ITEM_PRICE_XPATH);
			xpath.setNamespaceContext(context);
			node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setPrice(discernJingdongPrice(((org.dom4j.Attribute) node)
						.getValue()));
			}

			// photo
			xpath = new DefaultXPath(JING_DONG_ITEM_PHOTO_XPATH);
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

	private Double discernJingdongPrice(String photoURL) {
		System.out.println(String.format("Price photo url:%s", photoURL));
		File tmpFile = null;
		try {
			tmpFile = URLUtil.fetchFile(photoURL, ".png");
			String result = instance.doOCR(tmpFile);
			if (result.startsWith("Y")) {
				return Double.valueOf(result.substring("Y".length()));
			} else if (result.startsWith("51")) {
				return Double.valueOf(result.substring("51".length()));
			} else {
				return Double.valueOf(result);
			}
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