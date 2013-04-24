/**
 * 
 */
package com.ihome.matrix.parser.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilationFailedException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.iacrqq.util.DateUtil;
import com.iacrqq.util.StringUtil;
import com.ihome.matrix.domain.BrandDO;
import com.ihome.matrix.domain.CategoryDO;
import com.ihome.matrix.domain.CommentDO;
import com.ihome.matrix.domain.ItemDO;
import com.ihome.matrix.domain.ShopCategoryDO;
import com.ihome.matrix.domain.ShopDO;
import com.ihome.matrix.enums.BrandStatusEnum;
import com.ihome.matrix.enums.CommentTypeEnum;
import com.ihome.matrix.enums.FreightFeePayerEnum;
import com.ihome.matrix.enums.ItemStatusEnum;
import com.ihome.matrix.enums.ShopStatusEnum;
import com.ihome.matrix.enums.StuffStatusEnum;

import edu.uci.ics.crawler4j.util.CrawlerThreadFactory;
import edu.uci.ics.crawler4j.util.URLUtil;
import groovy.lang.GroovyClassLoader;

/**
 * 
 * @author sihai
 *
 */
public class HtmlParserHelper {

	private static final Log logger = LogFactory.getLog(HtmlParserHelper.class);
	
	public static final String PARSER_CONF_FILE_NAME = "parser.cnf";
	
	public static final int DEFAULT_MIN_THREAD = 256;
	public static int DEFAULT_MAX_THREAD = 512;
	public static int DEFAULT_MAX_WORK_QUEUE_SIZE = 8192;
	public static long MAX_KEEP_ALIVE_TIME = 60;

	private static int minThread = DEFAULT_MIN_THREAD;
	private static int maxThread = DEFAULT_MAX_THREAD;
	private static int workQueueSize = DEFAULT_MAX_WORK_QUEUE_SIZE;
	private static long keepAliveTime = MAX_KEEP_ALIVE_TIME;		// s
	
	private static BlockingQueue<Runnable> workQueue;	//
	private static ThreadPoolExecutor threadPool;		//
	
	private static List<HtmlParser> htmlParserChain;
	
	/**
	 * 
	 */
	public static void init() {
		
		// parser chain
		htmlParserChain = new ArrayList<HtmlParser>(16);
		//htmlParserChain.add(new AmazonHtmlParser());
		htmlParserChain.add(new JingdongHtmlParser());
		
		htmlParserChain.add(new EhaoyaoHtmlParser());
		htmlParserChain.add(new No1YaoWangHtmlParser());
		htmlParserChain.add(new BeijingYaoPinWangHtmlParser());
		htmlParserChain.add(new DaoYaoHtmlParser());
		htmlParserChain.add(new HePingHtmlParser());
		htmlParserChain.add(new GuangYaoJianMinHtmlParser());
		htmlParserChain.add(new HuaTuoYaoFangHtmlParser());
		htmlParserChain.add(new JianYiWangHtmlParser());
		htmlParserChain.add(new JinXiangDaYaoFangHtmlParser());
		
		_init_parser_(PARSER_CONF_FILE_NAME);
		
		// thread pool
		workQueue = new LinkedBlockingQueue<Runnable>(workQueueSize);
		threadPool = new ThreadPoolExecutor(minThread, maxThread,
		            keepAliveTime, TimeUnit.SECONDS,
		            workQueue, new CrawlerThreadFactory("URL-Parser", null, true));
	}
	
	/**
	 * 
	 * @param confFileName
	 */
	private static void _init_parser_(String confFileName) {
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(confFileName);
		if(null == is) {
			throw new RuntimeException(String.format("Can not load configuration from file :%s, can not found this file in classpath", confFileName));
		} else {
			try {
				Properties properties = new Properties();
				properties.load(is);
				_init_parser_(properties);
			} catch (IOException e) {
				logger.warn(String.format("Can not load configuration from file :%s, can read this file", confFileName), e);
			} finally {
				if(null != is) {
					try {
						is.close();
					} catch (IOException e) {
						logger.error(e);
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param properties
	 */
	private static void _init_parser_(Properties properties) {
		String key = String.format("%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, ParserConfiguration.PARSER_CONFIGURATION_PARSERS);
		String value = properties.getProperty(key);
		if(StringUtil.isBlank(value)) {
			logger.warn(String.format("There is no define value for key: %s", key));
			return;
		}
		String[] parsers = value.split(",");
		for(String parser : parsers) {
			if(StringUtil.isNotBlank(parser)) {
				htmlParserChain.add(_init_parser_(parser, properties));
			}
		}
	}
	
	/**
	 * 
	 * @param parser
	 * @param properties
	 * @return
	 */
	private static HtmlParser _init_parser_(String parser, Properties properties) {
		String key;
		String value;
		ParserConfiguration configuration = ParserConfiguration.newConfiguration();

		// charset
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_CHARSET);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withCharset(StringUtil.trim(value));
		}
		
		// platform
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_PLATFORM);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withPlatform(Integer.valueOf(StringUtil.trim(value)));
		} else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}
		
		// platform name
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_PLATFORM_NAME);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withPlatformName(StringUtil.trim(value));
		} else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}
		
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_PLATFORM_URL);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withPlatformURL(StringUtil.trim(value));
		} else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}
		
		// shop.name
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_SHOP_NAME);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withShopName(StringUtil.trim(value));
		} else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}
		
		// shop.id
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_SHOP_ID);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withShopId(StringUtil.trim(value));
		} else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}
		
		// shop.sellerName
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_SHOP_SELLER_NAME);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withSellerName(StringUtil.trim(value));
		} else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}
		
		// shop.url
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_SHOP_URL);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withShopURL(StringUtil.trim(value));
		} else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}
		
		// shop.logoURL
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_SHOP_LOGO_URL);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withShopLogoURL(StringUtil.trim(value));
		} else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}
		
		// shop.isRecommend
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_SHOP_IS_RECOMMEND);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withIsShopRecommend(Boolean.valueOf(StringUtil.trim(value)));
		} else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}
		
		// shop.shopRank
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_SHOP_RANK);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withShopRank(Long.valueOf(StringUtil.trim(value)));
		} else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}
		
		// shop.shopPayways
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_SHOP_PAYWAYS);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withShopPayways(StringUtil.trim(value));
		} else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}
		
		// shop.distributeWays
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_SHOP_DISTRIBUTE_WAYS);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withShopDistributeWays(StringUtil.trim(value));
		} else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}
		
		// shop.categoryList
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_SHOP_CATEGORY_LIST);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			String[] kv = null;
			Long [] tmp = null;
			String[] ids = value.split(",");
			List<Long[]> idList = new ArrayList<Long[]>(ids.length);
			for(String id : ids) {
				if(StringUtil.isNotBlank(id)) {
					kv = StringUtil.trim(id).split(":");
					if(2 != kv.length) {
						throw new IllegalArgumentException(String.format("Please set right value for key:%s, value format: id:rank, id:rank,...", key));
					}
					tmp = new Long[2];
					tmp[0] = Long.valueOf(kv[0]);
					tmp[1] = Long.valueOf(kv[1]);
					idList.add(tmp);
				}
			}
			configuration.withShopCategoryList(idList);
		} else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}
		
		// item.url.patterns
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_ITEM_URL_PATTERNS);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			String[] patterns = value.split(",");
			for(String pattern : patterns) {
				if(StringUtil.isNotBlank(pattern)) {
					configuration.addItemPattern(StringUtil.trim(pattern));
				}
			}
			if(null == configuration.itemPatternList || configuration.itemPatternList.isEmpty()) {
				throw new IllegalArgumentException(String.format("Please set right value for key:%s", key));
			}
		} else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}
		
		// item.id.between
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_ITEM_ID_BETWEEN);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			String[] vs = value.split(",");
			if(vs.length > 2) {
				throw new IllegalArgumentException(String.format("Please set right value for key:%s", key));
			}
			if(StringUtil.isBlank(vs[0])) {
				throw new IllegalArgumentException(String.format("Please set right value for key:%s", key));
			}
			vs[0] = StringUtil.trim(vs[0]);
			if(vs.length == 2) {
				vs[1] = StringUtil.trim(vs[1]);
			}
			configuration.withItemIdBetween(vs);
		} else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}
		
		// item.name.csspath
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_ITEM_NAME_CSS_PATH);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withItemNameCSSPath(StringUtil.trim(value));
		} else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}
		
		// item.catgoryPath.csspath
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_ITEM_CATEGORY_PATH_CSS_PATH);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withCategoryPathCSSPath(StringUtil.trim(value));
		} else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}
		
		// item.catgoryPath.skips
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_ITEM_CATEGORY_PATH_SKIPS);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			List<Integer> tmpList = new ArrayList<Integer>(2);		// 一般2个
			String[] skips = value.split(",");
			for(String skip : skips) {
				if(StringUtil.isNotBlank(skip)) {
					tmpList.add(Integer.valueOf(StringUtil.trim(skip)));
				}
			}
			int i = 0;
			int[] tmps = new int[tmpList.size()];
			for(Integer n : tmpList) {
				tmps[i++] = n;
			}
			configuration.withCatgoryPathSkips(tmps);
		}/* else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}*/
		
		// item.price.csspath
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_ITEM_PRICE_CSS_PATH);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withPriceCSSPath(StringUtil.trim(value));
		} else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}
		
		// item.price.attribute
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_ITEM_PRICE_ATTRIBUTE);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withPriceAttribute(StringUtil.trim(value));
		}/* else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}*/
		
		// item.promitionPrice.csspath
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_ITEM_PROMOTION_PRICE_CSS_PATH);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withPromotionPriceCSSPath(StringUtil.trim(value));
		}/* else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}*/
		
		// item.price.removes
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_ITEM_PRICE_REMOVES);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			List<String> tmpList = new ArrayList<String>(2);		// 一般2个
			String[] skips = value.split("#");
			for(String skip : skips) {
				if(StringUtil.isNotBlank(skip)) {
					tmpList.add(StringUtil.trim(skip));
				}
			}
			configuration.withPriceRemoves(tmpList.toArray(new String[0]));
		} /*else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}*/
		
		// item.logo.csspath
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_ITEM_LOGO_CSS_PATH);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withLogoCSSPath(StringUtil.trim(value));
		} else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}
		
		// item.logo.attribute
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_ITEM_LOGO_ATTRIBUTE);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withLogoAttribute(StringUtil.trim(value));
		}/* else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}*/
		
		// item.oneStartCSSPath
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_ITEM_BRAND);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withBrandCSSPath(StringUtil.trim(value));
		}/* else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}*/
				
		// item.oneStartCSSPath
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_ITEM_ONE_START);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withOneStartCSSPath(StringUtil.trim(value));
		}/* else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}*/
		
		// item.secondStartCSSPath
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_ITEM_SECOND_START);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withSecondStartCSSPath(StringUtil.trim(value));
		}/* else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}*/
		
		// item.threeStartCSSPath
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_ITEM_THREE_START);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withThreeStartCSSPath(StringUtil.trim(value));
		}/* else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}*/
		
		// item.fourStartCSSPath
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_ITEM_FOUR_START);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withFourStartCSSPath(StringUtil.trim(value));
		}/* else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}*/
		
		// item.fiveStartCSSPath
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_ITEM_FIVE_START);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withFiveStartCSSPath(StringUtil.trim(value));
		}/* else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}*/
		
		// item.groovyScriptFile
		key = String.format("%s.%s.%s", ParserConfiguration.PARSER_CONFIGURATION_PREFIX, parser, ParserConfiguration.PARSER_CONFIGURATION_ITEM_GROOVY_SCRIPT_FILE);
		value = properties.getProperty(key);
		if(StringUtil.isNotBlank(value)) {
			configuration.withGroovyScriptFile((StringUtil.trim(value)));
		}/* else {
			throw new IllegalArgumentException(String.format("Please set value for key:%s", key));
		}*/
				
		CommonHtmlParser htmlParser = new CommonHtmlParser();
		htmlParser.init(configuration);
		return htmlParser;
	}
	/**
	 * 
	 * @param strURL
	 * @param content
	 * @param charset
	 */
	public static void parse(String strURL, byte[] content, String charset) {
		logger.warn("HtmlParser.threadPool:");
		logger.warn(String.format("corePoolSize:%d\n" +
	    		"maximumPoolSize:%d\n" +
	    		"activeCount:%d\n" +
	    		"poolSize:%d\n" +
	    		"workQueueSize:%d\n" +
	    		"workQueueRemainingCapacity:%d", 
	    		threadPool.getCorePoolSize(), 
	    		threadPool.getMaximumPoolSize(), 
	    		threadPool.getActiveCount(), 
	    		threadPool.getPoolSize(),
	    		threadPool.getQueue().size(),
	    		threadPool.getQueue().remainingCapacity()));
		ParseHtmlTask task = new ParseHtmlTask(strURL, content, charset);
		while(!submit(task)) {
			try {
				logger.warn(String.format("HtmlParser.threadPool is full, so try to sleep %d ms, then retry", 100));
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.error(e);
				Thread.currentThread().interrupt();
			}
		}
	}
	
	private static boolean submit(ParseHtmlTask task) {
		try {
			threadPool.execute(task);
			return true;
		} catch (RejectedExecutionException e) {
			return false;
		}
	}
	
	/**
	 * 
	 */
	public static void stop() {
		if(null != threadPool) {
			threadPool.shutdown();
		}
		if(null != workQueue) {
			workQueue.clear();
		}
	}
	
	/**
	 * 
	 */
	private static class ParseHtmlTask implements Runnable {
		
		private String url;
		private byte[] content;
		private String charset;
		
		public ParseHtmlTask(String url, byte[] content, String charset) {
			this.url = url;
			this.content = content;
			this.charset = charset;
		}

		@Override
		public void run() {
			for(HtmlParser parser : htmlParserChain) {
				try {
					parser.parse(url, content, charset);
				} catch (Throwable t) {
					t.printStackTrace();
					logger.error(String.format("Parse html failed, url:%s", url), t);
				}
			}
		}
	}
	
	/**
	 * 
	 * @author sihai
	 *
	 */
	private static class ParserConfiguration {

		//=========================================================
		//					Constants
		//=========================================================
		static final String PARSER_CONFIGURATION_PREFIX = "com.ihome.crawler.parser";
		static final String PARSER_CONFIGURATION_PARSERS = "parsers";
		static final String PARSER_CONFIGURATION_CHARSET = "charset";
		static final String PARSER_CONFIGURATION_PLATFORM = "platform";
		static final String PARSER_CONFIGURATION_PLATFORM_NAME = "platform.name";
		static final String PARSER_CONFIGURATION_PLATFORM_URL = "platform.url";
		
		
		static final String PARSER_CONFIGURATION_SHOP_NAME = "shop.name";
		static final String PARSER_CONFIGURATION_SHOP_ID = "shop.id";
		static final String PARSER_CONFIGURATION_SHOP_SELLER_NAME = "shop.sellerName";
		static final String PARSER_CONFIGURATION_SHOP_URL = "shop.url";
		static final String PARSER_CONFIGURATION_SHOP_LOGO_URL = "shop.logoURL";
		static final String PARSER_CONFIGURATION_SHOP_IS_RECOMMEND = "shop.isRecommend";
		static final String PARSER_CONFIGURATION_SHOP_RANK = "shop.rank";
		static final String PARSER_CONFIGURATION_SHOP_PAYWAYS= "shop.payways";
		static final String PARSER_CONFIGURATION_SHOP_DISTRIBUTE_WAYS = "shop.distributeWays";
		static final String PARSER_CONFIGURATION_SHOP_CATEGORY_LIST = "shop.categoryList";
		
		static final String PARSER_CONFIGURATION_ITEM_URL_PATTERNS = "item.url.patterns";
		static final String PARSER_CONFIGURATION_ITEM_ID_BETWEEN = "item.id.between";
		static final String PARSER_CONFIGURATION_ITEM_NAME_CSS_PATH = "item.name.csspath";
		static final String PARSER_CONFIGURATION_ITEM_CATEGORY_PATH_CSS_PATH = "item.catgoryPath.csspath";
		static final String PARSER_CONFIGURATION_ITEM_CATEGORY_PATH_SKIPS = "item.catgoryPath.skips";
		static final String PARSER_CONFIGURATION_ITEM_PRICE_CSS_PATH = "item.price.csspath";
		static final String PARSER_CONFIGURATION_ITEM_PRICE_ATTRIBUTE = "item.price.attribute";
		static final String PARSER_CONFIGURATION_ITEM_PROMOTION_PRICE_CSS_PATH = "item.promitionPrice.csspath";
		static final String PARSER_CONFIGURATION_ITEM_PRICE_REMOVES = "item.price.removes";
		static final String PARSER_CONFIGURATION_ITEM_LOGO_CSS_PATH = "item.logo.csspath";
		static final String PARSER_CONFIGURATION_ITEM_LOGO_ATTRIBUTE = "item.logo.attribute";
		
		static final String PARSER_CONFIGURATION_ITEM_BRAND = "item.brand.csspath";
		
		static final String PARSER_CONFIGURATION_ITEM_ONE_START = "item.oneStart.csspath";
		static final String PARSER_CONFIGURATION_ITEM_SECOND_START = "item.secondStart.csspath";
		static final String PARSER_CONFIGURATION_ITEM_THREE_START = "item.threeStart.csspath";
		static final String PARSER_CONFIGURATION_ITEM_FOUR_START = "item.fourStart.csspath";
		static final String PARSER_CONFIGURATION_ITEM_FIVE_START = "item.fiveStart.csspath";
		
		static final String PARSER_CONFIGURATION_ITEM_GROOVY_SCRIPT_FILE = "item.groovyScriptFile";
		
		String		 charset = URLUtil.DEFAULT_CHARSET;					//
		
		int			 platform;					// 
		String		 platformName;				// 
		String	     platformURL;				// 
		
		String		 shopName;					//
		String		 shopId;					//
		String 		 sellerName;				//
		String		 shopURL;					//
		String		 shopLogoURL;				// 
		Boolean 	 isShopRecommend = false;	//
		Long		 shopRank;					//
		String 		 shopPayways;				//
		String 		 shopDistributeWays;		//
		List<Long[]> shopCategoryList;			//
		
		List<String> itemPatternList;			// 
		String[]	 itemIdBetween;				// 
		String		 itemNameCSSPath;			// 
		String 		 categoryPathCSSPath;		// 
		int[]		 catgoryPathSkips;			// 
		String		 priceCSSPath;				// 
		String	     priceAttribute;			//
		String 		 promotionPriceCSSPath;		// 
		String[]	 priceRemoves;				// 
		String		 logoCSSPath;				// 
		String		 logoAttribute = "src";		// 
		
		String		 brandCSSPath;				//
		String 		 oneStartCSSPath;			//
		String 		 secondStartCSSPath;		//
		String 		 threeStartCSSPath;			//
		String 		 fourStartCSSPath;			//
		String 		 fiveStartCSSPath;			//
		
		String 		 groovyScriptFile;			// 
		
		//===========================================================
		//					DSL
		//===========================================================
		public static ParserConfiguration newConfiguration() {
			return new ParserConfiguration();
		}
		
		public ParserConfiguration withCharset(String charset) {
			this.charset = charset;
			return this;
		}
		
		public ParserConfiguration withPlatform(int platform) {
			this.platform = platform;
			return this;
		}
		
		public ParserConfiguration withPlatformName(String platformName) {
			this.platformName = platformName;
			return this;
		}
		
		public ParserConfiguration withPlatformURL(String platformURL) {
			this.platformURL = platformURL;
			return this;
		}
		
		public ParserConfiguration withShopName(String shopName) {
			this.shopName = shopName;
			return this;
		}
		
		public ParserConfiguration withShopId(String shopId) {
			this.shopId = shopId;
			return this;
		}
		
		public ParserConfiguration withSellerName(String sellerName) {
			this.sellerName = sellerName;
			return this;
		}
		
		public ParserConfiguration withShopURL(String shopURL) {
			this.shopURL = shopURL;
			return this;
		}
		
		public ParserConfiguration withShopLogoURL(String shopLogoURL) {
			this.shopLogoURL = shopLogoURL;
			return this;
		}
		
		public ParserConfiguration withIsShopRecommend(Boolean isShopRecommend) {
			this.isShopRecommend = isShopRecommend;
			return this;
		}
		
		public ParserConfiguration withShopRank(Long shopRank) {
			this.shopRank = shopRank;
			return this;
		}
		
		public ParserConfiguration withShopPayways(String shopPayways) {
			this.shopPayways = shopPayways;
			return this;
		}
		
		public ParserConfiguration withShopDistributeWays(String shopDistributeWays) {
			this.shopDistributeWays = shopDistributeWays;
			return this;
		}
		
		public ParserConfiguration withShopCategoryList(List<Long[]> shopCategoryList) {
			this.shopCategoryList = shopCategoryList;
			return this;
		}
		
		public ParserConfiguration withItemPatterns(String[] patterns) {
			if(null == itemPatternList) {
				itemPatternList = new ArrayList<String>(1);		// 大部分情况还是只有一个的
			}
			for(String p : patterns) {
				if(StringUtil.isNotBlank(p)) {
					itemPatternList.add(StringUtil.trim(p));
				}
			}
			return this;
		}
		
		public ParserConfiguration addItemPattern(String pattern) {
			if(null == itemPatternList) {
				itemPatternList = new ArrayList<String>(1);		// 大部分情况还是只有一个的
			}
			itemPatternList.add(pattern);
			return this;
		}
		
		public ParserConfiguration withItemIdBetween(String[] itemIdBetween) {
			this.itemIdBetween = itemIdBetween;
			return this;
		}
		
		public ParserConfiguration withItemNameCSSPath(String itemNameCSSPath) {
			this.itemNameCSSPath = itemNameCSSPath;
			return this;
		}
		
		public ParserConfiguration withCategoryPathCSSPath(String categoryPathCSSPath) {
			this.categoryPathCSSPath = categoryPathCSSPath;
			return this;
		}
		
		public ParserConfiguration withCatgoryPathSkips(int[] catgoryPathSkips) {
			this.catgoryPathSkips = catgoryPathSkips;
			return this;
		}
		
		public ParserConfiguration withPriceCSSPath(String priceCSSPath) {
			this.priceCSSPath = priceCSSPath;
			return this;
		}
		
		public ParserConfiguration withPriceAttribute(String priceAttribute) {
			this.priceAttribute = priceAttribute;
			return this;
		}
		
		public ParserConfiguration withPromotionPriceCSSPath(String promotionPriceCSSPath) {
			this.promotionPriceCSSPath = promotionPriceCSSPath;
			return this;
		}
		
		public ParserConfiguration withPriceRemoves(String[] priceRemoves) {
			this.priceRemoves = priceRemoves;
			return this;
		}
		
		public ParserConfiguration withLogoCSSPath(String logoCSSPath) {
			this.logoCSSPath = logoCSSPath;
			return this;
		}
		
		public ParserConfiguration withLogoAttribute(String logoAttribute) {
			this.logoAttribute = logoAttribute;
			return this;
		}
		
		public ParserConfiguration withBrandCSSPath(String brandCSSPath) {
			this.brandCSSPath = brandCSSPath;
			return this;
		}
		
		public ParserConfiguration withOneStartCSSPath(String oneStartCSSPath) {
			this.oneStartCSSPath = oneStartCSSPath;
			return this;
		}

		public ParserConfiguration withSecondStartCSSPath(String secondStartCSSPath) {
			this.secondStartCSSPath = secondStartCSSPath;
			return this;
		}
		
		public ParserConfiguration withThreeStartCSSPath(String threeStartCSSPath) {
			this.threeStartCSSPath = threeStartCSSPath;
			return this;
		}
		
		public ParserConfiguration withFourStartCSSPath(String fourStartCSSPath) {
			this.fourStartCSSPath = fourStartCSSPath;
			return this;
		}
		
		public ParserConfiguration withFiveStartCSSPath(String fiveStartCSSPath) {
			this.fiveStartCSSPath = fiveStartCSSPath;
			return this;
		}
		
		public ParserConfiguration withGroovyScriptFile(String groovyScriptFile) {
			this.groovyScriptFile = groovyScriptFile;
			return this;
		}
	}
	
	private static class CommonHtmlParser extends AbstractHtmlParser {

		private ParserConfiguration configuration;
		
		//
		private List<Pattern> itemPatternList;
		
		private Object   targetObject;		//
		private Method	 targetMethod;		//
		
		private ShopDO shop;
		
		public void init(ParserConfiguration configuration) {
			this.configuration = configuration;
			_init_();
		}
		
		private void _init_() {
			
			// TODO check
			
			itemPatternList = new ArrayList<Pattern>(configuration.itemPatternList.size());
			for(String p : configuration.itemPatternList) {
				itemPatternList.add(Pattern.compile(p, Pattern.CASE_INSENSITIVE));
			}
			
			// init script
			/*if(StringUtil.isNotBlank(configuration.groovyScriptFile)) {
				GroovyClassLoader loader = new GroovyClassLoader(HtmlParserHelper.class.getClassLoader());
				//System.out.println(groovyScript);
				try {
					Class<?> groovyRuleClass = loader.parseClass(loader.getResourceAsStream(configuration.groovyScriptFile));
					targetObject = groovyRuleClass.newInstance();
					targetMethod = targetObject.getClass().getMethod("execute", ItemDO.class, Document.class);
					targetMethod.setAccessible(true);
				} catch (CompilationFailedException e) {
					throw new IllegalArgumentException(String.format("Compile groovy script file:%s failed, script:\n", configuration.groovyScriptFile), e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(String.format("New instance for class of groovy script file:%s failed, script:\n%s", configuration.groovyScriptFile), e);
				} catch (InstantiationException e) {
					throw new RuntimeException(String.format("New instance for class of groovy script file:%s failed, script:\n%s", configuration.groovyScriptFile), e);
				} catch (NoSuchMethodException e) {
					throw new RuntimeException(String.format("Get execute method for class of groovy script file:%s failed, script:\n%s", configuration.groovyScriptFile), e);
				} catch (SecurityException e) {
					throw new RuntimeException(String.format("Get execute method for class of groovy script file:%s failed, script:\n%s", configuration.groovyScriptFile), e);
				} finally {
					if(null != loader) {
						// XXX 1.7 up
						try {
							loader.close();
						} catch (IOException e) {
							logger.error(e);
						}
					}
				}
			}*/
			
			_init_shop_();
		}
		
		private void _init_shop_() {
			shop = new ShopDO();
			shop.setId(Long.valueOf(configuration.platform));
			shop.setPlatform(configuration.platform);
			shop.setShopId(configuration.shopId);
			shop.setName(configuration.shopName);
			shop.setSellerName(configuration.sellerName);
			shop.setDetailURL(configuration.shopURL);
			shop.setLogoURL(configuration.shopLogoURL);
			shop.setStatus(ShopStatusEnum.SHOP_STATUS_NORMAL.getValue());
			shop.setIsRecommend(configuration.isShopRecommend);
			shop.setRank(configuration.shopRank);
			shop.setPayways(configuration.shopPayways);
			shop.setDistributeWays(configuration.shopDistributeWays);
			// shopCategory
			if(null != configuration.shopCategoryList && !configuration.shopCategoryList.isEmpty()) {
				ShopCategoryDO shopCategory = null;
				CategoryDO category = null;
				for(Long[] id : configuration.shopCategoryList) {
					shopCategory = new ShopCategoryDO();
					shopCategory.setShop(shop);
					category = new CategoryDO();
					category.setId(id[0]);
					shopCategory.setCategory(category);
					shopCategory.setRank(id[1]);
				}
			}
			shop.setIsDeleted(false);
		}
		
		@Override
		protected boolean accept(String strURL) {
			for(Pattern p : itemPatternList) {
				if(p.matcher(strURL).matches()) {
					return true;
				}
			}
			return false;
		}

		@Override
		protected ItemDO doParse(String strURL, byte[] content, String charset) {
			
			ItemDO item = new ItemDO();
			item.setPlatform(configuration.platform);
			item.setShop(shop);
			item.setDetailURL(strURL);
			//item.setStuffStatus(StuffStatusEnum.STUFF_NEW.getValue());
			item.setNumber(-1L);
			item.setStatus(ItemStatusEnum.ITEM_STATUS_ON_SALE.getValue());
			item.setFreightFeePayer(FreightFeePayerEnum.FREIGHT_FEE_PALYER_SELLER.getValue());
			item.setIsDeleted(false);
			item.setGmtCreate(new Date());
			item.setGmtModified(item.getGmtCreate());
			
			String html = null;
			try {
				html = new String(content, configuration.charset);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(String.format("Please make sure the charset of url:%s, try to use charset:%s", strURL, charset));
			}
			//write2File("/home/sihai/ihome/daoyao.html", html, charset);
			
			Document document = Jsoup.parse(html);
			Elements es = null;
			
			// ItemId
			int index0 = strURL.lastIndexOf(configuration.itemIdBetween[0]);
			if(-1 != index0) {
				if(configuration.itemIdBetween.length == 2) {
					int index1 = strURL.lastIndexOf(configuration.itemIdBetween[1]);
					if(-1 != index1) {
						item.setItemId(strURL.substring(index0 + configuration.itemIdBetween[0].length(), index1));
					} else {
						item.setItemId(strURL.substring(index0 + configuration.itemIdBetween[0].length()));
					}
				} else {
					item.setItemId(strURL.substring(index0 + configuration.itemIdBetween[0].length()));
				}
			} else {
				item.setItemId(strURL);
			}
			
			// Name
			es = document.select(configuration.itemNameCSSPath);
			if(!es.isEmpty()) {
				item.setName(es.first().ownText());
			}
			
			// category
			List<String> categoryPath = new ArrayList<String>(3);
			es = document.select(configuration.categoryPathCSSPath);
			if(!es.isEmpty()) {
				int i = 0;
				boolean isRemoved = false;
				for (Element e : es) {
					isRemoved = false;
					++i;
					if(null != configuration.catgoryPathSkips) {
						for(int skip : configuration.catgoryPathSkips) {
							if(i == skip) {
								isRemoved = true;
								break;
							}
						}
					}
					if(!isRemoved) {
						categoryPath.add(e.html());
					}
				}
			}
			
			// 生成类目树
			CategoryDO category = generateCategoryTree(configuration.platform, categoryPath);
			item.setCategory(category);
			
			// price
			es = document.select(configuration.priceCSSPath);
			if(!es.isEmpty()) {
				String tmp = null;
				if(null == configuration.priceAttribute) {
					tmp = es.first().html();
				} else {
					tmp = es.first().attr(configuration.priceAttribute);
				}
				if(null != configuration.priceRemoves) {
					for(String r : configuration.priceRemoves) {
						tmp = tmp.replaceAll(r, "");
					}
				}
				item.setPrice(Double.valueOf(StringUtil.trim(tmp)));
			}

			// promotion price
			if(null != configuration.promotionPriceCSSPath) {
				es = document.select(configuration.promotionPriceCSSPath);
				if(!es.isEmpty()) {
					String tmp = es.first().html();
					if(null != configuration.priceRemoves) {
						for(String r : configuration.priceRemoves) {
							tmp = tmp.replaceAll(r, "");
						}
					}
					item.setPrice(Double.valueOf(StringUtil.trim(tmp)));
				}
			}
			
			// photo
			es = document.select(configuration.logoCSSPath);
			if(!es.isEmpty()) {
				item.setLogoURL(generatePhoto(strURL, es.first().attr(configuration.logoAttribute)));
			}
			
			// gifts
			// 取自 http://jprice.360buy.com/pageadword/itemId-1-1.html
			/*es = document.select("li#summary-gifts > div.dd > div.li-img");
			if(!es.isEmpty()) {
				List<Map<String, Object>> gifts = new ArrayList<Map<String, Object>>();
				for(Element e : es) {
					Map<String, Object> gift = new HashMap<String, Object>();
					gift.put("name", e.child(0).child(0).html());
					gift.put("photo", e.child(0).child(0).attr("src"));
					gift.put("number", e.child(1).html());
					gifts.add(gift);
				}
				item.setGifts(JSONObject.fromObject(gifts).toString());
			}*/
			
			if(null != configuration.brandCSSPath) {
				// TODO
				es = document.select(configuration.brandCSSPath);
				if(!es.isEmpty()) {
					BrandDO brand = new BrandDO();
					brand.setName(StringUtil.trim(es.first().html()));
					brand.setStatus(BrandStatusEnum.NORMAL.getValue());
					brand.setIsDeleted(false);
					item.setBrand(brand);
				}
			}
			
			/*// one start count
			if(null != configuration.oneStartCSSPath) {
				es = document.select(configuration.oneStartCSSPath);
				if(!es.isEmpty()) {
					item.setOneStartCount(Long.valueOf(StringUtil.trim(es.first().html())));
				}
			}
			// second start count
			if(null != configuration.secondStartCSSPath) {
				es = document.select(configuration.secondStartCSSPath);
				if(!es.isEmpty()) {
					item.setSecondStartCount(Long.valueOf(StringUtil.trim(es.first().html())));
				}
			}
			// three start count
			if(null != configuration.threeStartCSSPath) {
				es = document.select(configuration.threeStartCSSPath);
				if(!es.isEmpty()) {
					item.setThreeStartCount(Long.valueOf(StringUtil.trim(es.first().html())));
				}
			}
			// four start count
			if(null != configuration.fourStartCSSPath) {
				es = document.select(configuration.fourStartCSSPath);
				if(!es.isEmpty()) {
					item.setFourStartCount(Long.valueOf(StringUtil.trim(es.first().html())));
				}
			}
			// five start count
			if(null != configuration.fiveStartCSSPath) {
				es = document.select(configuration.fiveStartCSSPath);
				if(!es.isEmpty()) {
					item.setFiveStartCount(Long.valueOf(StringUtil.trim(es.first().html())));
				}
			}*/
			
			// groovy script
			/*if(StringUtil.isNotBlank(configuration.groovyScriptFile) && null != targetObject && null != targetMethod) {
				try {
					targetMethod.invoke(targetObject, item, document);
				} catch (InvocationTargetException e) {
					throw new IllegalArgumentException(String.format("调用方法execute失败, groovy script file:%s ", configuration.groovyScriptFile), e);
				} catch (IllegalAccessException e) {
					throw new IllegalArgumentException(String.format("调用方法execute失败, groovy script file:%s ", configuration.groovyScriptFile), e);
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException(String.format("调用方法execute失败, groovy script file:%s ", configuration.groovyScriptFile), e);
				}
			}*/
			// 先用java代码调试
			
			//executeGroovy(item, document);
			
			return item;
		}
		
		private void executeGroovy(ItemDO item, Document document) {
			/*String tmpStr = null;
			Elements tmp = null;
			CommentDO comment = null;
			List<CommentDO> commentList = new ArrayList<CommentDO>();
			Elements es = document.select("div#revMHRL");
			if(!es.isEmpty()) {
				for(Element e : es.first().children()) {
					comment = new CommentDO();
					comment.setOwner(item);
					// publisher
					tmp = e.select("div.mt4.ath > span.gr10 > span.txtsmall > a");
					if(!tmp.isEmpty()) {
						comment.setPublisher(StringUtil.trim(tmp.html()));
					}
					// score
					tmp = e.select("div.mt4.ttl > span.swSprite > span");
					if(!tmp.isEmpty()) {
						tmpStr = tmp.html();
						if(tmpStr.contains("5.0")) {
							comment.setScore(5.0D);
						} else if(tmpStr.contains("4.0")) {
							comment.setScore(4.0D);
						} else if(tmpStr.contains("3.0")) {
							comment.setScore(3.0D);
						} else if(tmpStr.contains("2.0")) {
							comment.setScore(2.0D);
						} else if(tmpStr.contains("1.0")) {
							comment.setScore(1.0D);
						} else {
							logger.error(String.format("Unknown score string:%s", tmpStr));
						}
					}
					// is buy ?
					tmp = e.select("div.txtsmall.mt4.fvavp > span.inlineblock.avpOrVine > span.orange.strong.avp");
					if(!tmp.isEmpty()) {
						if(StringUtil.trim(tmp.first().html()).equals("购买过此商品")) {
							comment.setIsBuy(true);
						}
					}
					// content
					tmp = e.select("div.mt9.reviewText");
					if(!tmp.isEmpty()) {
						comment.setContent(tmp.html());
					}
					// type
					comment.setType(CommentTypeEnum.ITEM.getValue());
					// publishTime
					tmp = e.select("div.mt4.ttl > span.gry.valignMiddle > span.inlineblock.txtsmall");
					if(!tmp.isEmpty()) {
						try {
							comment.setPublishTime(DateUtil.parse(StringUtil.trim(tmp.html()), "yyyy年MM月dd日"));
						} catch (ParseException ex) {
							ex.printStackTrace();
						}
					}
					comment.setIsDeleted(false);
					commentList.add(comment);
				}
				item.setCommentList(commentList);
			}*/
		}
	}
}