/**
 * 
 */
package com.ihome.matrix;

import com.ihome.matrix.parser.html.HtmlParserHelper;
import com.ihome.matrix.parser.url.URLParserHelper;

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
	
	public static void main(String[] args) {
		
		try {
			String crawlStorageFolder = "/home/sihai/ihome/matrix";
			int numberOfCrawlers = 16;
	
		    CrawlConfig config = new CrawlConfig();
		    config.setCrawlStorageFolder(crawlStorageFolder);
	
		    /*
		     * Instantiate the controller for this crawl.
		     */
		    PageFetcher pageFetcher = new PageFetcher(config);
		    RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		    RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		    CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
	
		    /*
		     * For each crawl, you need to add some seed urls. These are the first
		     * URLs that are fetched and then the crawler starts following links
		     * which are found in these pages
		     */
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295283.1.BEwI8a&active=1&from=sn_1_rightnav&area_code=310000&search_condition=7&vmarket=0&wwonline=1&style=g&sort=s&start_price=200&n=60&s=0&cat=50026502");
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295283.37.BEwI8a&active=1&from=sn_1_rightnav&area_code=330100&search_condition=7&style=g&sort=s&start_price=20&n=60&s=0&cat=50026504");
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295283.48.BEwI8a&active=1&from=sn_1_rightnav&area_code=330100&search_condition=7&vmarket=0&wwonline=1&style=g&sort=s&start_price=50&n=60&s=0&cat=50026505");
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295283.52.BEwI8a&active=1&from=sn_1_rightnav&area_code=330100&search_condition=7&vmarket=0&wwonline=1&style=g&sort=s&start_price=20&n=60&s=0&cat=50026391");
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295283.83.BEwI8a&active=1&from=sn_1_rightnav&area_code=310000&search_condition=7&vmarket=0&style=g&sort=s&start_price=130&n=60&s=0&cat=50026393");
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295283.91.BEwI8a&active=1&from=sn_1_rightnav&area_code=310000&search_condition=7&style=g&sort=s&q=%BC%D9%B7%A2&n=60&s=0&cat=50026426");
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295283.100.BEwI8a&active=1&from=sn_1_rightnav&area_code=330100&search_condition=7&vmarket=0&wwonline=1&style=g&sort=s&start_price=20&n=60&s=0&cat=50026506");
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295283.118.BEwI8a&active=1&from=sn_1_rightnav&area_code=330100&search_condition=7&style=g&sort=s&n=60&s=0&cat=50043479");
		   
		    // init 
		    URLParserHelper.init();
		    //HtmlParserHelper.init();
		    
		    /*
		     * Start the crawl. This is a blocking operation, meaning that your code
		     * will reach the line after this only when crawling is finished.
		     */
		    controller.start(MatrixCrawler.class, numberOfCrawlers);
		    
		    Thread.currentThread().join();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			URLParserHelper.stop();
			HtmlParserHelper.stop();
		}
	}
}
