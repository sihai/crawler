package com.ihome.matrix.parser.html;

import junit.framework.TestCase;

import org.junit.Test;

import edu.uci.ics.crawler4j.util.URLUtil;

public class DangdangHtmlParserTest extends TestCase {

	public static final String URL = "http://product.dangdang.com/product.aspx?product_id=1014060112&spm=123444&_xx_=123456";
	
	private DangdangHtmlParser parser = new DangdangHtmlParser();

	@Test
	public void test() {
		parser.parse(URL, URLUtil.fetchHtml(URL, "utf-8"));
	}
}
