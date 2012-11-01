package com.ihome.matrix.parser.html;

import junit.framework.TestCase;

import org.junit.Test;

import edu.uci.ics.crawler4j.util.URLUtil;

public class JingdongHtmlParserTest extends TestCase {

	public static final String URL = "http://www.360buy.com/product/717554.html";
	
	private JingdongHtmlParser parser = new JingdongHtmlParser();

	@Test
	public void test() {
		parser.parse(URL, URLUtil.fetchHtml(URL, "utf-8"));
	}
}
