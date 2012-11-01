package com.ihome.matrix.bridge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ihome.matrix.dao.exception.ValidateException;
import com.ihome.matrix.domain.CategoryDO;
import com.ihome.matrix.domain.ItemDO;
import com.ihome.matrix.domain.ShopDO;
import com.ihome.matrix.enums.PlatformEnum;
import com.ihome.matrix.enums.ShopStatusEnum;
import com.ihome.matrix.index.ItemSolrIndexer;
import com.ihome.matrix.index.ShopSolrIndexer;
import com.ihome.matrix.manager.CategoryManager;
import com.ihome.matrix.manager.ItemManager;
import com.ihome.matrix.manager.ShopManager;

/**
 * 
 * @author sihai
 *
 */
public class MatrixBridge {
	
	private static final Log logger = LogFactory.getLog(MatrixBridge.class);
	
	private static final String ITEM_MANAGER = "itemManager";			// bean name for itemManager
	private static final String SHOP_MANAGER = "shopManager";			// bean name for shopManager
	private static final String CATEGORY_MANAGER = "categoryManager";	// bean name for categoryManager
	
	private static final String ITEM_SOLR_INDEXER = "itemSolrIndexer";
	private static final String SHOP_SOLR_INDEXER = "shopSolrIndexer";
	
	private static ApplicationContext context;		//
	
	private static ShopManager shopManager;			//
	private static ItemManager itemManager;			//
	private static CategoryManager categoryManager;	//
	
	private static ItemSolrIndexer itemSolrIndexer;	//
	private static ShopSolrIndexer shopSolrIndexer;	//
	

	private static Map<PlatformEnum, ShopDO> fixedShopMap;
	
	// lock
    private static final int MAX_LOCK = 64;
    private static Map<Integer, Object> itemLockMap;
    private static Map<Integer, Object> shopLockMap;
    private static Map<Integer, Object> categoryLockMap;
    
    static {
    	itemLockMap = new HashMap<Integer, Object>();
    	shopLockMap = new HashMap<Integer, Object>();
    	categoryLockMap = new HashMap<Integer, Object>();
    	
    	for(int i = 0; i < MAX_LOCK; i++) {
    		itemLockMap.put(Integer.valueOf(i), new Object());
    		shopLockMap.put(Integer.valueOf(i), new Object());
    		categoryLockMap.put(Integer.valueOf(i), new Object());
    	}
    		
    }
	
	static {
		context = new ClassPathXmlApplicationContext("classpath:/spring/spring-matrix.xml");
		
		shopManager = (ShopManager)context.getBean(SHOP_MANAGER);
		itemManager = (ItemManager)context.getBean(ITEM_MANAGER);
		categoryManager = (CategoryManager)context.getBean(CATEGORY_MANAGER);
		
		itemSolrIndexer = (ItemSolrIndexer)context.getBean(ITEM_SOLR_INDEXER);
		shopSolrIndexer = (ShopSolrIndexer)context.getBean(SHOP_SOLR_INDEXER);
		
		fixedShopMap = new HashMap<PlatformEnum, ShopDO>(PlatformEnum.values().length);
		for(PlatformEnum platform : PlatformEnum.values()) {
			ShopDO shop = new ShopDO();
			shop.setId(Long.valueOf(platform.getValue()));
			shop.setPlatform(platform.getValue());
			shop.setShopId(platform.getName());
			shop.setName(platform.getName());
			shop.setSellerName(platform.getName());
			shop.setDetailURL(platform.getUrl());
			shop.setStatus(ShopStatusEnum.SHOP_STATUS_NORMAL.getValue());
			shop.setIsDeleted(false);
			fixedShopMap.put(platform, shop);
		}
	}

	/**
	 * 同步数据库, 索引
	 * @param item
	 * @param shop
	 */
	public static void sync(ItemDO item) {
		syncDB(item);
		syncIndex(item);
	}
	
	/**
	 * 
	 * @param item
	 */
	private static void syncDB(ItemDO item) {
		// Shop
		//ShopDO igoShop = syncShop(item.getShop());
		//if(null != igoShop) {
			syncShop(item.getShop());
			syncCategory(item.getCategory());
			syncItem(item);
		//}
	}
	
	/**
	 * 构建索引
	 * @param item
	 */
	private static void syncIndex(ItemDO item) {
		item.setCategory(item.getCategory());
		item.setShop(item.getShop());
		itemSolrIndexer.upate(item);
		shopSolrIndexer.upate(item.getShop());
	}
	
	public void close() {
	}
	
	/**
	 * 
	 * @param shop
	 * @return
	 */
	private static ShopDO syncShop(ShopDO shop) {
		String shopId = shop.getShopId();
		int index = shopId.hashCode() % MAX_LOCK;
		index = index < 0 ? -index : index;
		synchronized(shopLockMap.get(index)) {
			ShopDO igoShop = shopManager.getByShopIdAndPlatform(shopId, shop.getPlatform());
			
			try {
				if(null != igoShop) {
					shop.setId(igoShop.getId());
					shopManager.update(shop);
				} else {
					shopManager.add(shop);
				}
				return shop;
			} catch (ValidateException e) {
				logger.error("Not possiable, sync shop data from taobao failed, exception", e);
				return null;
			}
		}
	}
	
	/**
	 * 
	 * @param category
	 */
	private static void syncOneCategory(CategoryDO category) {
		String name = category.getName();
		int index = name.hashCode() % MAX_LOCK;
		index = index < 0 ? -index : index;
		synchronized(categoryLockMap.get(index)) {
			CategoryDO cat = categoryManager.getByPlatformAndNameAndParent(category.getPlatform(), name, null == category.getParent() ? null : category.getParent().getId());
			try {
				if(null != cat) {
					category.setId(cat.getId());
					categoryManager.update(category);
				} else {
					categoryManager.add(category);
				}
			} catch (ValidateException e) {
				logger.error("Not possiable, sync shop data from taobao failed, exception", e);
			}
		}
	}
	/**
	 * 
	 * @param category
	 */
	private static void syncCategory(CategoryDO category) {
		List<CategoryDO> categoryPath = new ArrayList<CategoryDO>(3);
		categoryPath.add(category);
		CategoryDO parent = category.getParent();
		while(null != parent) {
			categoryPath.add(parent);
			parent = parent.getParent();
		}
		
		for(int i = categoryPath.size() - 1; i >= 0; i--) {
			syncOneCategory(categoryPath.get(i));
		}
	}
	
	/**
	 * 
	 * @param item
	 * @param shop
	 */
	private static void syncItem(ItemDO item) {
		String itemId = item.getItemId();
		int index = itemId.hashCode() % MAX_LOCK;
		index = index < 0 ? -index : index;
		synchronized(itemLockMap.get(index)) {
			ItemDO igoItem = itemManager.getByItemIdAndPlatform(itemId.toString(), item.getPlatform());
			
			try {
				if(null != igoItem) {
					item.setId(igoItem.getId());
					itemManager.update(item);
				} else {
					itemManager.add(item);
				}
			} catch (ValidateException e) {
				logger.error("Not possiable, sync item data from taobao failed, exception", e);
			}
		}
	}
	
	public static ShopManager getShopManager() {
		return shopManager;
	}
	
	public static ItemManager getItemManager() {
		return itemManager;
	}
	
	public static CategoryManager getCategoryManager() {
		return categoryManager;
	}
	
	public static ItemSolrIndexer getItemSolrIndexer() {
		return itemSolrIndexer;
	}

	public static ShopSolrIndexer getShopSolrIndexer() {
		return shopSolrIndexer;
	}
	
	public static ShopDO getFixedShop(PlatformEnum platform) {
		return fixedShopMap.get(platform);
	}
}
