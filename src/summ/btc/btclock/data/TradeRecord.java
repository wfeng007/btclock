/**
 * 
 */
package summ.btc.btclock.data;

import java.util.Date;

/**
 * 交易记录
 * @author wfeng007
 * @date 2016-2-10 下午09:45:59
 */
public class TradeRecord {
    private Long                 id;//数字单号
    private String               code; //非数字单号
    private TradeTypeEnum        tradeType;//交易类型
    private String               strikePrice;//实际成交价格
    private CurrencyTypeEnum     currencyType;//出价货币（商品）类型
    private CurrencyTypeEnum     targetCurrencyType;//目标货币（商品）类型
    private String               amount;       //成交前所有量
    private Date				 tradeTs;		//交易时间戳
    
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}
	/**
	 * @return the tradeType
	 */
	public TradeTypeEnum getTradeType() {
		return tradeType;
	}
	/**
	 * @param tradeType the tradeType to set
	 */
	public void setTradeType(TradeTypeEnum tradeType) {
		this.tradeType = tradeType;
	}
	/**
	 * @return the strikePrice
	 */
	public String getStrikePrice() {
		return strikePrice;
	}
	/**
	 * @param strikePrice the strikePrice to set
	 */
	public void setStrikePrice(String strikePrice) {
		this.strikePrice = strikePrice;
	}
	/**
	 * @return the currencyType
	 */
	public CurrencyTypeEnum getCurrencyType() {
		return currencyType;
	}
	/**
	 * @param currencyType the currencyType to set
	 */
	public void setCurrencyType(CurrencyTypeEnum currencyType) {
		this.currencyType = currencyType;
	}
	/**
	 * @return the targetCurrencyType
	 */
	public CurrencyTypeEnum getTargetCurrencyType() {
		return targetCurrencyType;
	}
	/**
	 * @param targetCurrencyType the targetCurrencyType to set
	 */
	public void setTargetCurrencyType(CurrencyTypeEnum targetCurrencyType) {
		this.targetCurrencyType = targetCurrencyType;
	}
	/**
	 * @return the amount
	 */
	public String getAmount() {
		return amount;
	}
	/**
	 * @param amount the amount to set
	 */
	public void setAmount(String amount) {
		this.amount = amount;
	}
	/**
	 * @return the tradeTs
	 */
	public Date getTradeTs() {
		return tradeTs;
	}
	/**
	 * @param tradeTs the tradeTs to set
	 */
	public void setTradeTs(Date tradeTs) {
		this.tradeTs = tradeTs;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TradeRecord [id=" + id + ", code=" + code + ", tradeType="
				+ tradeType + ", strikePrice=" + strikePrice
				+ ", currencyType=" + currencyType + ", targetCurrencyType="
				+ targetCurrencyType + ", amount=" + amount + ", tradeTs="
				+ tradeTs + "]";
	}
	
	
}
