/**
 * 
 */
package com.ihome.matrix;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.ihome.matrix.parser.html.HtmlParserHelper;
import com.ihome.matrix.parser.url.URLParserHelper;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.util.URLUtil;

/**
 * 
 * @author sihai
 *
 */
public class MatrixCrawler extends WebCrawler {
	
	private static Map<String, String> charsetMap;
	
	static {
		charsetMap = new HashMap<String, String>();
		charsetMap.put("product.dangdang.com", "gb2312");
		charsetMap.put("www.amazon.cn", "utf-8");
		charsetMap.put("www.coo8.com", "gbk");
		charsetMap.put("www.efeihu.com", "gb2312");
		charsetMap.put("item.51buy.com", "gb2312");
		charsetMap.put("www.gome.com.cn", "utf-8");
		charsetMap.put("www.360buy.com", "gb2312");
		charsetMap.put("www.lusen.com", "utf-8");
		charsetMap.put("www.new7.com", "utf-8");
		charsetMap.put("www.newegg.com.cn", "gb2312");
		charsetMap.put("www.yihaodian.com", "utf-8");
		charsetMap.put("www.ouku.com", "utf-8");
		charsetMap.put("www.redbaby.com.cn", "utf-8");
		charsetMap.put("www.suning.com", "utf-8");
		charsetMap.put("www.tao3c.com", "gb2312");
	}
	
	private static List<Pattern> acceptPatternList;
	
	static {
		acceptPatternList = new ArrayList<Pattern>();
		//
		acceptPatternList.add(Pattern.compile("http://skii.tmall.com[\\s|\\S]*"));
		acceptPatternList.add(Pattern.compile("http://list.tmall.com/search_product.htm\\?[\\s|\\S]+"));
		acceptPatternList.add(Pattern.compile("http://detail.tmall.com/item.htm\\?[\\s|\\S]*&id=[\\d]+[\\s|\\S]*"));
		
		//
		acceptPatternList.add(Pattern.compile("http://www.amazon.cn/s/[\\s|\\S]*"));
		acceptPatternList.add(Pattern.compile("http://www.amazon.cn/[\\s|\\S]+/dp/[\\s|\\S]+"));
		acceptPatternList.add(Pattern.compile("http://www.amazon.cn/[\\s|\\S]+/b/[\\s|\\S]+"));

		acceptPatternList.add(Pattern.compile("http://www.amazon.cn/gp/product/[\\s|\\S]+"));
		acceptPatternList.add(Pattern.compile("http://www.amazon.cn/[\\s|\\S]+/dp/[\\s|\\S]+"));
		
		acceptPatternList.add(Pattern.compile("http://www.coo8.com/baojianjiankang/[\\s|\\S]*"));
		acceptPatternList.add(Pattern.compile("http://www.coo8.com/meizhuanggehu/[\\s|\\S]*"));
		
		acceptPatternList.add(Pattern.compile("http://www.coo8.com/products/[\\s|\\S]+.html[\\s|\\S]*"));
		acceptPatternList.add(Pattern.compile("http://www.coo8.com/product/[\\s|\\S]+.html[\\s|\\S]*"));
		
		acceptPatternList.add(Pattern.compile("http://cosmetic.dangdang.com/[\\s|\\S]*"));
		acceptPatternList.add(Pattern.compile("http://health.dangdang.com/[\\s|\\S]*"));
		acceptPatternList.add(Pattern.compile("http://category.dangdang.com/list\\?cat=[\\s|\\S]+"));
		acceptPatternList.add(Pattern.compile("http://category.dangdang.com/all/\\?category_id=[\\s|\\S]+"));
		acceptPatternList.add(Pattern.compile("http://category.dangdang.com/all/\\?category_path=[\\s|\\S]+"));
		acceptPatternList.add(Pattern.compile("http://product.dangdang.com/product.aspx\\?product_id=[\\s|\\S]+"));
		
		acceptPatternList.add(Pattern.compile("http://www.360buy.com/beauty.html[\\s|\\S]*"));
		acceptPatternList.add(Pattern.compile("http://www.360buy.com/baby.html[\\s|\\S]*"));
		acceptPatternList.add(Pattern.compile("http://www.360buy.com/products/[\\s|\\S]+.html[\\s|\\S]*"));
		acceptPatternList.add(Pattern.compile("http://www.360buy.com/product/[\\s|\\S]+.html[\\s|\\S]*"));
		
		acceptPatternList.add(Pattern.compile("http://www.gome.com.cn/ec/homeus/jump/category/cat[\\s|\\S]+.html[\\s|\\S]*"));
		acceptPatternList.add(Pattern.compile("http://www.gome.com.cn/ec/homeus/jump/product/[\\s|\\S]+.html[\\s|\\S]*"));
		
		
		acceptPatternList.add(Pattern.compile("http://www.newegg.com.cn/Health.htm[\\s|\\S]*"));
		acceptPatternList.add(Pattern.compile("http://www.newegg.com.cn/Baby.htm[\\s|\\S]*"));
		acceptPatternList.add(Pattern.compile("http://www.newegg.com.cn/Food.htm[\\s|\\S]*"));
		acceptPatternList.add(Pattern.compile("http://www.newegg.com.cn/Category/[\\s|\\S]+.htm[\\s|\\S]*"));
		acceptPatternList.add(Pattern.compile("http://www.newegg.com.cn/SubCategory/[\\s|\\S]+.htm[\\s|\\S]*"));
		acceptPatternList.add(Pattern.compile("http://www.newegg.com.cn/Product/[\\s|\\S]+.htm[\\s|\\S]*"));
		
		
		acceptPatternList.add(Pattern.compile("http://channel.yihaodian.com/meihu[\\s|\\S]*"));
		acceptPatternList.add(Pattern.compile("http://channel.yihaodian.com/muying[\\s|\\S]*"));
		acceptPatternList.add(Pattern.compile("http://www.yihaodian.com/channel/8704_1[\\s|\\S]*"));
		acceptPatternList.add(Pattern.compile("http://search.yihaodian.com/s2/[\\s|\\S]+"));
		acceptPatternList.add(Pattern.compile("http://www.yihaodian.com/ctg/s2/[\\s|\\S]+"));
		acceptPatternList.add(Pattern.compile("http://www.yihaodian.com/cmsPage/show.do\\?[\\s|\\S]+"));
		acceptPatternList.add(Pattern.compile("http://www.yihaodian.com/product/[\\s|\\S]+"));
		
	}
	
	@Override
	public boolean shouldVisit(WebURL url) {
		for(Pattern p : acceptPatternList) {
			if(p.matcher(url.getURL()).matches()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void visit(Page page) {
		//System.out.println(String.format("URL:%s", page.getWebURL().getURL()));
		HtmlParserHelper.parse(page.getWebURL().getURL(), new String(page.getContentData()), getCharset(page.getWebURL().getURL()));
	}
	
	@Override
	public void parseURL(WebURL webURL) {
		super.parseURL(webURL);
		URLParserHelper.parse(webURL.getURL());
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 */
	private String getCharset(String strURL) {
		try {
			URL url = new URL(strURL);
			String charset = charsetMap.get(url.getHost());
			if(null == charset) {
				charset = URLUtil.DEFAULT_CHARSET;
			}
			return charset;
		} catch (MalformedURLException e) {
			logger.error(e);
			return URLUtil.DEFAULT_CHARSET;
		}
	}
	
	public static void main(String[] args) {
		MatrixCrawler c = new MatrixCrawler();
		WebURL webURL = new WebURL();
		webURL.setURL("http://www.amazon.cn/AVON%E9%9B%85%E8%8A%B3%E5%B0%8F%E9%BB%91%E8%A3%99%E8%B5%B0%E7%8F%A0%E9%A6%99%E6%B0%B49ml/dp/B0023W6RY6/ref=pd_ts_zgc_beauty_746781051_7_i?ie=UTF8&pf_rd_i=746776051&pf_rd_m=A1AJ19PSB66TGU&pf_rd_p=58626252&pf_rd_r=1D");
		System.out.println(c.shouldVisit(webURL));
	}
}
