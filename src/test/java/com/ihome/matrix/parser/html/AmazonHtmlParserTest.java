package com.ihome.matrix.parser.html;

import junit.framework.TestCase;

import org.junit.Test;

import edu.uci.ics.crawler4j.util.URLUtil;

public class AmazonHtmlParserTest extends TestCase {

	public static final String URL = "http://www.amazon.cn/Apple-%E8%8B%B9%E6%9E%9C-iPhone-4S-3G%E6%99%BA%E8%83%BD%E6%89%8B%E6%9C%BA/dp/B0063CCZZW/ref=sr_1_1?s=wireless&ie=UTF8&qid=1351705004&sr=1-1";
	
	private AmazonHtmlParser parser = new AmazonHtmlParser();

	@Test
	public void test() {
		parser.parse(URL, URLUtil.fetchHtml(URL, "utf-8"));
	}
}
