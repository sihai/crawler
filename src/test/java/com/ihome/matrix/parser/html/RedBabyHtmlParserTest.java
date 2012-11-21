package com.ihome.matrix.parser.html;

import junit.framework.TestCase;

import org.junit.Test;

import edu.uci.ics.crawler4j.util.URLUtil;

public class RedBabyHtmlParserTest extends TestCase {

	public static final String URL = "http://www.redbaby.com.cn/yingyangfs/11101041088399.html";
	public static final String URL2 = "http://www.redbaby.com.cn/yongpin/10805101238040.html";
	
	private RedBabyHtmlParser parser = new RedBabyHtmlParser();

	@Test
	public void test() {
		parser.parse(URL, URLUtil.fetchHtml(URL, "utf-8"), "utf-8");
		parser.parse(URL2, URLUtil.fetchHtml(URL, "utf-8"), "utf-8");
	}
}
