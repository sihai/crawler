/**
 * 
 */
package com.ihome.matrix;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	
	private static final Log logger = LogFactory.getLog(MatrixContrller.class);
	
	public static void main(String[] args) {
		
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                 if (!(e instanceof ThreadDeath)) {
                	 logger.error("OOPS:", e);
                     System.err.print(String.format("Exception in thread: %s", t.getName()));
                     e.printStackTrace(System.err);
                 }   
            }
        });
		
		try {
			String crawlStorageFolder = "/home/sihai/ihome/matrix";
			int numberOfCrawlers = 32;
	
		    CrawlConfig config = new CrawlConfig();
		    //config.setResumableCrawling(true);
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
	
		    /*
		     * For each crawl, you need to add some seed urls. These are the first
		     * URLs that are fetched and then the crawler starts following links
		     * which are found in these pages
		     */
		    //===============================================
		    //			化妆品
		    //===============================================
		    // skii
		    controller.addSeed("http://skii.tmall.com/?spm=3.1000473.295283.17.2XAavo");
		    // 面部护肤
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295283.1.BEwI8a&active=1&from=sn_1_rightnav&area_code=310000&search_condition=7&vmarket=0&wwonline=1&style=g&sort=s&start_price=200&n=60&s=0&cat=50026502");
		    // 美体瘦身
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295283.37.BEwI8a&active=1&from=sn_1_rightnav&area_code=330100&search_condition=7&style=g&sort=s&start_price=20&n=60&s=0&cat=50026504");
		    // 精油芳疗
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295283.48.BEwI8a&active=1&from=sn_1_rightnav&area_code=330100&search_condition=7&vmarket=0&wwonline=1&style=g&sort=s&start_price=50&n=60&s=0&cat=50026505");
		    // 时尚彩妆
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295283.52.BEwI8a&active=1&from=sn_1_rightnav&area_code=330100&search_condition=7&vmarket=0&wwonline=1&style=g&sort=s&start_price=20&n=60&s=0&cat=50026391");
		    // 经典香氛
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295283.83.BEwI8a&active=1&from=sn_1_rightnav&area_code=310000&search_condition=7&vmarket=0&style=g&sort=s&start_price=130&n=60&s=0&cat=50026393");
		    // 美妆工具
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295283.91.BEwI8a&active=1&from=sn_1_rightnav&area_code=310000&search_condition=7&style=g&sort=s&q=%BC%D9%B7%A2&n=60&s=0&cat=50026426");
		    // 男士护肤
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295283.100.BEwI8a&active=1&from=sn_1_rightnav&area_code=330100&search_condition=7&vmarket=0&wwonline=1&style=g&sort=s&start_price=20&n=60&s=0&cat=50026506");
		    // 美发/个人护理
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295283.118.BEwI8a&active=1&from=sn_1_rightnav&area_code=330100&search_condition=7&style=g&sort=s&n=60&s=0&cat=50043479");
		   
		    //===============================================
		    //			母婴
		    //===============================================
		    // 宝宝食品
		    controller.addSeed("http://list.tmall.com/50025137/g-d-----40-0--50025137-x.htm?spm=3.1000473.295289.1.BEwI8a&TBG=19624.15484.1");
		    // 宝宝用品
		    controller.addSeed("http://list.tmall.com/50024803/g-d-----40-0--50024803-x.htm?spm=3.1000473.295289.25.BEwI8a&TBG=19624.15484.6");
		    // 童装童鞋
		    controller.addSeed("http://list.tmall.com/50023647/g-st-----40-0--50023647-x.htm?spm=3.1000473.295289.63.BEwI8a&TBG=19624.15484.46");
		    // 孕妈专区
		    controller.addSeed("http://list.tmall.com/50024803/g-s-----40-0--50029253-x.htm?spm=3.1000473.295289.96.BEwI8a&TBG=19624.15484.41");
		    // 早教/玩具
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295289.119.BEwI8a&area_code=330100&TBG=54622.101440.22&search_condition=16&style=g&sort=st&n=42&s=0&cat=50033500");
		    // 毛绒/模型
		    controller.addSeed("http://list.tmall.com/50025163/g-s-----40-0--50021187-x.htm?spm=3.1000473.295289.144.BEwI8a");
		    
		    //===============================================
		    //			医药保健
		    //===============================================
		    // 品牌保健品
		    controller.addSeed("http://list.tmall.com/50072043/g-d-----40-0--50072043-x.htm?spm=3.1000473.295291.1.BEwI8a");
		    // 传统滋补品
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295291.26.BEwI8a&active=1&area_code=330100&hotsale=0&search_condition=16&vmarket=0&style=g&sort=st&n=40&s=0&cat=50072030");
		    // 中西药品
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295291.53.BEwI8a&cat=50074804");
		    // 医疗器械
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295291.81.BEwI8a&active=1&from=sn_1_cat&area_code=330100&search_condition=7&style=g&sort=s&n=60&s=0&cat=50074901#J_crumbs");
		    // 计生用品
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295291.104.BEwI8a&cat=50074917");
		    // 隐形眼镜/护理液
		    controller.addSeed("http://list.tmall.com/search_product.htm?spm=3.1000473.295291.120.BEwI8a&active=1&from=sn_1_cat&area_code=330100&search_condition=7&style=g&sort=s&n=60&s=0&cat=50074933#J_crumbs");
		    
		    // init 
		    URLParserHelper.init();
		    HtmlParserHelper.init();
		    
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
