/**
 *
 */
package summ.btc.btclock;

import java.math.BigDecimal;
import java.util.concurrent.Future;

import javax.swing.JOptionPane;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import summ.btc.btclock.Kanban.Depth;
import summ.btc.btclock.btcc.BtccTrader;
import summ.btc.btclock.btcc.BtccTrader.TradeOption;
import summ.btc.btclock.data.TradeOrder;
import summ.btc.btclock.data.TradeOrderStatusEnum;
import summ.btc.btclock.okcoin.OkcoinMarketProbe;
import summ.btc.btclock.okcoin.OkcoinTrader;

/**
 * 决策执行者，集中使用算法通知Trader进行买卖。
 *
 * @author wangfeng
 */
public class SimpleClockDecider {

    private Thread            simpleClockTradeWorker = null;
    private SimpleClockRunner simpleClockRunner      = null;
    
    private Tradable  trader;
    private Kanban kanban;

    static final String       AMOUNT                 = "0.1000";

    SimpleClockDecider() {
    }
    
    void startup() {
    	
    	if(this.simpleClockRunner==null||this.simpleClockRunner.isFinished){
    		this.simpleClockRunner = new SimpleClockRunner(20);
	    	this.simpleClockTradeWorker = new Thread(simpleClockRunner);
	    	this.simpleClockRunner.setFinished(false);
	        this.simpleClockTradeWorker.start();
    	}
    }

    void stop() {
        this.simpleClockRunner.setFinished(true);
    }
    
    /*
     * 下买单 ，买一价。不等待成交。
     */
    public TradeOrder offerBuyOrder(String amount) {
    		Depth d = kanban.nowDepth;
			if (d.bidList.size() <= 0) {
				throw new RuntimeException("无参考价格用于下单。");
			}
			TradeOrder to = d.bidList.get(0);
			System.out.println("买入价：" + to.getSubmitPrice() + " 买入量："
					+ amount);
    		return trader.buy(to.getSubmitPrice(), amount);
    }
    
    /*
     * 下卖单，卖一价。不等待成交。
     */
    public TradeOrder offerSellOrder(String amount) {
			Depth d = kanban.nowDepth;
			if (d.askList.size() <= 0) {
				throw new RuntimeException("无参考价格用于下单。");
			}
			TradeOrder to = d.askList.get(0);
			System.out.println("卖出价：" + to.getSubmitPrice() + " 卖出量："
					+ amount);
			return trader.sell(to.getSubmitPrice(), amount);
	}
    
    public TradeOrder waitingTrade(Long id){
    		if(id==null){
    			throw new NullPointerException("id not be Null!");
    		}
//    		TradeOrder to=new TradeOrder();
//    		to.setId(id);
    		outFor:for(;;){
    			for (int i = 0; i < 20; i++) {
    				if(kanban.closedOrderMap.containsKey(id)){
    					return kanban.closedOrderMap.get(id);
    				}else{
    					try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
    				}
				}
    			//通过远端访问
    			TradeOrder to=this.trader.get(id);
    			System.out.println(to);
    			if(TradeOrderStatusEnum.CLOSED.equals(to.getStatus())){
    				return to;
    			}
    		}
    }
    
    public static class Innings {
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
    

    final class SimpleClockRunner implements Runnable {

        private boolean isFinished = true;

        private int     count      = 0;

        public SimpleClockRunner( int count) {
            super();
            this.count = count;
        }

        @Override
        public void run() {
            //定期循环 等待周期为一个局间隔1分钟。 10次。
        	BigDecimal matchTotaProfit=new BigDecimal(0); 
//        	matchTotaProfit.setScale(8);
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
                    Innings inni=biz();
                    matchTotaProfit=matchTotaProfit.add(inni.profit);
                    System.out.println("now Total profit:"+matchTotaProfit.toPlainString());
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
        private Innings biz() {
        	Innings inni=new Innings();
            //1下买入市价单1单，需要注意处理长时间部分成交。
            TradeOrder toB = offerBuyOrder(AMOUNT);
            toB=waitingTrade(toB.getId()); //TODO 增加超时撤单，并判断是否为entry则重试。
            inni.entry=toB;
            
            //3针对成交价计算卖出价
            String strikePriceStr = null;
            strikePriceStr = toB.getStrikePrice();//成交价 而不是报价 市价单报价时为0.00
            BigDecimal strikePrice = new BigDecimal(strikePriceStr);

            BigDecimal newPrice = strikePrice.add(new BigDecimal("0.05"));
            newPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
            String newPriceStr = newPrice.toString();
            System.out.println("建仓价：" + strikePriceStr + "  准备平仓价：" + newPriceStr);

            //4下卖出价
            TradeOrder toS = trader.sell(newPriceStr, AMOUNT);
            //检测是否在超时周期内交易成功
            toS=waitingTrade(toS.getId());//TODO 增加超时撤单，并判断是否为exit则市价强制卖单。
            inni.exit=toS;

            //完成处理
/*            if (TradeOrderStatusEnum.CLOSED.equals(toS.getStatus())) {
                System.out.println("一局交易成功！");
                System.out.println(toS);
            } else {//5超时未出单取消订单并直接市价出单
                System.out.println("一局交易超时未出单，取消订单并直接市价出单！");

                //超时，取消之前订单
                trader.cancel(Long.valueOf(orderId2));

                //超时，市价单平仓
                TradeOption sell2Opt = new TradeOption();
                sell2Opt.setWaitingClose(false);
//                trader.sell(null, AMOUNT, sell2Opt);

            }*/
            inni.parseProfit();
            System.out.println(inni.profit.toPlainString());
            return inni;
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

    static AbstractApplicationContext appContext;
    public static void main(String[] args) {
    	appContext=new ClassPathXmlApplicationContext("applicationContext-beans.xml");
    	SimpleClockDecider decider=(SimpleClockDecider)appContext.getBean("simpleClockDecider");
    	try {
			Thread.sleep(15* 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	decider.startup();
    }

	/**
	 * @return the trader
	 */
	public Tradable getTrader() {
		return trader;
	}

	/**
	 * @param trader the trader to set
	 */
	public void setTrader(Tradable trader) {
		this.trader = trader;
	}

	/**
	 * @return the kanban
	 */
	public Kanban getKanban() {
		return kanban;
	}

	/**
	 * @param kanban the kanban to set
	 */
	public void setKanban(Kanban kanban) {
		this.kanban = kanban;
	}
}
