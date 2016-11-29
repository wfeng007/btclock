/**
 * 
 */
package summ.btc.btclock.okcoin;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpException;

import summ.btc.btclock.data.TradeOrder;

import com.okcoin.rest.stock.IStockRestApi;
import com.okcoin.rest.stock.impl.StockRestApi;

/**
 * TODO 还没有完成,具体作用可能是想从probe与trader拆分出链接服务器的功能。
 * 
 * @author wfeng007
 * @date 2016-10-23 上午02:46:17
 */
public class OkcoinMyDataLoader {
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
	
	
//	public List<TradeOrder> listMyOrders(String type){
//		try {
//			stockPost.order_history("btc_usd", type, "1", "30");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
	
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

}
