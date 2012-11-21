package com.ihome.matrix.parser.html;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
import com.ihome.matrix.domain.ItemDO;
import com.ihome.matrix.domain.ShopDO;
import com.ihome.matrix.enums.FreightFeePayerEnum;
import com.ihome.matrix.enums.ItemStatusEnum;
import com.ihome.matrix.enums.PlatformEnum;
import com.ihome.matrix.enums.ShopStatusEnum;
import com.ihome.matrix.enums.StuffStatusEnum;

import edu.uci.ics.crawler4j.util.URLUtil;

/**
 * 解析亚马逊商品页面
 * @author sihai
 *
 */
public class AmazonHtmlParser extends AbstractHtmlParser {
	
	private static final Log logger = LogFactory.getLog(AmazonHtmlParser.class);
	
	private static final String AMAZON_PARAMETER_SELLER = "seller";
	
	private static final String AMAZON_ITEM_NAME_XPATH = "//*[@id='btAsinTitle']/text()";
	private static final String AMAZON_ITEM_CATEGORY_XPATH = "//*[@id='nav-subnav']/LI[1]/A";
	private static final String AMAZON_ITEM_PRICE_XPATH = "//*[@id='actualPriceValue']/B";
	private static final String AMAZON_ITEM_PHOTO_XPATH = "//*[@id='prodImage']/@src";
	private static final String AMAZON_ITEM_PHOTO_XPATH_1 = "//*[@id='original-main-image']/@src";
	
	private static final String AMAZON_SHOP_NAME_XPATH = "//*[@id='handleBuy']/TABLE[3]/TR[4]/TD/DIV/B/A/text()";
	private static final String AMAZON_SHOP_URL_XPATH =  "//*[@id='handleBuy']/TABLE[3]/TR[4]/TD/DIV/B/A/@href";
	
	private static final Pattern AMAZON_ITEM_URL_PATTERN = Pattern.compile("http://www.amazon.cn/gp/product/(\\S)+");
	private static final Pattern AMZON_ITEM_URL_PATTERN_1 = Pattern.compile("http://www.amazon.cn/(\\S)*/dp/(\\S)+");
	
	@Override
	protected boolean accept(String strURL) {
		return AMAZON_ITEM_URL_PATTERN.matcher(strURL).matches() || AMZON_ITEM_URL_PATTERN_1.matcher(strURL).matches();
	}

	@Override
	protected ItemDO doParse(String strURL, String html, String charset) {
		ItemDO item = parseAmazonItem(strURL, html, charset);
		//System.out.println(item);
		return item;
	}
	
	/**
	 * 
	 * @return
	 */
	private ItemDO parseAmazonItem(String strURL, String html, String charset) {
		try {
			  ItemDO item = new ItemDO();
			  item.setPlatform(PlatformEnum.PLATFORM_AMAZON.getValue());
			  item.setShop(MatrixBridge.getFixedShop(PlatformEnum.PLATFORM_AMAZON));
			  item.setDetailURL(strURL);
			  item.setStuffStatus(StuffStatusEnum.STUFF_NEW.getValue());
			  item.setNumber(-1L);
			  item.setStatus(ItemStatusEnum.ITEM_STATUS_ON_SALE.getValue());
			  item.setFreightFeePayer(FreightFeePayerEnum.FREIGHT_FEE_PALYER_SELLER.getValue());
			  item.setIsDeleted(false);
			  item.setGmtCreate(new Date());
			  item.setGmtModified(item.getGmtCreate());
			  
			  //write2File("/home/sihai/ihome/amazon.html", html, charset);
			  
		      InputSource input = new InputSource(new ByteArrayInputStream(html.getBytes()));
		      DOMParser parser = new DOMParser();
		      input.setEncoding(charset);
		      parser.parse(input);
		      org.w3c.dom.Document w3cDoc = parser.getDocument(); 
		      DOMReader domReader = new DOMReader();
		      org.dom4j.Document document = domReader.read(w3cDoc);
		      document.setXMLEncoding(charset);
		      
		      XPath xpath = null;
		      Object node = null;
		      
		      // itemId
		      int index0 = strURL.indexOf("/gp/product/");
		      int index1 = -1;
		      if(-1 != index0) {
		    	  index1 = strURL.indexOf("/", index0 + "/gp/product/".length());
		    	  if(-1 != index1) {
		    		  item.setItemId(strURL.substring(index0 + "/gp/product/".length(), index1));
		    	  }
		      } else {
		    	  index0 = strURL.indexOf("/dp/");
		    	  if(-1 != index0) {
			    	  index1 = strURL.indexOf("/", index0 + "/dp/".length());
			    	  if(-1 != index1) {
			    		  item.setItemId(strURL.substring(index0 + "/dp/".length(), index1));
			    	  }
			      }
		      }
		      
		      /*XPath xpath = new DefaultXPath(COO8_ITEM_ID_XPATH);
			  Object node = xpath.selectSingleNode(document);
			  if(null != node) {
				  item.setItemId(((org.dom4j.Node)node).getText());
			  }*/
			  
			  // itemName
			  xpath = new DefaultXPath(AMAZON_ITEM_NAME_XPATH);
			  node = xpath.selectSingleNode(document);
			  if(null != node) {
				  item.setName(((org.dom4j.Node)node).getText());
			  }
			  
			  // category and name
			  List<String> categoryPath = new ArrayList<String>(3);
			  xpath = new DefaultXPath(AMAZON_ITEM_CATEGORY_XPATH);
			  node = xpath.selectNodes(document);
			  if (null != node) {
				  for (org.dom4j.Node n : (List<org.dom4j.Node>) node) {
					categoryPath.add(n.getText());
				  }
			  }
			  item.setCategory(generateCategoryTree(PlatformEnum.PLATFORM_AMAZON, categoryPath));
			  // itemPrice
			  xpath = new DefaultXPath(AMAZON_ITEM_PRICE_XPATH);
			  node = xpath.selectSingleNode(document);
			  if(null != node) {
				  item.setPrice(Double.valueOf(((org.dom4j.Node)node).getText().substring("￥ ".length()).replaceAll(",", "")));
			  }
			  
			  // photo
			  /*String html = content.toString();
			  int start = html.indexOf("\"hiResImage\":");
			  if(-1 != start) {
				  int end = html.indexOf(",", start);
				  if(-1 != end) {
					  item.setLogoURL(html.substring(start + "\"hiResImage\":".length() + "\"".length(), end - "\"".length()));
				  }
			  }*/
			  xpath = new DefaultXPath(AMAZON_ITEM_PHOTO_XPATH);  
			  node = xpath.selectSingleNode(document);
			  if(null != node) {
				  item.setLogoURL(generatePhoto(((org.dom4j.Attribute)node).getValue()));
			  } else {
				  xpath = new DefaultXPath(AMAZON_ITEM_PHOTO_XPATH_1);  
				  node = xpath.selectSingleNode(document);
				  if(null != node) {
					  item.setLogoURL(generatePhoto(((org.dom4j.Attribute)node).getValue()));
				  }
			  }
			  
			  // Shop
			  xpath = new DefaultXPath(AMAZON_SHOP_NAME_XPATH);  
			  node = xpath.selectSingleNode(document);
			  if(null != node) {
				  ShopDO shop = new ShopDO();
				  shop.setName(((org.dom4j.Node)node).getText());
				  xpath = new DefaultXPath(AMAZON_SHOP_URL_XPATH);  
				  node = xpath.selectSingleNode(document);
				  if(null != node) {
					  shop.setDetailURL(String.format("%s%s", PlatformEnum.PLATFORM_AMAZON.getUrl(), ((org.dom4j.Attribute)node).getValue()));
				  }
				  shop.setShopId(URLUtil.getParameter(shop.getDetailURL(), AMAZON_PARAMETER_SELLER));
				  shop.setSellerName(shop.getName());
				  shop.setPlatform(PlatformEnum.PLATFORM_AMAZON.getValue());
				  shop.setStatus(ShopStatusEnum.SHOP_STATUS_NORMAL.getValue());
				  shop.setIsDeleted(false);
				  shop.setGmtCreate(new Date());
				  shop.setGmtModified(shop.getGmtCreate());
				  
				  item.setShop(shop);
			  }
			  // Category
			  
			  return item;
		  } catch (IOException e) {
			  logger.error(e);
		  } catch (SAXException e) {
			  logger.error(e);
		  }
		  
		  return null;
	}
}