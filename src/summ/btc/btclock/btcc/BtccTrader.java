/**
 *
 */
package summ.btc.btclock.btcc;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.DatatypeConverter;

import summ.btc.btclock.Tradable;
import summ.btc.btclock.data.TradeOrder;
import summ.btc.btclock.data.TradeOrderStatusEnum;
import summ.btc.btclock.data.TradeTypeEnum;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/**
 * @author wangfeng
 */
public class BtccTrader implements Tradable {

    private final String  ACCESS_KEY          = "xxx";
    private final String  SECRET_KEY          = "xxx";

    //bt中国秘钥
    //    API 访问密匙 (Access Key) : 4b1b04c1-e445-4125-9bd5-a02c47b2e91c
    //    API 秘密密匙 (Secret Key) : 8e2de793-a19d-449e-84d9-1c56bf7a2a71

    private static String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    //    private final String  postdata            = "";
    private final String  tonce               = "" + (System.currentTimeMillis() * 1000);
    private final String  url                 = "https://api.btcchina.com/api_trade_v1.php";

    private HttpsURLConnection buildConn(String opParams) {
        try {
            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
            String tonce = "" + (System.currentTimeMillis() * 1000);
            String params = "tonce=" + tonce.toString() + "&accesskey=" + ACCESS_KEY + "&" + opParams;
            String hash;
            hash = getSignature(params, SECRET_KEY);
            String userpass = ACCESS_KEY + ":" + hash;
            String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
            con.setRequestMethod("POST");
            con.setRequestProperty("Json-Rpc-Tonce", tonce.toString());
            con.setRequestProperty("Authorization", basicAuth);

            return con;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    //写入http内容。
    private void writeData(HttpsURLConnection con, String data) {
        DataOutputStream wr = null;
        try {
            con.setDoOutput(true);
            wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(data);
            wr.flush();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            try {
                if (wr != null)
                    wr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //读取http协议内容
    private String readData(HttpsURLConnection con) {
        BufferedReader in = null;
        try {
            int responseCode = con.getResponseCode();
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine = null;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            if (responseCode != 200) {
                //                System.out.println("Response Code : " + responseCode);
                //                System.out.println("Response Content : " + inputLine);
                throw new RuntimeException("HTTP执行异常！Response Code : " + responseCode + "\n HTTP应答内容返回如下：\n"
                        + response.toString());
            }
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getSignature(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {

        // get an hmac_sha1 key from the raw key bytes
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);

        // get an hmac_sha1 Mac instance and initialize with the signing key
        Mac mac = null;
        mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);

        // compute the hmac on input data bytes
        byte[] rawHmac = mac.doFinal(data.getBytes());

        return bytArrayToHex(rawHmac);
    }

    private static String bytArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for (byte b : a)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    /*
     * order内部 {"id":232477826,"type"
     * :"ask","price":"0.96","avg_price":"2580.27", "currency":"CNY","amount"
     * :"0.00000000","amount_original":"0.00100000"
     * ,"date":1453620039,"status":"closed"}
     */

    public static class TradeReqData {
        public Integer   id;
        public String    method;
        public JSONArray params = new JSONArray();

        public TradeReqData addParam(Object para) {
            this.params.add(para);
            return this;
        }

        public String buildBizParam() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < params.size(); i++) {
                Object oob = params.opt(i);
                if (params.opt(i) == null || params.opt(i) instanceof JSONNull) {
                    sb.append(",");
                } else {
                    sb.append(params.get(i).toString() + ",");
                }

            }
            String pStr;
            if (sb.length() > 0) {
                pStr = sb.substring(0, sb.length() - 1);
            } else {
                pStr = sb.toString();
            }
            String rt = "requestmethod=post&id=" + id + "&method=" + method + "&params=";//sb...
            return rt + pStr;
        }

        public JSONObject toJson() {
            return JSONObject.fromObject(this);
        }

        public String toJsonStr() {
            return JSONObject.fromObject(this).toString();
        }

        /**
         * @return the method
         */
        public String getMethod() {
            return method;
        }

        /**
         * @param method the method to set
         */
        public void setMethod(String method) {
            this.method = method;
        }

        /**
         * @return the params
         */
        public JSONArray getParams() {
            return params;
        }

        /**
         * @param params the params to set
         */
        public void setParams(JSONArray params) {
            this.params = params;
        }

        /**
         * @return the id
         */
        public Integer getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(Integer id) {
            this.id = id;
        }
    }

    /**
     * @author wangfeng
     */
    public static class TradeRespData {
        public int        id;
        public JSONObject result;   //还是嵌套则还有该属性
        public String     resultStr;
        public JSONObject errorJson;

        void fromData(String data) {
            JSONObject json = JSONObject.fromObject(data);
            this.errorJson = json.optJSONObject("error");
            if (this.errorJson != null) {
                return;
            }

            this.result = json.optJSONObject("result");
            if (this.result != null) {
                this.resultStr = this.result.toString();
                return;
            }

            Object objJs = json.opt("result");
            if (objJs != null) {
                resultStr = objJs.toString();
            }
        }
    }

    static class TradeData {
        public int    id;
        TradeReqData  req  = new TradeReqData();
        TradeRespData resp = new TradeRespData();
    }

    public TradeData trade(TradeData data) {
        HttpsURLConnection conn = null;
        try {
            String bizP = data.req.buildBizParam();
            conn = this.buildConn(bizP);
            this.writeData(conn, data.req.toJsonStr());
            String rdata = this.readData(conn);
            data.resp.fromData(rdata);
            return data;
        } finally {
            if (conn != null)
                conn.disconnect();
        }
    }
    
	/* (non-Javadoc)
	 * @see summ.btc.btclock.Tradable#buy(java.lang.String, java.lang.String)
	 */
	@Override
	public TradeOrder buy(String price, String amount) {
		return this.buy(price, amount, new TradeOption());
	}

	/* (non-Javadoc)
	 * @see summ.btc.btclock.Tradable#sell(java.lang.String, java.lang.String)
	 */
	@Override
	public TradeOrder sell(String price, String amount) {
		return this.sell(price, amount, new TradeOption());
	}

	/* (non-Javadoc)
	 * @see summ.btc.btclock.Tradable#cancel(java.lang.String)
	 */
	@Override
    public void cancel(Long orderId) {
        TradeData td6 = new TradeData();
        td6.req.id = 6;
        td6.req.method = "cancelOrder";
        td6.req.params.add(orderId);
        td6.req.params.add("BTCCNY");
        this.trade(td6);
    }

    public TradeOrder sell(String price, String amount, TradeOption tdeOpt) {
        TradeData td = new TradeData();
        td.req.id = 4;
        td.req.method = "sellOrder2";
        td.req.addParam(price).addParam(amount).addParam("BTCCNY");
        this.trade(td);
        if (td.resp.errorJson != null) {
            throw new RuntimeException("trade err!:" + td.resp.errorJson);
        }
        String orderId = td.resp.resultStr;
        if (tdeOpt.isWaitingClose()) {
            return waitingClose(orderId);
        } else {
            TradeOrder reTo = new TradeOrder();
            reTo.setId(Long.valueOf(orderId));
            reTo.setCode(orderId);
            return reTo;
        }
    }

    public static class TradeOption {
        boolean isWaitingClose = false;

        /**
         * @return the isWaitingClose
         */
        public boolean isWaitingClose() {
            return isWaitingClose;
        }

        /**
         * @param isWaitingClose the isWaitingClose to set
         */
        public void setWaitingClose(boolean isWaitingClose) {
            this.isWaitingClose = isWaitingClose;
        }
    }

    /**
     * 买下单。根据Option判断是否阻塞等待交易完成。
     *
     * @param price
     * @param amount
     * @param tdeOpt
     * @return
     */
    public TradeOrder buy(String price, String amount, TradeOption tdeOpt) {
        TradeData td = new TradeData();
        td.req.id = 1;
        td.req.method = "buyOrder2";
        td.req.addParam(price).addParam(amount).addParam("BTCCNY");
        this.trade(td);
        if (td.resp.errorJson != null) {
            throw new RuntimeException("trade err!:" + td.resp.errorJson);
        }
        String orderId = td.resp.resultStr;
        if (tdeOpt.isWaitingClose()) {
            return waitingClose(orderId);
        } else {
            TradeOrder reTo = new TradeOrder();
            reTo.setId(Long.valueOf(orderId));
            reTo.setCode(orderId);
            return reTo;
        }
    }

    public TradeOrder getOrder(TradeOrder order) {
        TradeData td = new TradeData();
        td.req.id = 2;
        td.req.method = "getOrder";
        td.req.addParam(order.getId());
        this.trade(td);
        if (td.resp.errorJson != null) {
            throw new RuntimeException("trade err!:" + td.resp.errorJson);
        }
        return parseRsToOrder(td.resp.result.optJSONObject("order"));
    }

    //
    private TradeOrder parseRsToOrder(JSONObject orderJs) {
        /*
         * order内部 {"id":232477826,"type"
         * :"ask","price":"0.96","avg_price":"2580.27",
         * "currency":"CNY","amount"
         * :"0.00000000","amount_original":"0.00100000"
         * ,"date":1453620039,"status":"closed"}
         */
        TradeOrder reTo = new TradeOrder();
        reTo.setId(orderJs.optLong("id"));
        reTo.setCode(orderJs.optString("id"));
        reTo.setTradeType(TradeTypeEnum.getByCode(orderJs.optString("type")));
        reTo.setSubmitPrice(orderJs.optString("price"));
        reTo.setStrikePrice(orderJs.optString("avg_price"));
        reTo.setOrigAmount(orderJs.optString("amount_original"));
        reTo.setNowAmount(orderJs.optString("amount"));
        String status = orderJs.optString("status");
        if ("closed".equalsIgnoreCase(status)) {
            reTo.setStatus(TradeOrderStatusEnum.CLOSED);
        } else if ("pending".equalsIgnoreCase(status)) {
            reTo.setStatus(TradeOrderStatusEnum.PENDING);
        }
        return reTo;
    }

    private TradeOrder waitingClose(String orderId) {
        return waitingClose(orderId, Integer.MAX_VALUE, 10);
    }

    /**
     * 轮询等待订单完成，如果超时则返回最后的订单情况。
     *
     * @param orderId
     * @param time 等待次数
     * @param slice 间隔时间
     * @return
     */
    public TradeOrder waitingClose(String orderId, int time, int slice) {
        if (slice < 10) {
            throw new RuntimeException("waitingClose err!, time:" + time + " slice:" + slice);
        }
        TradeOrder reTo = null;
        for (int i = 0; i <= time; i++) {
            TradeOrder order = new TradeOrder();
            order.setId(Long.valueOf(orderId));
            order.setCode(orderId);
            reTo = getOrder(order);
            if (TradeOrderStatusEnum.CLOSED.equals(reTo.getStatus()) && !"0".equalsIgnoreCase(reTo.getStrikePrice())) {
                break;//直到交易成功恢复
            } else {
                try {
                    Thread.sleep(slice);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return reTo;
    }

    public static void main(String[] args) {
        BtccTrader tdr = new BtccTrader();
        //        tdr.getAccountInfo().getOrders().sellOrder().buyOrder();

        //        TradeData td = new TradeData();
        //        td.req.id = 1;
        //        td.req.method = "getAccountInfo";
        //        tdr.trade(td);
        //        System.out.println(td.resp.resultStr);

    }


}
