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
	 * @param content
	 * @param charset
	 */
	void parse(String url, byte[] content, String charset);
}
