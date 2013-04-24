/**
 * 
 */
package com.ihome.matrix.parser.url;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.matrix.bridge.MatrixBridge;
import com.ihome.matrix.domain.CategoryDO;
import com.ihome.matrix.domain.ItemDO;
import com.ihome.matrix.domain.ProductDO;
import com.ihome.matrix.domain.PropertyDO;
import com.ihome.matrix.domain.ShopDO;
import com.ihome.matrix.enums.CategoryStatusEnum;
import com.ihome.matrix.enums.FreightFeePayerEnum;
import com.ihome.matrix.enums.ItemStatusEnum;
import com.ihome.matrix.enums.PlatformEnum;
import com.ihome.matrix.enums.ShopStatusEnum;
import com.ihome.matrix.enums.StuffStatusEnum;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.ItemCat;
import com.taobao.api.domain.ItemProp;
import com.taobao.api.domain.Location;
import com.taobao.api.domain.Product;
import com.taobao.api.domain.Shop;
import com.taobao.api.request.ItemGetRequest;
import com.taobao.api.request.ItemcatsGetRequest;
import com.taobao.api.request.ItempropsGetRequest;
import com.taobao.api.request.ProductGetRequest;
import com.taobao.api.request.ProductsGetRequest;
import com.taobao.api.request.ProductsSearchRequest;
import com.taobao.api.request.ShopGetRequest;
import com.taobao.api.response.ItemGetResponse;
import com.taobao.api.response.ItemcatsGetResponse;
import com.taobao.api.response.ItempropsGetResponse;
import com.taobao.api.response.ProductGetResponse;
import com.taobao.api.response.ProductsGetResponse;
import com.taobao.api.response.ProductsSearchResponse;
import com.taobao.api.response.ShopGetResponse;

import edu.uci.ics.crawler4j.util.URLUtil;

/**
 * 
 * @author sihai
 *
 */
public class TaobaoURLParser extends AbstractURLParser {
	
	private static final Log logger = LogFactory.getLog(TaobaoURLParser.class);
	
	public static final String PARMATER_ITEM_ID = "id";
	public static final String PARAMETER_MALL_ST_ITEM_ID = "mallstItemId";
	
	private static final String GATEWAY = "http://gw.api.taobao.com/router/rest";
	private static final String APP_KEY = "21341192";
    private static final String SECRET = "63808a2f09a9c500c49db480e9d25b21";

    private static final TaobaoClient client = new DefaultTaobaoClient(GATEWAY, APP_KEY, SECRET, "json", 5000, 15000);
    
    private PlatformEnum platform;
    
	@Override
	protected String getItemId(String url) {
		String itemId = URLUtil.getParameter(url, PARMATER_ITEM_ID);
		if(null == itemId) {
			itemId = URLUtil.getParameter(url, PARAMETER_MALL_ST_ITEM_ID);
		}
		return itemId;
	}

	protected void process(String itemId) {
		logger.info(String.format("Process one item in Taobao, itemId:%s", itemId));
		ItemDO item = getItem(Long.valueOf(itemId.trim()));
		// 为了product ass 啊
		ProductDO product = item.getProduct();
		if(null != product) {
			item.setTmpProduct(MatrixBridge.getTmpProductId(product, platform.getValue()));
			item.setProduct(null);
		}
		item.setIsRecommended(false);
		ShopDO shop = item.getShop();
		shop.setRank(Long.MAX_VALUE);
		shop.setIsRecommend(false);
		if(null != item) {
			MatrixBridge.sync(item);
		}
	}
	
	private ItemDO getItem(Long itemId) {
		Item item = getTaobaoItem(itemId);
		if(null != item) {
			Shop shop = getTaobaoShop(item.getNick());
			if(null != shop) {
				return toIgoItem(item, shop);
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param catId
	 * @return
	 */
	private CategoryDO getCategory(Long catId) {
		try {
			ItemCat tcat = getTaobaoCategory(catId);
			if(null != tcat) {
				CategoryDO category = new CategoryDO();
				category.setIsDeleted(false);
				category.setId(tcat.getCid());
				category.setName(tcat.getName());
				//category.setDescription(category.getName());
				category.setStatus(CategoryStatusEnum.NORMAL.getValue());
				category.setRank(0);
				CategoryDO cat = category;
				Long parentId = tcat.getParentCid();
				while(null != parentId && !parentId.equals(-1L) && !parentId.equals(0L)) {
					tcat = getTaobaoCategory(parentId);
					CategoryDO parent = new CategoryDO();
					parent.setIsDeleted(false);
					parent.setName(tcat.getName());
					//parent.setDescription(parent.getName());
					parent.setStatus(CategoryStatusEnum.NORMAL.getValue());
					parent.setRank(0);
					cat.setParent(parent);
					cat = parent;
					parentId = tcat.getParentCid();
				}
				return category;
			}
		} catch (ApiException e) {
			logger.error("Get category from taobao failed", e);
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param item
	 * @return
	 */
	private List<ProductDO> getProducts(Item item) {
		try {
			List<Product> products = getTaobaoProducts(item);
			if(null != products) {
				List<ProductDO> ps = new ArrayList<ProductDO>(products.size()); 
				for(Product product : products) {
					ProductDO p = new ProductDO();
					p.setId(product.getProductId());
					p.setName(product.getName());
					p.setLogoURL(product.getPicUrl());
					CategoryDO category = new CategoryDO();
					category.setId(product.getCid());
					category.setName(product.getCatName());
					p.setCategory(category);
					p.setDescription(product.getDesc());
					List<PropertyDO> pList = new ArrayList<PropertyDO>();
					fillProperty(pList, product.getBindsStr());
					fillProperty(pList, product.getPropsStr());
					fillProperty(pList, product.getSalePropsStr());
					ps.add(p);
				}
				return ps;
			}
		} catch (ApiException e) {
			logger.error("Get category from taobao failed", e);
		}
		
		return null;
	}
	
	private void fillProperty(List<PropertyDO> pList, String value) {
		String[] tmp = null;
		String[] kv = null;
		if(StringUtils.isNotBlank(value)) {
			tmp = value.split("#scln#");
			for(String v : tmp) {
				kv = v.split("#cln#");
				if(2 == kv.length) {
					PropertyDO property = new PropertyDO();
					property.setKey(kv[0]);
					property.setValue(kv[1]);
					pList.add(property);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param item
	 * @return
	 */
	private ProductDO getProductV2(Item item) {
		try {
			Product product = getTaobaoProduct(item);
			ProductDO p = new ProductDO();
			p.setId(product.getProductId());
			p.setName(product.getName());
			p.setLogoURL(product.getPicUrl());
			CategoryDO category = new CategoryDO();
			category.setId(product.getCid());
			category.setName(product.getCatName());
			p.setCategory(category);
			p.setDescription(product.getDesc());
			List<PropertyDO> pList = new ArrayList<PropertyDO>();
			fillProperty(pList, product.getBindsStr());
			fillProperty(pList, product.getPropsStr());
			fillProperty(pList, product.getSalePropsStr());
			return p;
		} catch (ApiException e) {
			logger.error("Get product form taobao failed", e);
		}
		return null;
	}
	
	private ItemDO toIgoItem(Item item, Shop shop) {
		ItemDO igoItem = new ItemDO();
		ShopDO igoShop = new ShopDO();
		igoShop.setDescription(shop.getDesc());
		igoShop.setShopId(shop.getSid().toString());
		igoShop.setDetailURL("http://shop" + shop.getSid() + ".taobao.com");
		igoShop.setIsDeleted(false);
		igoShop.setGmtCreate(new Date());
		igoShop.setGmtModified(igoShop.getGmtCreate());
		igoShop.setName(shop.getTitle());
		igoShop.setLogoURL("http://logo.taobao.com/" + shop.getPicPath());
		igoShop.setPlatform(platform.getValue());
		igoShop.setSellerName(shop.getNick());
		igoShop.setStatus(ShopStatusEnum.SHOP_STATUS_NORMAL.getValue());
		
		// FIXME
		if(null != item.getCid() && !item.getCid().equals(-1L)) {
			igoItem.setCategory(getCategory(item.getCid()));
		}
		
		// 商家自定义类目
		String cids = item.getSellerCids();
		if(StringUtils.isNotBlank(cids)) {
			// 
			CategoryDO cat = igoItem.getCategory();
			if(null == cat) {
				cat = new CategoryDO();
			}
			String[] tmp = cids.split(",");
			List<String> idList = new ArrayList<String>(tmp.length);
			for(String v : tmp) {
				if(StringUtils.isNotBlank(v)) {
					idList.add(StringUtils.trim(v));
				}
			}
			cat.setDescription(StringUtils.join(idList.iterator(), ","));
			igoItem.setCategory(cat);
		}
		// TODO Product
		igoItem.setProduct(getProductV2(item));
		
		igoItem.setDetailURL(item.getDetailUrl());
		igoItem.setPostFee(Double.valueOf(item.getPostFee()));
		igoItem.setEmsFee(Double.valueOf(item.getEmsFee()));
		igoItem.setExpressFee(Double.valueOf(item.getExpressFee()));
		FreightFeePayerEnum f = FreightFeePayerEnum.valueOf4Taobao(item.getFreightPayer());
		if(null != f) {
			igoItem.setFreightFeePayer(f.getValue());
		}
		igoItem.setHasDiscount(item.getHasDiscount());
		igoItem.setHasInvoice(item.getHasInvoice());
		igoItem.setHasWarranty(item.getHasWarranty());
		igoItem.setIsSellPromise(item.getSellPromise());
		igoItem.setItemId(item.getNumIid().toString());
		Location l = item.getLocation();
		if(null != l) {
			igoItem.setLocation(String.format("%s,%s,%s,%s,%s,%s", l.getCountry(), l.getState(), l.getCity(), l.getDistrict(), l.getAddress(), l.getZip()));
		}
		igoItem.setLogoURL(item.getPicUrl());
		igoItem.setName(item.getTitle());
		igoItem.setNumber(item.getNum());
		igoItem.setPlatform(platform.getValue());
		igoItem.setPrice(Double.valueOf(item.getPrice()));
		igoItem.setShop(igoShop);
		igoItem.setStatus(ItemStatusEnum.ITEM_STATUS_ON_SALE.getValue());
		StuffStatusEnum status = StuffStatusEnum.valueOf4Taobao(item.getStuffStatus());
		if(null == status) {
			status = StuffStatusEnum.STUFF_NEW;
		}
		igoItem.setIsDeleted(false);
		igoItem.setGmtCreate(new Date());
		igoItem.setGmtModified(igoItem.getGmtCreate());
		
		return igoItem;
	}
	
	private Item getTaobaoItem(Long itemId) {
		ItemGetRequest request = new ItemGetRequest();
		request.setFields("detail_url,num_iid,title,nick,type,"
				+ "desc,skus,props_name,created,"
				+ "is_lightning_consignment,is_fenxiao,auction_point,after_sale_id,"
				+ "is_xinpin,global_stock_type,cid, seller_cids,props,pic_url,num,stuff_status,"
				+ "location,price,post_fee,express_fee,ems_fee,has_discount,freight_payer,"
				+ "has_invoice,has_warranty,postage_id,product_id,item_imgs,prop_imgs,is_virtual,"
				+ "videos,is_3D,one_station,second_kill,auto_fill,violation,wap_detail_url,cod_postage_id,sell_promise,input_pids,inputStr");
		request.setNumIid(Long.valueOf(itemId));
		//request.setTrackIid("123_track_456");
		try {
			ItemGetResponse response = client.execute(request);
			Item item = response.getItem();
			logger.info(String.format("Get one item from Taobao", item));
			return item;
		} catch (ApiException e) {
			logger.error(String.format("Get item info from Taobao failed, itemId:%s", itemId), e);
		}
		return null;
	}
	
	private Shop getTaobaoShop(String sellerNick) {
		
		ShopGetRequest req = new ShopGetRequest();
		req.setFields("sid,cid,title,nick,desc,bulletin,pic_path,created,modified");
		req.setNick(sellerNick);
		try {
			ShopGetResponse response = client.execute(req);
			return response.getShop();
		} catch (ApiException e) {
			logger.error(String.format("Get shop info from Taobao failed, sellerNick:%s", sellerNick), e);
		}
		
		return null;
	}
	
	private ItemCat getTaobaoCategory(Long catId) throws ApiException {
		ItemcatsGetRequest request = new ItemcatsGetRequest();
		request.setFields("cid,parent_cid,name,is_parent");
		request.setCids(catId.toString());
		ItemcatsGetResponse response = client.execute(request);
		List<ItemCat> catList = response.getItemCats();
		if(!catList.isEmpty()) {
			return catList.get(0);
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param item
	 * @return
	 * @throws ApiException
	 */
	private List<Product> getTaobaoProducts(Item item) throws ApiException {
		// 这个类目下的产品 与 这个商家的产品交集
		List<Product> catProductList = new ArrayList<Product>();
		List<Long> catProductIdList = new ArrayList<Long>();
		ProductsSearchRequest request = new ProductsSearchRequest();
		request.setCid(item.getCid());
		request.setFields("product_id,outer_id,tsc,name,pic_url,cid,cat_name,binds_str,desc,propsStr,salePropsStr");
		request.setPageSize(100L);
		Long currentPage = 1L;
		do {
			//request.setProps(item.getProps());
			request.setPageNo(currentPage++);
			ProductsSearchResponse response = client.execute(request);
			if(null == response.getProducts() || response.getProducts().isEmpty()) {
				break;
			}
			catProductList.addAll(response.getProducts());
			for(Product p : response.getProducts()) {
				catProductIdList.add(p.getProductId());
			}
		} while(true);
		
		List<Product> sellerProductList = new ArrayList<Product>();
		ProductsGetRequest r = new ProductsGetRequest();
		r.setNick(item.getNick());
		r.setFields("product_id,outer_id,tsc,name,pic_url,cid,cat_name,binds_str,desc,propsStr,salePropsStr");
		r.setPageSize(100L);
		currentPage = 1L;
		do {
			//request.setProps(item.getProps());
			request.setPageNo(currentPage++);
			ProductsGetResponse rp = client.execute(r);
			if(null == rp.getProducts() || rp.getProducts().isEmpty()) {
				break;
			}
			sellerProductList.addAll(rp.getProducts());
		} while(true);
		
		List<Product> pList = new ArrayList<Product>();
		for(Product p : sellerProductList) {
			if(catProductIdList.contains(p.getProductId())) {
				pList.add(p);
			}
		}
		return pList;
	}
	
	/**
	 * 
	 * @param item
	 * @return
	 * @throws ApiException
	 */
	private Product getTaobaoProduct(Item item) throws ApiException {
		List<String> idList = getTaobaoKeyProperties(item.getCid());
		ProductGetRequest request = new ProductGetRequest();
		request.setFields("product_id,outer_id,tsc,name,pic_url,cid,cat_name,binds_str,desc,propsStr,salePropsStr");
		request.setCid(item.getCid());
		String pvs = item.getProps();
		if(StringUtils.isNotBlank(pvs)) {
			String[] tmp = pvs.split(";");
			StringBuilder sb = new StringBuilder();
			for(String kv : tmp) {
				String[] tmp2 = kv.split(":");
				if(idList.contains(tmp2[0])) {
					sb.append(kv);
					sb.append(";");
				}
			}
			
			if(sb.length() > 0 && sb.charAt(sb.length() - 1) == ';') {
				sb.deleteCharAt(sb.length() - 1);
			}
			
			request.setProps(sb.toString());
			ProductGetResponse response = client.execute(request);
			if(null != response.getProduct()) {
				return response.getProduct();
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param cid
	 * @param idList
	 * @return
	 */
	private List<String> getSample(Long cid, List<String> idList) {
		return null;
	}
	
	private List<String> getTaobaoKeyProperties(Long cid) throws ApiException {
		List<String> idList = new ArrayList<String>();
		ItempropsGetRequest request = new ItempropsGetRequest();
		request.setCid(cid);
		request.setFields("pid");
		request.setIsKeyProp(true);
		ItempropsGetResponse response = client.execute(request);
		List<ItemProp> pList = response.getItemProps();
		for(ItemProp ip : pList) {
			idList.add(String.valueOf(ip.getPid()));
		}
		return idList;
	}
	
	public void setPlatform(PlatformEnum platform) {
		this.platform = platform;
	}
	
	public static void main(String[] args) {
		TaobaoURLParser parser = new TaobaoURLParser();
		parser.setPlatform(PlatformEnum.PLATFORM_TMALL);
		parser.parse("http://item.taobao.com/item.htm?id=16251065906&spm=2014.21341192.0.0");
	}
}
