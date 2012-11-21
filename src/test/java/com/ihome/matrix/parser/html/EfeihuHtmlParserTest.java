package com.ihome.matrix.parser.html;

import junit.framework.TestCase;

import org.junit.Test;

import edu.uci.ics.crawler4j.util.URLUtil;

public class EfeihuHtmlParserTest extends TestCase {

	public static final String URL = "http://www.efeihu.com/Product/2020101019082.html?pcid=hp6-1-1";
	
	private EfeihuHtmlParser parser = new EfeihuHtmlParser();

	@Test
	public void test() {
		parser.parse(URL, URLUtil.fetchHtml(URL, "gb2312"), "gb2312");
	}
}
