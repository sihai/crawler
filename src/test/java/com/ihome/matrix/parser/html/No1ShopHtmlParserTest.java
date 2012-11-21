package com.ihome.matrix.parser.html;

import junit.framework.TestCase;

import org.junit.Test;

import edu.uci.ics.crawler4j.util.URLUtil;

public class No1ShopHtmlParserTest extends TestCase {

	public static final String[] URLS = new String[] {
		"http://www.yihaodian.com/product/3833859_1",
		"http://www.yihaodian.com/product/4250224_1"
	};
	
	private static No1ShopHtmlParser parser = new No1ShopHtmlParser();

	@Test
	public void test() {
		for(String url : URLS) {
			parser.parse(url, URLUtil.fetchHtml(url, "utf-8"), "utf-8");
		}
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		for(String url : URLS) {
			parser.parse(url, URLUtil.fetchHtml(url, "utf-8"), "utf-8");
		}
	}
}
