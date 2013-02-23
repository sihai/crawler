package com.ihome.matrix.parser.html;

import junit.framework.TestCase;

import org.junit.Test;

import edu.uci.ics.crawler4j.util.URLUtil;

public class JingdongHtmlParserTest extends TestCase {

	public static final String[] URLS = new String[] {
		"http://www.360buy.com/product/717554.html",
		"http://www.360buy.com/product/620876.html",
		"http://www.ehaoyao.com/product/1850003183.html"
	};
	
	private static JingdongHtmlParser parser = new JingdongHtmlParser();

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
