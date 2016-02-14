/**
 *
 */
package summ.btc.btclock.data;

import java.util.Date;

/**
 * @author wangfeng
 */
public class TradeOrder {
    private Long                 id;//数字单号
    private String               code; //非数字单号
    private TradeTypeEnum        tradeType;//交易类型
    private String               submitPrice;//出价价格
    private String               strikePrice;//实际成交价格
    private CurrencyTypeEnum     currencyType;//出价货币（商品）类型
    private CurrencyTypeEnum     targetCurrencyType;//目标货币（商品）类型
    private String               nowAmount;        //本订单现有剩余量
    private String               origAmount;       //成交前所有量
    private TradeOrderStatusEnum status;           //状态
    private Date 				 createdTs;           //创建时间

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
     * @return the submitPrice
     */
    public String getSubmitPrice() {
        return submitPrice;
    }

    /**
     * @param submitPrice the submitPrice to set
     */
    public void setSubmitPrice(String submitPrice) {
        this.submitPrice = submitPrice;
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
     * @return the targeCurrencyType
     */
    public CurrencyTypeEnum getTargetCurrencyType() {
        return targetCurrencyType;
    }

    /**
     * @param targeCurrencyType the targeCurrencyType to set
     */
    public void setTargetCurrencyType(CurrencyTypeEnum targeCurrencyType) {
        this.targetCurrencyType = targeCurrencyType;
    }

    /**
     * @return the nowAmount
     */
    public String getNowAmount() {
        return nowAmount;
    }

    /**
     * @param nowAmount the nowAmount to set
     */
    public void setNowAmount(String nowAmount) {
        this.nowAmount = nowAmount;
    }

    /**
     * @return the origAmount
     */
    public String getOrigAmount() {
        return origAmount;
    }

    /**
     * @param origAmount the origAmount to set
     */
    public void setOrigAmount(String origAmount) {
        this.origAmount = origAmount;
    }

    /**
     * @return the status
     */
    public TradeOrderStatusEnum getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(TradeOrderStatusEnum status) {
        this.status = status;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TradeOrder [id=" + id + ", code=" + code + ", tradeType=" + tradeType + ", submitPrice=" + submitPrice
                + ", strikePrice=" + strikePrice + ", currencyType=" + currencyType + ", targeCurrencyType="
                + targetCurrencyType + ", nowAmount=" + nowAmount + ", origAmount=" + origAmount + ", status=" + status
                + "]";
    }

	/**
	 * @return the createdTs
	 */
	public Date getCreatedTs() {
		return createdTs;
	}

	/**
	 * @param createdTs the createdTs to set
	 */
	public void setCreatedTs(Date createdTs) {
		this.createdTs = createdTs;
	}

}
