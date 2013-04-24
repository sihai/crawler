/**
 * 
 */
package com.ihome.matrix;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.matrix.bridge.MatrixBridge;
import com.ihome.matrix.domain.ShopDO;
import com.ihome.matrix.model.ResultModel;
import com.ihome.matrix.model.ShopQueryModel;
import com.ihome.matrix.parser.html.HtmlParserHelper;
import com.ihome.matrix.parser.url.URLParserHelper;
import com.ihome.matrix.plugin.PluginRepository;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * 
 * @author sihai
 *
 */
public class MatrixContrller {
	
	private static final Log logger = LogFactory.getLog(MatrixContrller.class);
	
	private static final String COMMENT_TAG = "#";
	private static final String SEED_FILE_NAME = "seed.txt";
	
	private static void initPlugin() {
		PluginRepository.init();
	}
	
	private static void stopPlugin() {
		PluginRepository.stop();
	}
	
	/**
	 * 
	 * @param controller
	 */
	private static void initSeed(CrawlController controller) {
		
		initSeedFromDatabase(controller);
	}
	
	/**
	 * 
	 * @param controller
	 */
	private static void initSeedFromFile(CrawlController controller) {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(SEED_FILE_NAME);
		if(null == is) {
			logger.warn(String.format("Can not load seed from file :%s, can not found this file in classpath", SEED_FILE_NAME));
		} else {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(is));
				String line = null;
				while(null != (line = reader.readLine())) {
					if(StringUtils.isBlank(line) || StringUtils.trim(line).startsWith(COMMENT_TAG)) {
						continue;
					}
					logger.info(String.format("Add seed url:%s", line));
					controller.addSeed(StringUtils.trim(line));
				}
			} catch (FileNotFoundException e) {
				// NOT POSSIABLE
				logger.warn(String.format("Can not load configuration from file :%s, can not found this file", SEED_FILE_NAME), e);
			} catch (IOException e) {
				logger.warn(String.format("Can not load configuration from file :%s, can read this file", SEED_FILE_NAME), e);
			} finally {
				if(null != reader) {
					try {
						reader.close();
					} catch (IOException e) {
						logger.error(e);
					}
				}
				try {
					is.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param controller
	 */
	private static void initSeedFromDatabase(CrawlController controller) {
		Long currentPage = 1L;
		ShopQueryModel queryModel = ShopQueryModel.newInstance();
		ResultModel<ShopDO> result = null;
		for(;;) {
			queryModel.setCurrentPage(currentPage++);
			result = MatrixBridge.getShopManager().query(queryModel);
			if(result.getItemList().isEmpty()) {
				break;
			}
			for(ShopDO shop : result.getItemList()) {
				controller.addSeed(StringUtils.trim(shop.getDetailURL()));
				logger.info(String.format("Add seed url:%s", shop.getDetailURL()));
			}
			result.getItemList().clear();
		}
	}
	
	public static void main(String[] args) {
		
		/*if(1 != args.length) {
			System.err.print("Uage: Matrix configFileName");
			return;
		}*/
		
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                 if (!(e instanceof ThreadDeath)) {
                	 logger.error("OOPS:", e);
                     System.err.print(String.format("Exception in thread: %s, errorMsg:%s", t.getName(), e.getMessage()));
                     e.printStackTrace(System.err);
                 }   
            }
        });
		
		try {
			
			/*Properties properties = new Properties();
			properties.load(new FileInputStream(args[0]));*/
			
			String crawlStorageFolder = "/home/sihai/ihome/matrix";
			int numberOfCrawlers = 32;
	
		    CrawlConfig config = new CrawlConfig();
		    config.setResumableCrawling(false);
		    config.setFollowRedirects(true);
		    config.setMaxDepthOfCrawling(4);
		    config.setMaxOutgoingLinksToFollow(1000);
		    config.setCrawlStorageFolder(crawlStorageFolder);
	
		    /*
		     * Instantiate the controller for this crawl.
		     */
		    PageFetcher pageFetcher = new PageFetcher(config);
		    RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		    RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		    CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
		    //
		    initPlugin();
		    // init seed
		    initSeed(controller);
		    
		    // init
		    MatrixHttpClientWrap.init();
		    URLParserHelper.init();
		    HtmlParserHelper.init();
		    
		    /*
		     * Start the crawl. This is a blocking operation, meaning that your code
		     * will reach the line after this only when crawling is finished.
		     */
		    controller.start(MatrixCrawler.class, numberOfCrawlers);
		    
		    Thread.currentThread().join();
		    
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 
		    stopPlugin();
			MatrixHttpClientWrap.destroy();
			URLParserHelper.stop();
			HtmlParserHelper.stop();
		}
	}
}
