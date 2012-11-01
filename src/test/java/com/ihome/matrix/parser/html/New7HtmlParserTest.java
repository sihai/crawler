package com.ihome.matrix.parser.html;

import junit.framework.TestCase;

import org.junit.Test;

import edu.uci.ics.crawler4j.util.URLUtil;

public class New7HtmlParserTest extends TestCase {

	public static final String URL = "http://www.new7.com/product/114752.html#F2";
	
	private New7HtmlParser parser = new New7HtmlParser();

	@Test
	public void test() {
		parser.parse(URL, URLUtil.fetchHtml(URL, "utf-8"));
	}
}
