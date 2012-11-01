package com.ihome.matrix.parser.html;

import junit.framework.TestCase;

import org.junit.Test;

import edu.uci.ics.crawler4j.util.URLUtil;

public class Coo8HtmlParserTest extends TestCase {

	public static final String URL = "http://www.coo8.com/product/358495.html";
	
	private Coo8HtmlParser parser = new Coo8HtmlParser();

	@Test
	public void test() {
		parser.parse(URL, URLUtil.fetchHtml(URL, "utf-8"));
	}
}
