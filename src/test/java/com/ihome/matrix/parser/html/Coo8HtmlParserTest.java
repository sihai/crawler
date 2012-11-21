package com.ihome.matrix.parser.html;

import junit.framework.TestCase;

import org.junit.Test;

import edu.uci.ics.crawler4j.util.URLUtil;

public class Coo8HtmlParserTest extends TestCase {

	public static final String[] URLS = new String[] {
			"http://www.coo8.com/product/358495.html",
			"http://www.coo8.com/product/482252.html",
			"http://www.coo8.com/product/482238.html",
	};
	
	private static Coo8HtmlParser parser = new Coo8HtmlParser();

	@Test
	public void test() {
		for(String url : URLS) {
			parser.parse(url, URLUtil.fetchHtml(url, "gbk"), "gbk");
		}
	}
	
	public static void main(String[] args) {
		for(String url : URLS) {
			parser.parse(url, URLUtil.fetchHtml(url, "gbk"), "gbk");
		}
	}
}
