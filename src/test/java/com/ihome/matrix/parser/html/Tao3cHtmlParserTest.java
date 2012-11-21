package com.ihome.matrix.parser.html;

import junit.framework.TestCase;

import org.junit.Test;

import edu.uci.ics.crawler4j.util.URLUtil;

public class Tao3cHtmlParserTest extends TestCase {

	public static final String URL = "http://www.tao3c.com/product/503451.html?jkjlsjlj=kjflkdsjflkdsf&jlkdsjfslkdf=jlkjdsflksd";
	
	private Tao3cHtmlParser parser = new Tao3cHtmlParser();

	@Test
	public void test() {
		parser.parse(URL, URLUtil.fetchHtml(URL, "gb2312"), "gb2312");
	}
}
