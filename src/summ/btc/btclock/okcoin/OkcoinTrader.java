/**
 * 
 */
package summ.btc.btclock.okcoin;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;

import com.okcoin.rest.StringUtil;
import com.okcoin.rest.stock.IStockRestApi;
import com.okcoin.rest.stock.impl.StockRestApi;

import summ.btc.btclock.Tradable;
import summ.btc.btclock.data.CurrencyTypeEnum;
import summ.btc.btclock.data.TradeOrder;
import summ.btc.btclock.data.TradeOrderStatusEnum;
import summ.btc.btclock.data.TradeTypeEnum;

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
	
	/* (non-Javadoc)
	 * @see summ.btc.btclock.Tradable#get(java.lang.Long)
	 */
	@Override
	public TradeOrder get(Long orderId) {
		try {
			String reStr=stockPost.order_info("btc_cny", ""+orderId);
			return parseOrders(reStr);
		}catch  (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} 
	}
	/*
{
    "result": true,
    "orders": [
        {
            "amount": 0.1,
            "avg_price": 0,
            "create_date": 1418008467000,
            "deal_amount": 0,
            "order_id": 10000591,
            "orders_id": 10000591,
            "price": 500,
            "status": 0,
            "symbol": "btc_cny",
            "type": "sell"
        },
        {
            "amount": 0.2,
            "avg_price": 0,
            "create_date": 1417417957000,
            "deal_amount": 0,
            "order_id": 10000724,
            "orders_id": 10000724,
            "price": 0.1,
            "status": 0,
            "symbol": "btc_cny",
            "type": "buy"
        }
    ]
}
	 */
	private TradeOrder parseOrders(String reStr){
		TradeOrder to=new TradeOrder();
		JSONObject js=JSONObject.fromObject(reStr);
		if(StringUtils.isNotBlank(js.optString("error_code"))){
			throw new RuntimeException("trade err!:" + js.optString("error_code"));
		}
		JSONArray odrJa=js.optJSONArray("orders");
		if(odrJa==null|| odrJa.size()<=0){
			return null;
		}
		if(odrJa.size()>1){
			System.out.println("warn:size>1 get by OrdergId res:"+reStr);
		}
		JSONObject orderJs=odrJa.optJSONObject(0); 
		to.setId(orderJs.optLong("order_id",0l));
		to.setCode(orderJs.optString("order_id"));
		to.setSubmitPrice(orderJs.optString("price"));//委托价
		to.setOrigAmount(orderJs.optString("amount"));//委托量
		//
		to.setStrikePrice(orderJs.optString("avg_price"));//平均成交价
		String dealAm=orderJs.optString("deal_amount"); //已成交量
		if(StringUtils.isNotBlank(dealAm) && StringUtils.isNotBlank(to.getOrigAmount())){
			BigDecimal origAmBd=new BigDecimal(to.getOrigAmount());
			BigDecimal dealAmBd=new BigDecimal(dealAm);
			BigDecimal nowAmBd=origAmBd.subtract(dealAmBd);
			nowAmBd.setScale(2, BigDecimal.ROUND_HALF_UP);
			to.setNowAmount(nowAmBd.toPlainString());
		}
//		to.setNowAmount(orderJs.optString("deal_amount")); //未成交量，okcoin只给已成交量。
	////TODO 需要根据情况判断设置
		to.setCurrencyType(CurrencyTypeEnum.CNY); 
		to.setTargetCurrencyType(CurrencyTypeEnum.BTC); 
		// 创建订单的时间戳
		long cTsL=orderJs.optLong("create_date",Long.MIN_VALUE);
		to.setCreatedTs(new Date(cTsL));
		//
		String typeStr= orderJs.optString("type");
		if("buy".equals(typeStr) || "buy_market".equals(typeStr) ){
			to.setTradeType(TradeTypeEnum.BID);
		}else if("sell".equals(typeStr) || "sell_market".equals(typeStr) ){
			to.setTradeType(TradeTypeEnum.ASK);
		}else{
			to.setTradeType(TradeTypeEnum.UNKNOWN);
		}
		//
		String stat=orderJs.optString("status");
		if("0".equals(stat) ){
			to.setStatus(TradeOrderStatusEnum.OPEN);
		}else if("2".equals(stat) ){
			to.setStatus(TradeOrderStatusEnum.CLOSED);
		}else if("4".equals(stat) ){
			to.setStatus(TradeOrderStatusEnum.CANCELLING);
		}else if("-1".equals(stat) ){
			to.setStatus(TradeOrderStatusEnum.CANCELLED);
		}else if("1".equals(stat) ){
			to.setStatus(TradeOrderStatusEnum.PENDING);
		}else{
			to.setStatus(TradeOrderStatusEnum.UNKNOWN);
		}
		return to;
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
