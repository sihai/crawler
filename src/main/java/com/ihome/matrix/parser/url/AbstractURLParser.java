/**
 * 
 */
package com.ihome.matrix.parser.url;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sihai
 *
 */
public abstract class AbstractURLParser implements URLParser {

	private static final Log logger = LogFactory.getLog(AbstractURLParser.class);
	
	@Override
	public void parse(String url) {
		String itemId = getItemId(url);
		if(null != itemId) {
			process(itemId);
		} else  {
			logger.info(String.format("Ignore, url:%s", url));
		}
	}
	
	/**
	 * 
	 * @param itemId
	 */
	protected abstract void process(String itemId);
	
	/**
	 * 
	 * @param url
	 * @return
	 */
	protected abstract String getItemId(String url);
}
