/**
 *
 */
package summ.btc.btclock.data;

/**
 * @author wangfeng
 */
public enum CurrencyTypeEnum {

    CNY(1, "cny", "人民币"),
    USD(2, "usd", "美刀"),
    BTC(21, "btc", "比特币"),
    LTC(22, "ltc", "比特币"),
    UNKNOWN(99, "unknown", "未知"), ;

    private int    id;
    private String code;
    private String msg;

    private CurrencyTypeEnum(int id, String code, String msg) {
        this.id = id;
        this.code = code;
        this.msg = msg;
    }

    public static CurrencyTypeEnum getById(int id) {
        for (CurrencyTypeEnum type : CurrencyTypeEnum.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public static CurrencyTypeEnum getByCode(String code) {
        for (CurrencyTypeEnum type : CurrencyTypeEnum.values()) {
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
