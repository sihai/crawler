package com.ihome.matrix.parser.html;

import junit.framework.TestCase;

import org.junit.Test;

import edu.uci.ics.crawler4j.util.URLUtil;

public class LusenHtmlParserTest extends TestCase {

	public static final String URL = "http://www.lusen.com/Product/ProductInfo.aspx?id=2429&cruxId=38&Type=PanicBuy";
	public static final String URL2 = "http://www.lusen.com/Product/ProductInfo.aspx?id=4707";
	
	private LusenHtmlParser parser = new LusenHtmlParser();

	@Test
	public void test() {
		parser.parse(URL, URLUtil.fetchHtml(URL, "utf-8"), "utf-8");
		parser.parse(URL2, URLUtil.fetchHtml(URL2, "utf-8"), "utf-8");
	}
}
