/**
 *
 */
package summ.btc.btclock.btcc;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.net.URISyntaxException;
import java.security.DomainCombiner;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.json.JSONObject;

import com.sun.org.apache.bcel.internal.generic.NEW;

import summ.btc.btclock.Kanban;
import summ.btc.btclock.MarketProbable;
import summ.btc.btclock.Kanban.Depth;
import summ.btc.btclock.data.TickRecord;
import summ.btc.btclock.data.TradeRecord;
import summ.btc.btclock.data.TradeTypeEnum;
import summ.btc.sample.BTCCDataMain;

/**
 * 获取当前行情相关数据 trade 当前交易 grouporder 市场深度或买5卖5
 * 
 * @author wangfeng
 */
public class BtccMarketProbe implements MarketProbable{

	private Kanban kanban;

	// static interface DoMessage {
	// void onTrade(TradeRecord tradeRecord);
	//
	// void onTick(TickRecord tickRecord);
	//
	// }
	// public Queue<BtcMarketProbe.DoMessage> messageLister = new
	// LinkedList<BtcMarketProbe.DoMessage>();

	private void onTrade(JSONObject tradeJs) {
		// String[] rowData = { tradeJs.optString("trade_id"),
		// tradeJs.optString("price"),
		// tradeJs.optString("amount"), tradeJs.optString("type"),
		// tradeJs.optString("date"),
		// tradeJs.optString("market") };

		TradeRecord tr = new TradeRecord();
		tr.setId(tradeJs.optLong("trade_id"));
		tr.setCode(tradeJs.optString("trade_id"));
		tr.setStrikePrice(tradeJs.optString("price"));
		tr.setAmount(tradeJs.optString("amount"));
		//
		String typeStr = tradeJs.optString("type");
		if ("buy".equalsIgnoreCase(typeStr)) {
			tr.setTradeType(TradeTypeEnum.BID);
		} else if ("sell".equalsIgnoreCase(typeStr)) {
			tr.setTradeType(TradeTypeEnum.ASK);
		}
		//
		Long lTs = tradeJs.optLong("date", -1);

		if (lTs != null && lTs != -1) {
			lTs = lTs * 1000;
			tr.setTradeTs(new Date(lTs));
		}

		kanban.tradeRecords.offer(tr);
	}

	private void onTick(JSONObject tjs) {
		/*
		 * {"ticker":{"open":2539.15,"vwap":2560.94,"vol":20566.8968,"market":
		 * "btccny"
		 * ,"last":2587.71,"sell":2587.69,"buy":2586.38,"high":2600,"date"
		 * :1455112845,"low":2531.3,"prev_close":2538.01}}
		 */
		JSONObject tickJs = tjs.optJSONObject("ticker");
		if (tickJs == null) {
			return;
		}

		TickRecord tr = new TickRecord();
		Long lTs = tickJs.optLong("date", -1);
		if (lTs != null && lTs != -1) {
			lTs = lTs * 1000;
			tr.setTimestamp(new Date(lTs));
		}
		tr.setBuy(tickJs.optString("buy"));
		tr.setSell(tickJs.optString("sell"));
		tr.setLast(tickJs.optString("last"));
		tr.setHigh(tickJs.optString("high"));
		tr.setLow(tickJs.optString("low"));
		//
		kanban.tickRecords.offer(tr);
	}

	private void onDepth(JSONObject depthJs) {
		Depth de=new Depth();
		//解析
//		
//		org.json.JSONObject goJs = nowDe.optJSONObject("grouporder");
//        org.json.JSONArray goAskJa = goJs.optJSONArray("ask");
//        org.json.JSONArray gobidJa = goJs.optJSONArray("bid");
//        org.json.JSONObject ask1 = goAskJa.optJSONObject(goAskJa.length() - 1);
//        org.json.JSONObject bid1 = gobidJa.optJSONObject(0);
//        SimpleTradePane.bidAskPanel.setBidAsk(ask1.optString("price"), ask1.optString("totalamount"),
//                bid1.optString("price"), bid1.optString("totalamount"));
		
		kanban.nowDepth=de;
	}
	
	private void onMyOrder(JSONObject orderJs){
		
        /* 使用Trader获取到的内容
         * order内部 {"id":232477826,"type"
         * :"ask","price":"0.96","avg_price":"2580.27",
         * "currency":"CNY","amount"
         * :"0.00000000","amount_original":"0.00100000"
         * ,"date":1453620039,"status":"closed"}
         */
		
		 /*
//       * {"order":{"amount":0.01,"id":253002867,"price":2535.37,"market"
//       * :"btccny","status":"open","date":1454988653,"type":"ask",
//       * "amount_original":0.01}}
//       */
//      JSONObject orderJs = obj.optJSONObject("order");
//      Long id = orderJs.optLong("id");
//      String stu = orderJs.optString("status");
//      if ("open".equals(stu)) {
//          openningCache.put(id, orderJs);//放入未完成缓存
//      } else if ("closed".equals(stu)) {
//          openningCache.remove(id);
//          closedOrders.offer(orderJs); //放入已完成order队列
//      }
//      System.out.println("cache size:" + openningCache.size() + " closedOrders.size:" + closedOrders.size());
	}

	private final String ACCESS_KEY = "xx";
	private final String SECRET_KEY = "xxx";
	private static String HMAC_SHA1_ALGORITHM = "HmacSHA1";

	private String postdata = "";
	private final String tonce = "" + (System.currentTimeMillis() * 1000);

	private Socket socket;

	@Override
	public void init() {
		if (kanban == null) {
			throw new RuntimeException("kanban is not be null!!");
		}
		try {
			//
			IO.Options opt = new IO.Options();
			opt.reconnection = true;
			Logger.getLogger(BTCCDataMain.class.getName()).setLevel(Level.FINE);
			socket = IO.socket("https://websocket.btcchina.com", opt); // 地址
			socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {// 链接上
						// BTCCDataMain sm = new BTCCDataMain();

						@Override
						public void call(Object... args) {
							System.out.println("Connected!");
							// 订阅市场交易（marketdata）及市场深度（grouporder）
							socket.emit("subscribe", "marketdata_cnybtc"); // subscribe
							socket.emit("subscribe", "grouporder_cnybtc"); // subscribe
																			// grouporder
							// Use 'private' method to subscribe the order and
							// balance feed
							try {
								List arg = new ArrayList();
								arg.add(getPayload());
								arg.add(getSign());
								socket.emit("private", arg); // 私人（本账户）信息
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).on("message", new Emitter.Listener() { // 通知信息
						@Override
						public void call(Object... args) {
							System.out.println(args[0]);
						}

					}).on("trade", new Emitter.Listener() { // 市场行情
						@Override
						public void call(Object... args) {
							JSONObject json = (JSONObject) args[0]; // receive
																	// the trade
																	// message
							// for (DoMessage dm : messageLister) {
							// dm.onTrade(json);
							// }
							BtccMarketProbe.this.onTrade(json);
						}
					}).on("ticker", new Emitter.Listener() {// 交易行情
						@Override
						public void call(Object... args) {
							JSONObject json = (JSONObject) args[0];// receive
																	// the
																	// ticker
																	// message
							// for (DoMessage dm : messageLister) {
							// dm.onTick(json);
							// }
							BtccMarketProbe.this.onTick(json);
						}
					}).on("grouporder", new Emitter.Listener() {// 市场深度
						@Override
						public void call(Object... args) {
							JSONObject json = (JSONObject) args[0];// receive
																	// the
																	// grouporder
																	// message
							// for (DoMessage dm : messageLister) {
							// dm.onGrouporder(json);
							// }
							BtccMarketProbe.this.onDepth(json);
						}
					}).on("order", new Emitter.Listener() {// 私人订单情况
						@Override
						public void call(Object... args) {
							JSONObject json = (JSONObject) args[0];// receive
																	// the order
																	// message
							// for (DoMessage dm : messageLister) {
							// dm.onMyOrder(json);
							// }
							onMyOrder(json);
						}
					}).on("account_info", new Emitter.Listener() {// 私人账户信息
						@Override
						public void call(Object... args) {
							JSONObject json = (JSONObject) args[0];// receive
																	// the
																	// balance
																	// message
							// for (DoMessage dm : messageLister) {
							// dm.onMyAccount(json);
							// }
						}
					}).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {// 断开链接
						@Override
						public void call(Object... args) {
							System.out.println("Disconnected!");
						}
					});
			socket.connect();
		} catch (URISyntaxException ex) {
			Logger.getLogger(BTCCDataMain.class.getName()).log(Level.SEVERE,
					null, ex);
		} finally {

		}
	}

	public String getPayload() throws Exception {
		postdata = "{\"tonce\":\""
				+ tonce.toString()
				+ "\",\"accesskey\":\""
				+ ACCESS_KEY
				+ "\",\"requestmethod\": \"post\",\"id\":\""
				+ tonce.toString()
				+ "\",\"method\": \"subscribe\", \"params\": [\"order_cnybtc\",\"order_cnyltc\",\"order_btcltc\",\"account_info\"]}";// subscribe
																																		// order
																																		// and
																																		// balance
																																		// feed

		// System.out.println("postdata is: " + postdata);
		return postdata;
	}

	public String getSign() throws Exception {
		String params = "tonce="
				+ tonce.toString()
				+ "&accesskey="
				+ ACCESS_KEY
				+ "&requestmethod=post&id="
				+ tonce.toString()
				+ "&method=subscribe&params=order_cnybtc,order_cnyltc,order_btcltc,account_info";
		String hash = getSignature(params, SECRET_KEY);
		String userpass = ACCESS_KEY + ":" + hash;
		String basicAuth = DatatypeConverter.printBase64Binary(userpass
				.getBytes());
		// System.out.println(basicAuth);
		return basicAuth;
	}

	private String getSignature(String data, String key) throws Exception {
		// get an hmac_sha1 key from the raw key bytes
		SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(),
				HMAC_SHA1_ALGORITHM);
		// get an hmac_sha1 Mac instance and initialize with the signing key
		Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
		mac.init(signingKey);
		// compute the hmac on input data bytes
		byte[] rawHmac = mac.doFinal(data.getBytes());
		return bytArrayToHex(rawHmac);
	}

	private String bytArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder();
		for (byte b : a)
			sb.append(String.format("%02x", b & 0xff));

		// System.out.println(sb);
		return sb.toString();
	}

	public void shutdown() {
		socket.disconnect();
		socket.close();
	}

	// private class ProbeRunner implements Runnable {
	//
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// }

	/**
	 * @return the kanban
	 */
	public Kanban getKanban() {
		return kanban;
	}

	/**
	 * @param kanban
	 *            the kanban to set
	 */
	@Override
	public void setKanban(Kanban kanban) {
		this.kanban = kanban;
	}

}
