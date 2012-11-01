package com.ihome.matrix.parser.html;

import junit.framework.TestCase;

import org.junit.Test;

import edu.uci.ics.crawler4j.util.URLUtil;

public class OukuHtmlParserTest extends TestCase {

	public static final String URL = "http://www.ouku.com/goods57133/?jkjlsjlj=kjflkdsjflkdsf&jlkdsjfslkdf=jlkjdsflksd";
	
	private OukuHtmlParser parser = new OukuHtmlParser();

	@Test
	public void test() {
		parser.parse(URL, URLUtil.fetchHtml(URL, "utf-8"));
	}
}
