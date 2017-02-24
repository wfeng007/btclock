/**
 *
 */
package summ.btc.btclock.data;

/**
 * @author wangfeng
 */
public enum TradeTypeEnum {

    BID(1, "bid", "现货买"),
    ASK(2, "ask", "现货卖"),
    UNKNOWN(99, "unknown", "未知"), ;

    private int    id;
    private String code;
    private String msg;

    private TradeTypeEnum(int id, String code, String msg) {
        this.id = id;
        this.code = code;
        this.msg = msg;
    }
    
    public static TradeTypeEnum getById(int id) {
        for (TradeTypeEnum type : TradeTypeEnum.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public static TradeTypeEnum getByCode(String code) {
        for (TradeTypeEnum type : TradeTypeEnum.values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
