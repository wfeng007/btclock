/**
 * 
 */
package summ.btc.btclock.data;

import java.math.BigDecimal;

/**
 * @author wfeng007
 * @date 2016年11月27日 下午5:32:34
 */
public class Innings {
	
	public TradeOrder entry;
	public TradeOrder exit;
	
	public BigDecimal profit;
	public BigDecimal parseProfit(){
		BigDecimal exitOrigAmBd=new BigDecimal(exit.getOrigAmount());
		BigDecimal exitStriPrBd=new BigDecimal(exit.getStrikePrice());
		BigDecimal entryOrigAmBd=new BigDecimal(entry.getOrigAmount());
		BigDecimal entryStriPrBd=new BigDecimal(entry.getStrikePrice());
		
		BigDecimal exitTotal= exitOrigAmBd.multiply(exitStriPrBd);
		BigDecimal entryTotal= entryOrigAmBd.multiply(entryStriPrBd);
		BigDecimal profit=exitTotal.subtract(entryTotal);
		profit.setScale(8, BigDecimal.ROUND_HALF_UP);
		this.profit=profit;
		return profit;
	}

}
