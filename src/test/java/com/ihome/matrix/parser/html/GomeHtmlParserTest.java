package com.ihome.matrix.parser.html;

import junit.framework.TestCase;

import org.junit.Test;

import edu.uci.ics.crawler4j.util.URLUtil;

public class GomeHtmlParserTest extends TestCase {

	public static final String URL = "http://www.gome.com.cn/ec/homeus/jump/product/9110530556.html?jkjlkj=jlkjlkdsjf&fdsfdsfjl=xjlsdjfsldkfj";
	
	private static GomeHtmlParser parser = new GomeHtmlParser();

	@Test
	public void test() {
		parser.parse(URL, URLUtil.fetchHtml(URL, "utf-8"), "utf-8");
	}
	
	public static void main(String[] args) {
		parser.parse(URL, URLUtil.fetchHtml(URL, "utf-8"), "utf-8");
	}
}
