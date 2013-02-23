/**
 * 
 */
package com.ihome.matrix.parser.url;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.matrix.bridge.MatrixBridge;
import com.ihome.matrix.domain.CategoryDO;
import com.ihome.matrix.domain.ItemDO;
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
import com.taobao.api.domain.Location;
import com.taobao.api.domain.Shop;
import com.taobao.api.request.ItemGetRequest;
import com.taobao.api.request.ItemcatsGetRequest;
import com.taobao.api.request.ShopGetRequest;
import com.taobao.api.response.ItemGetResponse;
import com.taobao.api.response.ItemcatsGetResponse;
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
	private static final String APP_KEY = "12553640";
    private static final String SECRET = "de463fd7cc82a51b060ffe6a11e345f9";

    private static final TaobaoClient client = new DefaultTaobaoClient(GATEWAY, APP_KEY, SECRET, "json", 5000, 15000);
    
    private PlatformEnum platform = PlatformEnum.PLATFORM_TMALL;

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
				category.setPlatform(platform.getValue());
				category.setName(tcat.getName());
				category.setDescription(category.getName());
				category.setStatus(CategoryStatusEnum.NORMAL.getValue());
				category.setRank(0);
				CategoryDO cat = category;
				Long parentId = tcat.getParentCid();
				while(null != parentId && !parentId.equals(-1L) && !parentId.equals(0L)) {
					tcat = getTaobaoCategory(parentId);
					CategoryDO parent = new CategoryDO();
					parent.setIsDeleted(false);
					parent.setPlatform(platform.getValue());
					parent.setName(tcat.getName());
					parent.setDescription(parent.getName());
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
	
	private ItemDO toIgoItem(Item item, Shop shop) {
		ItemDO igoItem = new ItemDO();
		
		ShopDO igoShop = new ShopDO();
		igoShop.setBulletin(shop.getBulletin());
		igoShop.setDescription(shop.getDesc());
		igoShop.setShopId(shop.getSid().toString());
		igoShop.setDetailURL("shop" + shop.getSid() + ".taobao.com");
		igoShop.setIsDeleted(false);
		igoShop.setGmtCreate(new Date());
		igoShop.setGmtModified(igoShop.getGmtCreate());
		igoShop.setName(shop.getTitle());
		igoShop.setLogoURL("http://logo.taobao.com/" + shop.getPicPath());
		igoShop.setPlatform(PlatformEnum.PLATFORM_TAOBAO.getValue());
		igoShop.setSellerName(shop.getNick());
		igoShop.setStatus(ShopStatusEnum.SHOP_STATUS_NORMAL.getValue());
		
		// FIXME
		if(null != item.getCid() && !item.getCid().equals(-1L)) {
			igoItem.setCategory(getCategory(item.getCid()));
		}
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
		//igoItem.setIsSecondKill(item.getSecondKill());
		igoItem.setIsSellPromise(item.getSellPromise());
		igoItem.setIsXinpin(item.getIsXinpin());
		igoItem.setItemId(item.getNumIid().toString());
		Location l = item.getLocation();
		if(null != l) {
			igoItem.setLocation(String.format("%s,%s,%s,%s,%s,%s", l.getCountry(), l.getState(), l.getCity(), l.getDistrict(), l.getAddress(), l.getZip()));
		}
		igoItem.setLogoURL(item.getPicUrl());
		igoItem.setName(item.getTitle());
		igoItem.setNumber(item.getNum());
		igoItem.setPlatform(PlatformEnum.PLATFORM_TAOBAO.getValue());
		igoItem.setPrice(Double.valueOf(item.getPrice()));
		igoItem.setShop(igoShop);
		igoItem.setStatus(ItemStatusEnum.ITEM_STATUS_ON_SALE.getValue());
		StuffStatusEnum status = StuffStatusEnum.valueOf4Taobao(item.getStuffStatus());
		if(null == status) {
			status = StuffStatusEnum.STUFF_NEW;
		}
		igoItem.setStuffStatus(status.getValue());
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
				+ "is_xinpin,global_stock_type,cid,props,pic_url,num,stuff_status,"
				+ "location,price,post_fee,express_fee,ems_fee,has_discount,freight_payer,"
				+ "has_invoice,has_warranty,postage_id,product_id,item_imgs,prop_imgs,is_virtual,"
				+ "videos,is_3D,one_station,second_kill,auto_fill,violation,wap_detail_url,cod_postage_id,sell_promise");
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
	
	public void setPlatform(PlatformEnum platform) {
		this.platform = platform;
	}
	
	public static void main(String[] args) {
		TaobaoURLParser parser = new TaobaoURLParser();
		parser.setPlatform(PlatformEnum.PLATFORM_TMALL);
		parser.parse("http://detail.tmall.com/item.htm?id=16872232278&is_b=1&cat_id=50022738&q=&rn=8a0eea4921157f8641c0589261736951");
	}
}
