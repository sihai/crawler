package com.ihome.matrix.parser.html;

import junit.framework.TestCase;

import org.junit.Test;

import edu.uci.ics.crawler4j.util.URLUtil;

public class No1ShopHtmlParserTest extends TestCase {

	public static final String URL = "http://www.yihaodian.com/product/3833859_1";
	
	private No1ShopHtmlParser parser = new No1ShopHtmlParser();

	@Test
	public void test() {
		parser.parse(URL, URLUtil.fetchHtml(URL, "utf-8"));
	}
}
