package com.ihome.matrix.parser.html;

import junit.framework.TestCase;

import org.junit.Test;

import edu.uci.ics.crawler4j.util.URLUtil;

public class AmazonHtmlParserTest extends TestCase {

	public static final String[] URLS = new String[] {
		"http://www.amazon.cn/Apple-%E8%8B%B9%E6%9E%9C-iPhone-4S-3G%E6%99%BA%E8%83%BD%E6%89%8B%E6%9C%BA/dp/B0063CCZZW/ref=sr_1_1?s=wireless&ie=UTF8&qid=1351705004&sr=1-1",
		"http://www.amazon.cn/gp/product/B006UAD4OS/ref=s9_hps_bw_g194_ir04?pf_rd_m=A1AJ19PSB66TGU&pf_rd_s=center-5&pf_rd_r=0WE5W5G763ZTQB6TCMZT&pf_rd_t=101&pf_rd_p=61703532&pf_rd_i=746776051",
		"http://www.amazon.cn/Panasonic%E6%9D%BE%E4%B8%8B%E7%94%B5%E5%AD%90%E8%A1%80%E5%8E%8B%E8%AE%A1EW3106/dp/B004FLKG04/ref=sr_1_1?s=hpc&ie=UTF8&qid=1351780172&sr=1-1",
		"http://www.amazon.cn/omron%E6%AC%A7%E5%A7%86%E9%BE%99%E7%94%B5%E5%AD%90%E8%A1%80%E5%8E%8B%E8%AE%A1HEM-7200/dp/B003GXG1B0/ref=sr_1_2?s=hpc&ie=UTF8&qid=1351780172&sr=1-2",
		"http://www.amazon.cn/omron%E6%AC%A7%E5%A7%86%E9%BE%99%E7%94%B5%E5%AD%90%E8%A1%80%E5%8E%8B%E8%AE%A1HEM-7201/dp/B003GXG1AQ/ref=sr_1_3?s=hpc&ie=UTF8&qid=1351780172&sr=1-3",
		"http://www.amazon.cn/%E9%B1%BC%E8%B7%83%E4%BF%9D%E5%81%A5%E7%9B%92%E8%A1%80%E5%8E%8B%E8%AE%A1/dp/B004DMXJ7M/ref=sr_1_4?s=hpc&ie=UTF8&qid=1351780172&sr=1-4",
		"http://www.amazon.cn/%E4%B9%9D%E5%AE%89%E7%94%B5%E5%AD%90%E8%A1%80%E5%8E%8B%E8%AE%A1-%E5%85%A8%E8%87%AA%E5%8A%A8%E4%B8%8A%E8%87%82%E5%BC%8F-BM-091/dp/B003M2WWSG/ref=sr_1_8?s=hpc&ie=UTF8&qid=1351780172&sr=1-8",
		"http://www.amazon.cn/omron%E6%AC%A7%E5%A7%86%E9%BE%99%E7%94%B5%E5%AD%90%E8%A1%80%E5%8E%8B%E8%AE%A1HEM-7301IT/dp/B002QMKKUU/ref=sr_1_22?s=hpc&ie=UTF8&qid=1351780172&sr=1-22"
	};
	
	private AmazonHtmlParser parser = new AmazonHtmlParser();

	@Test
	public void test() {
		for(String url : URLS) {
			parser.parse(url, URLUtil.fetchHtml(url, "utf-8"), "utf-8");
		}
	}
}
