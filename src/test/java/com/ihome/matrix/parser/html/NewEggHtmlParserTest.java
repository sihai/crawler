package com.ihome.matrix.parser.html;

import junit.framework.TestCase;

import org.junit.Test;

import edu.uci.ics.crawler4j.util.URLUtil;

public class NewEggHtmlParserTest extends TestCase {

	public static final String[] URLS = new String[] {
		"http://www.newegg.com.cn/Product/A41-299-2AE.htm?cm_sp=HotSell-_-A41-299-2AE-_-product",
		"http://www.newegg.com.cn/Product/A26-032-1R0-03.htm?cm_sp=ProductRank-_-A26-032-1R0-03-_-product"
	};
	
	private static NewEggHtmlParser parser = new NewEggHtmlParser();

	@Test
	public void test() {
		for(String url : URLS) {
			parser.parse(url, URLUtil.fetchContent(url, "gb2312"), "gb2312");
		}
	}
	
	public static void main(String[] args) {
		for(String url : URLS) {
			parser.parse(url, URLUtil.fetchContent(url, "gb2312"), "gb2312");
		}
	}
}
