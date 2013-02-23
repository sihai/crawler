/**
 * 
 */
package com.ihome.matrix;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.iacrqq.util.StringUtil;
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
		
		 InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(SEED_FILE_NAME);
		if(null == is) {
			logger.warn(String.format("Can not load seed from file :%s, can not found this file in classpath", SEED_FILE_NAME));
		} else {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(is));
				String line = null;
				while(null != (line = reader.readLine())) {
					if(StringUtil.isBlank(line) || StringUtil.trim(line).startsWith(COMMENT_TAG)) {
						continue;
					}
					logger.info(String.format("Add seed url:%s", line));
					controller.addSeed(StringUtil.trim(line));
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
                     System.err.print(String.format("Exception in thread: %s", t.getName()));
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
	
		    /*
		     * For each crawl, you need to add some seed urls. These are the first
		     * URLs that are fetched and then the crawler starts following links
		     * which are found in these pages
		     */
		    /*//===============================================
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
		    
		    //===============================================
		    //			Amazon
		    //===============================================
		    // 个护健康
		    controller.addSeed("http://www.amazon.cn/%E4%B8%AA%E6%8A%A4%E5%81%A5%E5%BA%B7/b/ref=sd_allcat_hpc_?ie=UTF8&node=852803051");
		    // 美容化妆
		    controller.addSeed("http://www.amazon.cn/%E7%BE%8E%E5%AE%B9%E5%8C%96%E5%A6%86/b/ref=sd_allcat_bty_?ie=UTF8&node=746776051");
		    
		    //===============================================
		    //			Coo8
		    //===============================================
		    controller.addSeed("http://www.coo8.com/baojianjiankang/");
		    controller.addSeed("http://www.coo8.com/meizhuanggehu/");
		    
		    //===============================================
		    //			dangdang
		    //===============================================
		    // 
		    controller.addSeed("http://cosmetic.dangdang.com/");
		    // 
		    controller.addSeed("http://health.dangdang.com/");
		    
		    //===============================================
		    //			360Buy
		    //===============================================
		    // 个护化妆
		    controller.addSeed("http://www.360buy.com/beauty.html");
		    // 母婴
		    controller.addSeed("http://www.360buy.com/baby.html");
		    // 营养健康
		    controller.addSeed("http://www.360buy.com/products/1320-1586-000.html");
		    // 亚健康调理
		    controller.addSeed("http://www.360buy.com/products/1320-1587-000.html");
		    // 健康礼品
		    controller.addSeed("http://www.360buy.com/products/1320-1588-000.html");
		    
		    //===============================================
		    //			Gome
		    //===============================================
		    // 瑜伽垫
		    controller.addSeed("http://www.gome.com.cn/ec/homeus/jump/category/cat10645563.html");
		    // 瑜伽服
		    controller.addSeed("http://www.gome.com.cn/ec/homeus/jump/category/cat10005505.html");
		    // 瑜伽配件
		    controller.addSeed("http://www.gome.com.cn/ec/homeus/jump/category/cat10645564.html");
		    // 健身服
		    controller.addSeed("http://www.gome.com.cn/ec/homeus/jump/category/cat10005503.html");
		    // 健身器材
		    controller.addSeed("http://www.gome.com.cn/ec/homeus/jump/category/cat10645544.html");
		    // 运动护具
		    controller.addSeed("http://www.gome.com.cn/ec/homeus/jump/category/cat10645545.html");
		    // 搏击类
		    controller.addSeed("http://www.gome.com.cn/ec/homeus/jump/category/cat10645546.html");
		    // 其它健身器材
		    controller.addSeed("http://www.gome.com.cn/ec/homeus/jump/category/cat10645547.html");
		    // 安全避孕
		    controller.addSeed("http://www.gome.com.cn/ec/homeus/jump/category/cat10005466.html");
		 	// 情爱玩具
		    controller.addSeed("http://www.gome.com.cn/ec/homeus/jump/category/cat10005469.html");
		    // 情趣内衣
		    controller.addSeed("http://www.gome.com.cn/ec/homeus/jump/category/cat10005470.html");
		    // 人体润滑
		    controller.addSeed("http://www.gome.com.cn/ec/homeus/jump/category/cat10005468.html");
		    // 验孕测孕
		    controller.addSeed("http://www.gome.com.cn/ec/homeus/jump/category/cat10005467.html");
		    
		    //===============================================
		    //			Newegg
		    //===============================================
		    // 美妆、个人护理
		    controller.addSeed("http://www.newegg.com.cn/Health.htm");
		    // 母婴用品、玩具
		    controller.addSeed("http://www.newegg.com.cn/Baby.htm");
		    // 食品、健康、保健
		    controller.addSeed("http://www.newegg.com.cn/Food.htm");
		    
		    //===============================================
		    //			No1Shop
		    //===============================================
		    controller.addSeed("http://channel.yihaodian.com/meihu/1/");
		    controller.addSeed("http://channel.yihaodian.com/muying");
		    controller.addSeed("http://www.yihaodian.com/channel/8704_1/");*/
		    
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
