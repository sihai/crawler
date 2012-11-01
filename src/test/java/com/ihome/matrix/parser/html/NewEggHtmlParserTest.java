package com.ihome.matrix.parser.html;

import junit.framework.TestCase;

import org.junit.Test;

import edu.uci.ics.crawler4j.util.URLUtil;

public class NewEggHtmlParserTest extends TestCase {

	public static final String URL = "http://www.yihaodian.com/product/3833859_1";
	public static final String URL2 = "http://www.newegg.com.cn/Product/A41-299-2AE.htm?cm_sp=HotSell-_-A41-299-2AE-_-product";
	
	private NewEggHtmlParser parser = new NewEggHtmlParser();

	@Test
	public void test() {
		parser.parse(URL, URLUtil.fetchHtml(URL, "utf-8"));
		parser.parse(URL2, URLUtil.fetchHtml(URL, "utf-8"));
	}
}
