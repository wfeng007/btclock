/**
 *
 */
package summ.btc.btclock;

import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import summ.btc.btclock.Kanban.Depth;
import summ.btc.btclock.MarketTrader.TradeOrderWaiter;
import summ.btc.btclock.data.TradeOrder;
import summ.btc.btclock.data.TradeOrderStatusEnum;

/**
 * 决策执行者，集中使用算法通知Trader进行买卖。
 *
 * @author wangfeng
 */
public class SimpleClockDecider {

	private Thread simpleClockTradeWorker = null;
	private SimpleClockRunner simpleClockRunner = null;
	
	private MarketTrader marketTrader=null;
    private Kanban kanban;

    static final String       AMOUNT                 = "0.0100";

    SimpleClockDecider() {
    }
    
    void startup() {
//    	threadpool=Executors.newFixedThreadPool(10);
    	if(this.simpleClockRunner==null||this.simpleClockRunner.isFinished){
    		this.simpleClockRunner = new SimpleClockRunner(20);
	    	this.simpleClockTradeWorker = new Thread(simpleClockRunner);
	    	this.simpleClockRunner.setFinished(false);
	        this.simpleClockTradeWorker.start();
    	}
    }

    void stop() {
//    	threadpool.shutdownNow();
        this.simpleClockRunner.setFinished(true);
    }
    
//    /**
//     * 
//     * @param amount
//     * @return
//     */
//    public Future<TradeOrder> offerSellOrderFuture(String amount) {
//    	TradeOrder order=offerSellOrder(amount);
//    	return threadpool.submit(new WaitingOrderCaller(this.kanban,this.trader,order.getId()));
//    }
//    /**
//     * 
//     * @param amount
//     * @return
//     */
//    public Future<TradeOrder> offerBuyOrderFuture(String amount) {
//    	TradeOrder order=offerBuyOrder(amount);
//    	FutureTask<TradeOrder> ft=new FutureTask<TradeOrder>(new WaitingOrderCaller(this.kanban,this.trader,order.getId()));
//    	threadpool.submit(ft,order);
//    	return ft;
//    }
    
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
                    if(inni.profit.compareTo(new BigDecimal(0))<0){//没有盈利 则考虑退避
                    	System.out.print("逃脱止损后退避20s");
                    	for (int i = 0; i < 20; i++) {
                    		Thread.sleep(1000);
                    		System.out.print(".");
						}
                    	System.out.println("退避结束");
                    }
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
         * @throws TimeoutException 
         * @throws ExecutionException 
         * @throws InterruptedException 
         * @TODO 继续
         */
        @SuppressWarnings("unused")
        private Innings biz() {
        	Innings inni=new Innings();
            //1下买入市价单1单，需要注意处理长时间部分成交。
        	TradeOrder toB=null;
        	for(;;){ //一直等到买到为止
	        	TradeOrderWaiter tow=marketTrader.buyWaiter(null,AMOUNT);
	        	toB=tow.await(5000);
	        	if(toB==null || !TradeOrderStatusEnum.CLOSED.equals(toB.getStatus())){
	        		toB=tow.canell(1000);
	        	}else{
	        		inni.entry=toB;
	        		break;
	        	}
        	}
            
            //2针对成交价计算卖出价
            String strikePriceStr = null;
            strikePriceStr = toB.getStrikePrice();//成交价 而不是报价 市价单报价时为0.00
            BigDecimal strikePrice = new BigDecimal(strikePriceStr);
            BigDecimal newPrice = strikePrice.add(new BigDecimal("0.05"));
            newPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
            String newPriceStr = newPrice.toPlainString();
//            System.out.println("价格计算---> 建仓价：" + strikePriceStr + "  盈利平仓价：" + newPriceStr);
            //...
            BigDecimal escPrice=strikePrice.subtract(new BigDecimal("0.40"));
            escPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
            String escPriceStr = escPrice.toPlainString();
            System.out.println("价格计算---> 建仓价：" + strikePriceStr + "  盈利平仓价：" + newPriceStr+  "  逃跑平仓价：" + escPriceStr);
            //
            //3 检测当前价格 直到为盈利或逃跑位
            //
            String exitPriceStr=null;
            for(;;){
            	Depth d = kanban.nowDepth;
            	String sell1=d.askList.get(0).getSubmitPrice();
            	String buy1=d.bidList.get(0).getSubmitPrice();
            	if(newPrice.compareTo(new BigDecimal(buy1))<0){//盈利平仓价 < buy1 时
            		exitPriceStr=newPrice.toPlainString();
            		System.out.println("使用盈利平仓价：" + exitPriceStr);
            	}else if(escPrice.compareTo(new BigDecimal(buy1))>0)  {
            		exitPriceStr=escPrice.toPlainString();
            		System.out.println("使用逃跑平仓价：" + exitPriceStr);
            	}
            	if(exitPriceStr!=null){
            		break;
            	}
            	try {
					Thread.sleep(100);//
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
            //
            //
            //

            //4下卖出价
            TradeOrder toS = null;
            //检测是否在超时周期内交易成功
            TradeOrderWaiter tow=marketTrader.sellWaiter(exitPriceStr,AMOUNT);
            for(;;){
	            toS=tow.await(5000);//如果5000中无法交易成功立即重新下卖单 并使用sell1价格
	            if(toS==null || !TradeOrderStatusEnum.CLOSED.equals(toS.getStatus())){
	            	toS=tow.canell(1000);
	            }else{
	            	inni.exit=toS;
	        		break;
	            }
	            System.out.println("没有及时成交，市价单卖。");
	            tow=marketTrader.sellWaiter(null,AMOUNT);
            }
            
            
            
            //完成处理
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
	 * @return the marketTrader
	 */
	public MarketTrader getMarketTrader() {
		return marketTrader;
	}

	/**
	 * @param marketTrader the marketTrader to set
	 */
	public void setMarketTrader(MarketTrader marketTrader) {
		this.marketTrader = marketTrader;
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
