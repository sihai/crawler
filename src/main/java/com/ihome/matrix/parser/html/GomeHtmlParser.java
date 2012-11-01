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
 * 解析国美商品页面
 * 
 * @author sihai
 * 
 */
public class GomeHtmlParser extends AbstractHtmlParser {

	private static final Log logger = LogFactory.getLog(GomeHtmlParser.class);
	
	private static final String GOME_ITEM_ID_XPATH = "/HTML/BODY/DIV[4]/DIV[1]/DIV[3]/DIV[2]/text()";
	private static final String GOME_ITEM_NAME_XPATH = "//*[@id='lxf-sctc']/DIV[1]/DIV[2]/P";
	private static final String GOME_ITEM_PRICE_XPATH = "/HTML/BODY/DIV[4]/DIV[1]/DIV[3]/DIV[3]/B";
	private static final String GOME_ITEM_PHOTO_XPATH = "//*[@id='pic_1']/@bgpic";
	private static final String GOME_ITEM_CATEGORY_XPATH = "/HTML/BODY/DIV[4]/A/text()";

	private static final Pattern GOME_ITEM_URL_PATTERN = Pattern
			.compile("^http://www.gome.com.cn/ec/homeus/jump/product/(\\S)*.html(\\S)*");

	@Override
	protected boolean accept(String strURL) {
		return GOME_ITEM_URL_PATTERN.matcher(strURL).matches();
	}

	@Override
	protected ItemDO doParse(String strURL, String html) {
		return parseGomeItem(strURL, html);
	}
	
	/**
	 * 
	 * @param content
	 * @return
	 */
	public ItemDO parseGomeItem(String strURL, String html) {
		try {
			ItemDO item = new ItemDO();
			item.setPlatform(PlatformEnum.PLATFORM_GOME.getValue());
			item.setShop(MatrixBridge.getFixedShop(PlatformEnum.PLATFORM_GOME));
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

			// itemId
			XPath xpath = new DefaultXPath(GOME_ITEM_ID_XPATH);
			Object node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setItemId(((org.dom4j.Node) node).getText());
			}

			// itemName
			xpath = new DefaultXPath(GOME_ITEM_NAME_XPATH);
			node = xpath.selectSingleNode(document);
			if (null != node) {
				String txt = ((org.dom4j.Node) node).getText();
				item.setName(txt.substring(0, txt.indexOf("已成功加入收藏")).trim());
			}

			// item category
			List<String> categoryPath = new ArrayList<String>(3);
			xpath = new DefaultXPath(GOME_ITEM_CATEGORY_XPATH);
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
					PlatformEnum.PLATFORM_GOME, categoryPath);
			item.setCategory(category);

			// itemPrice
			xpath = new DefaultXPath(GOME_ITEM_PRICE_XPATH);
			node = xpath.selectSingleNode(document);
			if (null != node) {
				item.setPrice(Double.valueOf(((org.dom4j.Node) node).getText()
						.replaceAll(",", "").trim()));
			}

			// TODO 优惠价格

			// photo
			xpath = new DefaultXPath(GOME_ITEM_PHOTO_XPATH);
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