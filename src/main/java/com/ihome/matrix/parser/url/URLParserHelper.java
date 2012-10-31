/**
 * 
 */
package com.ihome.matrix.parser.url;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.matrix.enums.PlatformEnum;

import edu.uci.ics.crawler4j.util.CrawlerThreadFactory;

/**
 * 
 * @author sihai
 *
 */
public class URLParserHelper {

	private static final Log logger = LogFactory.getLog(URLParserHelper.class);
	
	public static final int DEFAULT_MIN_THREAD = 2;
	public static int DEFAULT_MAX_THREAD = 8;
	public static int DEFAULT_MAX_WORK_QUEUE_SIZE = 2048;
	public static long MAX_KEEP_ALIVE_TIME = 60;

	private static int minThread = DEFAULT_MIN_THREAD;
	private static int maxThread = DEFAULT_MAX_THREAD;
	private static int workQueueSize = DEFAULT_MAX_WORK_QUEUE_SIZE;
	private static long keepAliveTime = MAX_KEEP_ALIVE_TIME;		// s

	private static BlockingQueue<Runnable> workQueue;	//
	private static ThreadPoolExecutor threadPool;		//
	
	private static Map<String, URLParser> urlParserMap;

	/**
	 * 
	 */
	public static void init() {
		
		// parser map
		urlParserMap = new HashMap<String, URLParser>();
		TaobaoURLParser parser = new TaobaoURLParser();
		parser.setPlatform(PlatformEnum.PLATFORM_TAOBAO);
		urlParserMap.put("www.taobao.com", parser);
		urlParserMap.put("item.taobao.com", urlParserMap.get("www.taobao.com"));
		
		TaobaoURLParser tmallParser = new TaobaoURLParser();
		tmallParser.setPlatform(PlatformEnum.PLATFORM_TMALL);
		urlParserMap.put("www.tmall.com", tmallParser);
		urlParserMap.put("detail.tmall.com", urlParserMap.get("www.tmall.com"));		

		// thread pool
		workQueue = new LinkedBlockingQueue<Runnable>(workQueueSize);
		threadPool = new ThreadPoolExecutor(minThread, maxThread,
		            keepAliveTime, TimeUnit.SECONDS,
		            workQueue, new CrawlerThreadFactory("URL-Parser", null, true));
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
	 * @param url
	 */
	public static void parse(String strURL) {
		
		try {
			URLParser parser = urlParserMap.get(new URL(strURL).getHost());
			if(null == parser) {
				logger.info(String.format("No parser for url:%s", strURL));
				return;
			}
			threadPool.execute(new ParseURLTask(parser, strURL));
			logger.warn("URLParser.threadPool:");
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
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(String.format("Wrong url:%s", strURL), e);
		}
	}
	
	/**
	   * 
	   * @author sihai
	   *
	   */
	  private static class ParseURLTask implements Runnable {

		  private URLParser parser;
		  private String url;

		  public ParseURLTask(URLParser parser, String url) {
			  this.parser = parser;
			  this.url = url;
		  }

		  @Override
		  public void run() {
			  try {
				  parser.parse(url);
			  } catch (Throwable t) {
				  logger.error(String.format("Parse url:%s failed", url), t);
			  }
		  }
	  }
}
