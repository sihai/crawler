package com.ihome.matrix.parser.html;

import org.junit.Test;

import edu.uci.ics.crawler4j.util.URLUtil;

public class DangdangHtmlParserTest {

	public static final String URL = "http://product.dangdang.com/product.aspx?product_id=1014060112&spm=123444&_xx_=123456";
	
	private static DangdangHtmlParser parser = new DangdangHtmlParser();

	@Test
	public void test() {
		parser.parse(URL, URLUtil.fetchHtml(URL, "gb2312"), "gb2312");
	}
	
	public static void main(String[] args) {
		parser.parse(URL, URLUtil.fetchHtml(URL, "gb2312"), "gb2312");
	}
}
