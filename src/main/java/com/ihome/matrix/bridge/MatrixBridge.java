/**
 * 
 */
package com.ihome.matrix.bridge;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ihome.matrix.cluster.Cluster;
import com.ihome.matrix.dao.exception.ValidateException;
import com.ihome.matrix.domain.BrandDO;
import com.ihome.matrix.domain.CategoryDO;
import com.ihome.matrix.domain.CommentDO;
import com.ihome.matrix.domain.ItemDO;
import com.ihome.matrix.domain.ProductDO;
import com.ihome.matrix.domain.ShopCategoryDO;
import com.ihome.matrix.domain.ShopDO;
import com.ihome.matrix.domain.ShopProductDO;
import com.ihome.matrix.domain.TmpProductDO;
import com.ihome.matrix.enums.CommentTypeEnum;
import com.ihome.matrix.enums.PlatformEnum;
import com.ihome.matrix.enums.ShopStatusEnum;
import com.ihome.matrix.index.ItemSolrIndexer;
import com.ihome.matrix.index.ShopSolrIndexer;
import com.ihome.matrix.manager.BrandManager;
import com.ihome.matrix.manager.CategoryManager;
import com.ihome.matrix.manager.CommentManager;
import com.ihome.matrix.manager.ItemManager;
import com.ihome.matrix.manager.ShopManager;
import com.ihome.matrix.manager.TmpProductManager;

/**
 * 
 * @author sihai
 * 
 */
public class MatrixBridge {
	
	private static final Log logger = LogFactory.getLog(MatrixBridge.class);
	
	private static final String BRAND_MANAGER = "brandManager";			// bean name for brandManager
	private static final String ITEM_MANAGER = "itemManager";			// bean name for itemManager
	private static final String SHOP_MANAGER = "shopManager";			// bean name for shopManager
	private static final String CATEGORY_MANAGER = "categoryManager";	// bean name for categoryManagerprivate static final String CATEGORY_MANAGER = "categoryManager";	// bean name for categoryManager
	private static final String COMMENT_MANAGER = "commentManager";		// bean name for commentManager
	private static final String TMP_PRODUCT_MANAGER = "tmpProductManager";
	
	private static final String DEFAULT_CLUSTER = "defaultCluster";		// bean name for defaultCluster
	
	private static final String ITEM_SOLR_INDEXER = "itemSolrIndexer";
	private static final String SHOP_SOLR_INDEXER = "shopSolrIndexer";
	
	private static ApplicationContext context;		//
	
	private static BrandManager brandManager;		//
	private static ShopManager shopManager;			//
	private static ItemManager itemManager;			//
	private static CategoryManager categoryManager;	//
	private static CommentManager commentManager;	// 
	
	private static TmpProductManager tmpProductManager;
	
	private static Cluster cluster;	// 
	
	private static ItemSolrIndexer itemSolrIndexer;	//
	private static ShopSolrIndexer shopSolrIndexer;	//
	

	private static Map<PlatformEnum, ShopDO> fixedShopMap;
	
	// lock
    private static final int MAX_LOCK = 64;
    private static Map<Integer, Object> itemLockMap;
    private static Map<Integer, Object> shopLockMap;
    private static Map<Integer, Object> brandLockMap;
    private static Map<Integer, Object> categoryLockMap;
    private static Map<Integer, Object> tmpProductLockMap;
    
    static {
    	brandLockMap = new HashMap<Integer, Object>();
    	itemLockMap = new HashMap<Integer, Object>();
    	shopLockMap = new HashMap<Integer, Object>();
    	categoryLockMap = new HashMap<Integer, Object>();
    	tmpProductLockMap = new HashMap<Integer, Object>();
    	
    	for(int i = 0; i < MAX_LOCK; i++) {
    		brandLockMap.put(Integer.valueOf(i), new Object());
    		itemLockMap.put(Integer.valueOf(i), new Object());
    		shopLockMap.put(Integer.valueOf(i), new Object());
    		categoryLockMap.put(Integer.valueOf(i), new Object());
    		tmpProductLockMap.put(Integer.valueOf(i), new Object());
    	}
    		
    }
	
	static {
		context = new ClassPathXmlApplicationContext("classpath:/spring/spring-matrix.xml");
		
		brandManager = (BrandManager)context.getBean(BRAND_MANAGER);
		shopManager = (ShopManager)context.getBean(SHOP_MANAGER);
		itemManager = (ItemManager)context.getBean(ITEM_MANAGER);
		categoryManager = (CategoryManager)context.getBean(CATEGORY_MANAGER);
		commentManager = (CommentManager)context.getBean(COMMENT_MANAGER);
		tmpProductManager = (TmpProductManager)context.getBean(TMP_PRODUCT_MANAGER);
		
		cluster = (Cluster)context.getBean(DEFAULT_CLUSTER);
		
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
		// 
		CategoryDO category = cluster.clusterCategory(item);
		item.setCategory(category);
		ProductDO product = cluster.clusterProduct(item);
		item.setProduct(product);
		
		syncDB(item);
		//syncIndex(item);
	}
	
	/**
	 * 
	 * @param item
	 */
	private static void syncDB(ItemDO item) {
		// Shop
		//ShopDO igoShop = syncShop(item.getShop());
		//if(null != igoShop) {
			if(null != item.getBrand()) {
				//syncBrand(item.getBrand());
			}
			syncShop(item.getShop());
			//syncCategory(item.getCategory());
			syncItem(item);

			/*if(null != item.getCommentList() && !item.getCommentList().isEmpty()) {
				syncComment(item, item.getCommentList());
			}*/
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
		// 非平台店铺, 才更索引
		//if(null == PlatformEnum.toEnum(item.getShop().getPlatform())) {
			shopSolrIndexer.upate(item.getShop());
		//}
	}
	
	public void close() {
	}
	
	/**
	 * 
	 * @param brand
	 */
	private static void syncBrand(BrandDO brand) {
		String name = brand.getName();
		int index = name.hashCode() % MAX_LOCK;
		index = index < 0 ? -index : index;
		synchronized(brandLockMap.get(index)) {
			BrandDO dbBrand = brandManager.getByName(name);
			try {
				if(null != dbBrand) {
					brand.setId(dbBrand.getId());
					brandManager.update(brand);
				} else {
					brandManager.add(brand);
				}
				brand.setGmtModified(new Date());
			} catch (ValidateException e) {
				throw new RuntimeException("Not possiable, exception", e);
			}
		}
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
				shop.setGmtModified(new Date());
				
				// sync shop category
				List<ShopCategoryDO> shopCategoryList = shop.getShopCategoryList();
				if(null != shopCategoryList && !shopCategoryList.isEmpty()) {
					syncShopCategory(shop, shopCategoryList);
				}
				// sync shop product
				List<ShopProductDO> shopProductList = shop.getShopProductList();
				if(null != shopProductList && !shopProductList.isEmpty()) {
					syncShopProduct(shop, shopProductList);
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
	 * @param shop
	 * @param shopCategoryList
	 */
	private static void syncShopCategory(ShopDO shop, List<ShopCategoryDO> shopCategoryList) {
		// delete all
		shopManager.deleteShopCategoryByShopId(shop.getId());
		// add all
		try {
			for(ShopCategoryDO shopCategory : shopCategoryList) {
				shopCategory.setShop(shop);
				shopManager.addShopCategory(shopCategory);
			}
		} catch (ValidateException e) {
			throw new RuntimeException("OMG, Not possiable", e);
		}
	}
	
	/**
	 * 
	 * @param shop
	 * @param shopProductList
	 */
	private static void syncShopProduct(ShopDO shop, List<ShopProductDO> shopProductList) {
		// delete all
		shopManager.deleteShopProductByShopId(shop.getId());
		// add all
		try {
			for(ShopProductDO shopProduct : shopProductList) {
				shopProduct.setShop(shop);
				shopManager.addShopProduct(shopProduct);
			}
		} catch (ValidateException e) {
			throw new RuntimeException("OMG, Not possiable", e);
		}
	}
	
	/**
	 * 
	 * @param item
	 * @param commentList
	 */
	private static void syncComment(ItemDO item, List<CommentDO> commentList) {
		// delete all
		commentManager.deleteByTypeAndOwner(CommentTypeEnum.ITEM.getValue(), item.getId());
		// insert new
		try {
			for(CommentDO comment : commentList) {
				comment.setOwner(item);
				commentManager.add(comment);
			}
		} catch (ValidateException e) {
			throw new RuntimeException("OMG, Not possiable", e);
		}
	}
	
	/**
	 * 
	 * @param category
	 */
	private static void syncOneCategory(CategoryDO category) {
		/*String name = category.getName();
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
		}*/
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
	
	/**
	 * 
	 * @param product
	 * @param platform
	 * @return
	 */
	public static Long getTmpProductId(ProductDO product, Integer platform) {
		Long outProductId = product.getId();
		int hashcode = outProductId.hashCode() + platform.hashCode();
		int index = hashcode % MAX_LOCK;
		index = index < 0 ? -index : index;
		synchronized(tmpProductLockMap.get(index)) {
			TmpProductDO tp = tmpProductManager.getByOutProductIdIdAndPlatform(outProductId, platform);
			if(null == tp) {
				tp = new TmpProductDO();
				tp.setOutProductId(outProductId);
				tp.setOutProductName(product.getName());
				tp.setOutCategoryId(product.getCategory().getId());
				tp.setOutCategoryName(product.getCategory().getName());
				tp.setPlatform(platform);
				tp.setLogoURL(product.getLogoURL());
				tp.setDescription(product.getDescription());
				if(null != product.getPropertyList()) {
					tp.setProperty(StringUtils.join(product.getPropertyList().iterator(), ","));
				}
				tp.setIsDeleted(false);
				try {
					tmpProductManager.add(tp);
				} catch (ValidateException e) {
					throw new RuntimeException("Not possible");
				}
			}
			return tp.getId();
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
