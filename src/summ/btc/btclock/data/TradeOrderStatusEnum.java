/**
 *
 */
package summ.btc.btclock.data;

/**
 * @author wangfeng
 */
public enum TradeOrderStatusEnum {

    OPEN(1, "open", "新建"),
    PENDING(5, "pending", "等待"), //btc //交易一部分则是这个状态
 
    //okcoin -1已撤销,0等待成交,1部分成交,2完全成交,4撤单处理中
    
    CANCELLING(6,"cancelling","撤单中"),
    CLOSED(7, "closed", "结束"),//成交结束 //一般指成功交易结束  根据deal_amount判断是否完全成交
    CANCELLED(8,"cancelled","撤单结束"),
    UNKNOWN(99, "unknown", "未知"),
    ;

    private int    id;
    private String code;
    private String msg;

    private TradeOrderStatusEnum(int id, String code, String msg) {
        this.id = id;
        this.code = code;
        this.msg = msg;
    }

    public static TradeOrderStatusEnum getById(int id) {
        for (TradeOrderStatusEnum type : TradeOrderStatusEnum.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public static TradeOrderStatusEnum getByCode(String code) {
        for (TradeOrderStatusEnum type : TradeOrderStatusEnum.values()) {
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
