/**
 * 
 */
package com.ihome.matrix.parser.html;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.uci.ics.crawler4j.util.CrawlerThreadFactory;

/**
 * 
 * @author sihai
 *
 */
public class HtmlParserHelper {

	private static final Log logger = LogFactory.getLog(HtmlParserHelper.class);
	
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
	
	private static List<HtmlParser> htmlParserChain;
	
	/**
	 * 
	 */
	public static void init() {
		
		// parser chain
		htmlParserChain = new ArrayList<HtmlParser>(16);
		
		// thread pool
		workQueue = new LinkedBlockingQueue<Runnable>(workQueueSize);
		threadPool = new ThreadPoolExecutor(minThread, maxThread,
		            keepAliveTime, TimeUnit.SECONDS,
		            workQueue, new CrawlerThreadFactory("URL-Parser", null, true));
	}
	
	/**
	 * 
	 * @param url
	 * @param html
	 */
	public void parse(String url, String html) {
		
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
		
		private HtmlParser parser;
		private String url;
		private String html;
		
		public ParseHtmlTask(HtmlParser parser, String url, String html) {
			this.parser = parser;
			this.url = url;
			this.html = html;
		}

		@Override
		public void run() {
			try {
				parser.parse(url, html);
			} catch (Throwable t) {
				logger.error(String.format("Parse html failed, url:%s, html:%s", url, html), t);
			}
		}
	}
}