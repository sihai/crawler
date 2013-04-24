/**
 * 
 */
package com.ihome.matrix;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.ihome.matrix.parser.html.HtmlParserHelper;
import com.ihome.matrix.parser.url.URLParserHelper;
import com.ihome.matrix.plugin.PluginRepository;

import edu.uci.ics.crawler4j.crawler.CrawlController;
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
		charsetMap.put("www.ehaoyao.com", "gb2312");
		charsetMap.put("www.111.com.cn", "utf-8");
		charsetMap.put("www.bjypw.com", "utf-8");
		charsetMap.put("www.daoyao.com", "utf-8");
		charsetMap.put("www.hp1997.com", "gb2312");
		charsetMap.put("www.gyjm.com.cn", "utf-8");
		charsetMap.put("www.huatuoyf.com", "utf-8");
		charsetMap.put("www.j1.com", "utf-8");
		charsetMap.put("www.jxdyf.com", "utf-8");
	}
	
	/*private static List<Pattern> acceptPatternList;
	
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
		
	}*/
	
	
	@Override
	public void init(int myId, CrawlController crawlController) {
		super.init(myId, crawlController);
	}
	
	@Override
	public void onBeforeExit() {
		super.onBeforeExit();
	}

	@Override
	public boolean shouldVisit(WebURL url) {
		return PluginRepository.getURLFilter().shouldAccept(url.getURL());
	}

	@Override
	public void visit(Page page) {
		//System.out.println(String.format("URL:%s", page.getWebURL().getURL()));
		String charset = getCharset(page.getWebURL().getURL());
		HtmlParserHelper.parse(page.getWebURL().getURL(), page.getContentData(), charset);
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
		webURL.setURL("http://qilekang.tmall.com/");
		System.out.println(c.shouldVisit(webURL));
	}
}
