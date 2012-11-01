package com.ihome.matrix.parser.html;

import junit.framework.TestCase;

import org.junit.Test;

import edu.uci.ics.crawler4j.util.URLUtil;

public class SuningHtmlParserTest extends TestCase {

	public static final String URL = "http://www.suning.com/emall/prd_10052_10051_-7_1350461_.html";
	
	private SuningHtmlParser parser = new SuningHtmlParser();

	@Test
	public void test() {
		parser.parse(URL, URLUtil.fetchHtml(URL, "utf-8"));
	}
}
