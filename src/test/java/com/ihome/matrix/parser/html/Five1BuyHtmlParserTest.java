package com.ihome.matrix.parser.html;

import junit.framework.TestCase;

import org.junit.Test;

import edu.uci.ics.crawler4j.util.URLUtil;

public class Five1BuyHtmlParserTest extends TestCase {

	public static final String URL = "http://item.51buy.com/item-107316.html?YTAG=1.100040000";
	
	private Five1BuyHtmlParser parser = new Five1BuyHtmlParser();

	@Test
	public void test() {
		parser.parse(URL, URLUtil.fetchHtml(URL, "utf-8"));
	}
}
