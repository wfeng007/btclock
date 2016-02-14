/**
 * 
 */
package summ.btc.btclock.okcoin;

import java.io.IOException;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;

import com.okcoin.rest.StringUtil;
import com.okcoin.rest.stock.IStockRestApi;
import com.okcoin.rest.stock.impl.StockRestApi;

import summ.btc.btclock.Tradable;
import summ.btc.btclock.data.TradeOrder;

/**
 * @author wfeng007
 * @date 2016-2-13 下午04:09:41
 */
public class OkcoinTrader implements Tradable {

	private String apiKey; // OKCoin申请的apiKey
	private String secretKey; // OKCoin
	private String urlPrex = "https://www.okcoin.cn"; // 注意：请求URL
												// 国际站https://www.okcoin.com ;
												// 国内站https://www.okcoin.cn
	
//	private IStockRestApi stockPost = new StockRestApi(urlPrex, apiKey, secretKey);
	private IStockRestApi stockPost ;
	
	public void init(){
		stockPost=new StockRestApi(urlPrex, apiKey, secretKey);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see summ.btc.btclock.Tradable#buy(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public TradeOrder buy(String price, String amount) {
		try {
			String reStr=stockPost.trade("btc_cny", "buy",price,amount);
            return parseRs(reStr);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} 
	}
	/*
	 * 
	 */
	private TradeOrder parseRs(String reStr){
		
		//reStr类似：{"result":true,"order_id":123456}
		//reStr错误返回：{"error_code":10000,"result":false}
		System.out.println(reStr);
		JSONObject js=JSONObject.fromObject(reStr);
		if(StringUtils.isNotBlank(js.optString("error_code"))){
			throw new RuntimeException("trade err!:" + js.optString("error_code"));
		}
		if(StringUtils.isBlank(js.optString("order_id"))){
			throw new RuntimeException("trade err!,result:"+reStr);
		}
		Long orderId = js.optLong("order_id");
		TradeOrder reTo = new TradeOrder();
        reTo.setId(orderId);
        reTo.setCode(""+orderId);
        return reTo;
	}
	/*
	 *
	 * 
	 * @see summ.btc.btclock.Tradable#sell(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public TradeOrder sell(String price, String amount) {
		try {
			String reStr=stockPost.trade("btc_cny", "sell",price,amount);
            return parseRs(reStr);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * 
	 * @see summ.btc.btclock.Tradable#cancel(java.lang.Long)
	 */
	@Override
	public void cancel(Long orderId) {
		try {
			String reStr=stockPost.cancel_order("btc_cny", ""+orderId);
			//reStr类似：{"success":"123456,123457","error":"123458,123459"}
			JSONObject js=JSONObject.fromObject(reStr);
			if(StringUtils.isNotBlank(js.optString("error_code"))){
				throw new RuntimeException("trade err!:" + js.optString("error_code"));
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} 
	}
	
	/**
	 * @return the apiKey
	 */
	public String getApiKey() {
		return apiKey;
	}
	/**
	 * @param apiKey the apiKey to set
	 */
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	/**
	 * @return the secretKey
	 */
	public String getSecretKey() {
		return secretKey;
	}
	/**
	 * @param secretKey the secretKey to set
	 */
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	/**
	 * @return the urlPrex
	 */
	public String getUrlPrex() {
		return urlPrex;
	}
	/**
	 * @param urlPrex the urlPrex to set
	 */
	public void setUrlPrex(String urlPrex) {
		this.urlPrex = urlPrex;
	}
	
//	/**
//	 * @return the stockPost
//	 */
//	public IStockRestApi getStockPost() {
//		return stockPost;
//	}
//	/**
//	 * @param stockPost the stockPost to set
//	 */
//	public void setStockPost(IStockRestApi stockPost) {
//		this.stockPost = stockPost;
//	}

}
