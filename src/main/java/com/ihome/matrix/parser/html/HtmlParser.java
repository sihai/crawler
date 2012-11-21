/**
 * 
 */
package com.ihome.matrix.parser.html;

/**
 * 
 * @author sihai
 *
 */
public interface HtmlParser {
	
	/**
	 * 
	 * @param url
	 * @param html
	 * @param charset
	 */
	void parse(String url, String html, String charset);
}
