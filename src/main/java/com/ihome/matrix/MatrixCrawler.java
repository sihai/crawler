/**
 * 
 */
package com.ihome.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.ihome.matrix.parser.url.URLParserHelper;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * 
 * @author sihai
 *
 */
public class MatrixCrawler extends WebCrawler {

	private static List<Pattern> acceptPatternList;
	
	static {
		acceptPatternList = new ArrayList<Pattern>();
		//
		acceptPatternList.add(Pattern.compile("http://list.tmall.com/search_product.htm\\?[\\s|\\S]+"));
		acceptPatternList.add(Pattern.compile("http://detail.tmall.com/item.htm\\?[\\s|\\S]*&id=[\\d]+[\\s|\\S]*"));
	}
	
	@Override
	public boolean shouldVisit(WebURL url) {
		for(Pattern p : acceptPatternList) {
			if(p.matcher(url.getURL()).matches()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void visit(Page page) {
		//System.out.println(String.format("URL:%s", page.getWebURL().getURL()));
	}
	
	@Override
	public void parseURL(WebURL webURL) {
		super.parseURL(webURL);
		URLParserHelper.parse(webURL.getURL());
	}
}
