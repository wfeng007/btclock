/**
 * 
 */
package summ.btc.btclock.okcoin;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import summ.btc.btclock.Kanban;
import summ.btc.btclock.MarketProbable;
import summ.btc.btclock.Kanban.Depth;
import summ.btc.btclock.data.CurrencyTypeEnum;
import summ.btc.btclock.data.TradeOrder;
import summ.btc.btclock.data.TradeOrderStatusEnum;
import summ.btc.btclock.data.TradeRecord;
import summ.btc.btclock.data.TradeTypeEnum;

import com.okcoin.websocket.WebSocketBase;
import com.okcoin.websocket.WebSocketService;

/**
 * @author wfeng007
 * @date 2016-2-13 下午05:20:15
 */
public class OkcoinMarketProbe implements MarketProbable,WebSocketService{

	private Kanban kanban;
	
	//apiKey 为用户申请的apiKey
	private String apiKey;
	//secretKey为用户申请的secretKey
	private String secretKey;
	//国内站WebSocket地址  注意如果访问国际站 请将 real.okcoin.cn 改为 real.okcoin.com
	private String url = "wss://real.okcoin.cn:10440/websocket/okcoinapi"; 
	
	
	//WebSocket客户端
	private WebSoketClient client ;
	
	
	/* (non-Javadoc)
	 * @see summ.btc.btclock.MarketProbable#init()
	 */
	@Override
	public void init() {
//		//订阅消息处理类,用于处理WebSocket服务返回的消息
//		WebSocketService service = new BuissnesWebSocketServiceImpl();
//		client = new WebSoketClient(url,service);
		client = new WebSoketClient(url,this);
		//启动客户端
		client.start();
		
		//
		//添加订阅
		//
		//市场报价
//		client.addChannel("ok_btccny_ticker");
		//市场深度
		client.addChannel("ok_btccny_depth");
		//
//		client.addChannel("ok_btccny_depth60");
		//实时成交
		client.addChannel("ok_btccny_trades_v1"); //
		//
		//个人现货交易订单变化
		client.realTrades(apiKey, secretKey);
	}
	
	static class WebSoketClient extends WebSocketBase{
		public WebSoketClient(String url,WebSocketService service){
			super(url,service);
		}
	}

	/* (non-Javadoc)
	 * @see summ.btc.btclock.MarketProbable#setKanban(summ.btc.btclock.SimpleKanban)
	 */
	@Override
	public void setKanban(Kanban kanban) {
		this.kanban=kanban;
	}

	/* (non-Javadoc)
	 * @see com.okcoin.websocket.WebSocketService#onReceive(java.lang.String)
	 */
	@Override
	public void onReceive(String msg) {
		if(msg.trim().startsWith("{")){
//			System.out.println(msg);
			return ;
		}
		JSONArray outerJa=JSONArray.fromObject(msg);
		for (int i = 0; i < outerJa.size(); i++) {
			JSONObject outerJs=outerJa.optJSONObject(i);
			String channel=outerJs.optString("channel");
			//
			if(StringUtils.isBlank(channel)){
				System.out.println("非注册通道："+outerJs.toString());
				continue;
			}
			
			//
			if("ok_btccny_ticker".equals(channel)){//ticker
			}else if("ok_btccny_trades_v1".equals(channel)){//整体实时交易信息
				
				JSONArray tradesJa=outerJs.optJSONArray("data");
				if(tradesJa==null){
					System.out.println(msg);
					continue;
				}
				for (int j = 0; j <  tradesJa.size(); j++) {
					JSONArray tJa=tradesJa.optJSONArray(j);
					TradeRecord tr=new TradeRecord();
					tr.setId(Long.valueOf(tJa.optString(0)));
					tr.setStrikePrice(tJa.optString(1));
					tr.setAmount(tJa.optString(2));
					//todo 时间
//					String tsStr= tJa.optString(3);
//					tr.setTradeTs(tradeTs);
					//交易类型
					if("bid".equals(tJa.optString(4))){
						tr.setTradeType(TradeTypeEnum.BID);
					}else if("ask".equals(tJa.optString(4))){
						tr.setTradeType(TradeTypeEnum.ASK);
					}else{
						tr.setTradeType(TradeTypeEnum.UNKNOWN);
					}
					//设置到看板。
					kanban.tradeRecords.offer(tr);
				}
				
			}else if("ok_btccny_depth".equals(channel)){//市场深度20条
//				System.out.println(msg);
				//1
				Depth newDp=new Depth();
				JSONObject depthJs=outerJs.optJSONObject("data");
				if(depthJs==null){
					System.out.println(msg);
					continue;
				}
				JSONArray bidsJa=depthJs.optJSONArray("bids");//买
				for (int j = 0; j < bidsJa.size(); j++) {
					JSONArray bJa=bidsJa.optJSONArray(j);
					TradeOrder dpTo=new TradeOrder();
					dpTo.setSubmitPrice(bJa.getString(0));//价格
					dpTo.setOrigAmount(bJa.getString(1));//btc量
					newDp.bidList.add(dpTo);
				}
				JSONArray asksJa=depthJs.optJSONArray("asks");//卖
				for (int j = 0; j < asksJa.size(); j++) {
					JSONArray aJa=asksJa.optJSONArray(j);
					TradeOrder dpTo=new TradeOrder();
					dpTo.setSubmitPrice(aJa.getString(0));//价格
					dpTo.setOrigAmount(aJa.getString(1));//btc量
					newDp.askList.add(dpTo);
				}
				Collections.reverse(newDp.askList);//倒序
				Long tsL=depthJs.optLong("timestamp");
				if(tsL!=null){
					newDp.timestamp=new Date(tsL);
				}
				
				//设置到看板。
				kanban.nowDepth=newDp;
				
			}else if("ok_btccny_depth60".equals(channel)){//市场深度60条
			}else if("ok_cny_realtrades".equals(channel)){ //个人订单
//				System.out.println(outerJs.optJSONObject("data"));
				/*
				 * {"averagePrice":"0","completedTradeAmount":"0","createdDate":
				 * 1455373879000
				 * ,"id":1909211500,"orderId":1909211500,"sigTradeAmount"
				 * :"0","sigTradePrice"
				 * :"0","status":-1,"symbol":"btc_cny","tradeAmount"
				 * :"0.01","tradePrice"
				 * :"0","tradeType":"sell","tradeUnitPrice":"2631.64"
				 * ,"unTrade":"0"}
				 */
				JSONObject orderJs=outerJs.optJSONObject("data");
				if(orderJs==null){ //[{"channel":"ok_cny_realtrades","success":"true"}]
					System.out.println(msg);
					continue;
				}
				
				TradeOrder to=new TradeOrder();
				to.setId(orderJs.optLong("id",0l));
				to.setCode(orderJs.optString("id"));
				to.setSubmitPrice(orderJs.optString("tradeUnitPrice"));//委托价
				to.setOrigAmount(orderJs.optString("tradeAmount"));//委托量
				//
				to.setStrikePrice(orderJs.optString("averagePrice"));//平均成交价
//				to.setNowAmount(orderJs.optString("completedTradeAmount")); //TODO 需要计算 有些提供完成量，有些提供剩余量。
				to.setNowAmount(orderJs.optString("unTrade"));
				////TODO 需要根据情况判断设置
				to.setCurrencyType(CurrencyTypeEnum.CNY); 
				to.setTargetCurrencyType(CurrencyTypeEnum.BTC); 
				// 创建订单的时间戳
				long cTsL=orderJs.optLong("createdDate",Long.MIN_VALUE);
				to.setCreatedTs(new Date(cTsL));
				//
				String typeStr= orderJs.optString("tradeType");
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
				
				//
				if("2".equals(stat)||"-1".equals(stat) ){
					kanban.closedOrders.offer(to);
					kanban.closedOrderMap.put(to.getId(), to);
					kanban.openningCache.remove(to.getId());
				}else{
					kanban.openningCache.put(to.getId(), to);
				}

				
			}else{
				
			}
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
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the kanban
	 */
	public Kanban getKanban() {
		return kanban;
	}

}
