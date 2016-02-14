/**
 *
 */
package summ.btc.btclock;

import java.math.BigDecimal;

import summ.btc.btclock.btcc.BtccTrader;
import summ.btc.btclock.btcc.BtccTrader.TradeOption;
import summ.btc.btclock.data.TradeOrder;
import summ.btc.btclock.data.TradeOrderStatusEnum;

/**
 * 决策执行者，集中使用算法通知Trader进行买卖。
 *
 * @author wangfeng
 */
public class SimpleClockDecider {

    private Thread            simpleClockTradeWorker = null;
    private SimpleClockRunner simpleClockRunner      = null;

    static final String       AMOUNT                 = "0.0100";

    SimpleClockDecider(BtccTrader btccTrader) {
        simpleClockRunner = new SimpleClockRunner(btccTrader, 20);
        simpleClockTradeWorker = new Thread(simpleClockRunner);
    }

    void startup() {
        this.simpleClockTradeWorker.start();
    }

    void stop() {
        this.simpleClockRunner.setFinished(true);
    }

    static final class SimpleClockRunner implements Runnable {

        private BtccTrader  tdr        = null;

        private boolean isFinished = false;

        private int     count      = 0;

        public SimpleClockRunner(BtccTrader tdr, int count) {
            super();
            this.tdr = tdr;
            this.count = count;
        }

        @Override
        public void run() {
            //定期循环 等待周期为一个局间隔1分钟。 10次。
            while (true) {
                if (count > 0) {
                    count--;
                } else {
                    this.isFinished = true;
                }
                if (isFinished) {
                    break;
                }
                try {
                    biz();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("SimpleClockRunner Err!:" + e.getMessage(), e);
                } finally {
                    //Think time 应自自适应等待以达到 胜率最大
                    //                    try {
                    //                        Thread.sleep(500);
                    //                    } catch (InterruptedException e) {
                    //                        e.printStackTrace();
                    //                    }
                }
            }
            //
        }

        //一局（进出组成的一组交易，最简单的是进出各1单）
        /**
         * @TODO 继续
         */
        @SuppressWarnings("unused")
        private void biz() {
            //1下买入市价单1单，需要注意处理长时间部分成交。
            TradeOrder toB = tdr.buy(null, AMOUNT, new TradeOption());
            //3针对成交价计算卖出价
            String strikePriceStr = null;
            strikePriceStr = toB.getStrikePrice();//成交价 而不是报价 市价单报价时为0.00
            BigDecimal strikePrice = new BigDecimal(strikePriceStr);

            BigDecimal newPrice = strikePrice.add(new BigDecimal("0.76"));
            newPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
            String newPriceStr = newPrice.toString();
            System.out.println("建仓价：" + strikePriceStr + "  准备平仓价：" + newPriceStr);

            //4下卖出价
            TradeOption sellOpt = new TradeOption();
            sellOpt.setWaitingClose(false);
            TradeOrder toS = tdr.sell(newPriceStr, AMOUNT, sellOpt);
            //检测是否在超时周期内交易成功
            String orderId2 = toS.getCode();
            toS = tdr.waitingClose(orderId2, 60, 5000);

            //完成处理
            if (TradeOrderStatusEnum.CLOSED.equals(toS.getStatus())) {
                System.out.println("一局交易成功！");
                System.out.println(toS);
            } else {//5超时未出单取消订单并直接市价出单
                System.out.println("一局交易超时未出单，取消订单并直接市价出单！");

                //超时，取消之前订单
                tdr.cancel(Long.valueOf(orderId2));

                //超时，市价单平仓
                TradeOption sell2Opt = new TradeOption();
                sell2Opt.setWaitingClose(false);
                tdr.sell(null, AMOUNT, sell2Opt);

            }
        }

        /**
         * @return the tdr
         */
        public BtccTrader getTdr() {
            return tdr;
        }

        /**
         * @param tdr the tdr to set
         */
        public void setTdr(BtccTrader tdr) {
            this.tdr = tdr;
        }

        /**
         * @param isFinished the isFinished to set
         */
        public void setFinished(boolean isFinished) {
            this.isFinished = isFinished;
        }

    }

    /**
     * @return the simpleClockTradeWorker
     */
    public Thread getSimpleClockTradeWorker() {
        return simpleClockTradeWorker;
    }

    public static void main(String[] args) {
        BtccTrader tdr = new BtccTrader();
        SimpleClockDecider scd = new SimpleClockDecider(tdr);
        scd.startup();
    }
}
