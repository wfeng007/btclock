/**
 * 
 */
package summ.btc.btclock;

import summ.btc.btclock.btcc.BtccTrader.TradeOption;
import summ.btc.btclock.data.TradeOrder;

/**
 * @author wfeng007
 * @date 2016-2-13 下午03:20:44
 */
public interface Tradable {
	
	public TradeOrder buy(String price, String amount);
	
	public TradeOrder sell(String price, String amount);
	
	public TradeOrder get(Long orderId);
	
	public void cancel(Long orderId);
	
//	public void cancel(String orderCode);

}
