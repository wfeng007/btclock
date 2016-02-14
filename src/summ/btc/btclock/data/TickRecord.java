/**
 * 
 */
package summ.btc.btclock.data;

import java.util.Date;

/**
 * 报价器的报价记录
 * @author wfeng007
 * @date 2016-2-10 下午10:13:00
 */
public class TickRecord {
	private Date	timestamp;		//报价时间戳
//	private Date	localtime;		//本地时间点
	//实时信息
	private String              buy; //买1报价
	private String              sell; //卖1报价
	private String              last; //当前最后成交价
	//24小时信息
	private String              vol;//交易量（一般指24小时内，目标货币（商品）单位，如btc。）
	private String              high; //最高价格
	private String              low; //最低价格
//	private String              open; //24小时开盘价
//	private String              prevClose; //24小时为单位的收盘价
	/**
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}
	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	/**
	 * @return the buy
	 */
	public String getBuy() {
		return buy;
	}
	/**
	 * @param buy the buy to set
	 */
	public void setBuy(String buy) {
		this.buy = buy;
	}
	/**
	 * @return the sell
	 */
	public String getSell() {
		return sell;
	}
	/**
	 * @param sell the sell to set
	 */
	public void setSell(String sell) {
		this.sell = sell;
	}
	/**
	 * @return the last
	 */
	public String getLast() {
		return last;
	}
	/**
	 * @param last the last to set
	 */
	public void setLast(String last) {
		this.last = last;
	}
	/**
	 * @return the vol
	 */
	public String getVol() {
		return vol;
	}
	/**
	 * @param vol the vol to set
	 */
	public void setVol(String vol) {
		this.vol = vol;
	}
	/**
	 * @return the high
	 */
	public String getHigh() {
		return high;
	}
	/**
	 * @param high the high to set
	 */
	public void setHigh(String high) {
		this.high = high;
	}
	/**
	 * @return the low
	 */
	public String getLow() {
		return low;
	}
	/**
	 * @param low the low to set
	 */
	public void setLow(String low) {
		this.low = low;
	}

	
	
	

}
