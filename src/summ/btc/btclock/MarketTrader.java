/**
 * 
 */
package summ.btc.btclock;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import summ.btc.btclock.Kanban.Depth;
import summ.btc.btclock.data.TradeOrder;

/**
 * @author wfeng007
 * @date 2016-3-6 下午03:13:43
 */
public class MarketTrader {
	
	private ExecutorService threadpool = null;
    private Tradable  trader;
    private Kanban kanban;

	/**
	 * 下订单，并返回一个可以担待订单结束的句柄
	 */
	public TradeOrderWaiter buyWaiter(String price, String amount) {
		TradeOrder order = offerBuyOrder(price, amount);
		return new TradeOrderWaiter(order, this.trader, this.kanban,
				this.threadpool);
	}

	/**
	 * 下订单，并返回一个可以担待订单结束的句柄
	 */
	public TradeOrderWaiter sellWaiter(String price, String amount) {
		TradeOrder order = offerSellOrder(price, amount);
		return new TradeOrderWaiter(order, this.trader, this.kanban,
				this.threadpool);
	}
	
	/*
     * 下买单 ，买一价。不等待成交。
     */
    public TradeOrder offerBuyOrder(String price,String amount) {
			if(price!=null){
				return trader.buy(price, amount);
			}
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
    public TradeOrder offerSellOrder(String price,String amount) {
    		if(price!=null){
    			return trader.sell(price, amount);
    		}
			Depth d = kanban.nowDepth;
			if (d.askList.size() <= 0) {
				throw new RuntimeException("无参考价格用于下单。");
			}
			TradeOrder to = d.askList.get(0);
			System.out.println("卖出价：" + to.getSubmitPrice() + " 卖出量："
					+ amount);
			return trader.sell(to.getSubmitPrice(), amount);
	}

    /**
     * 用于等待及取消的句柄
     * @author wfeng007
     * @date 2016-3-7 下午11:11:43
     */
	public static class TradeOrderWaiter {

		private TradeOrder waitingTradeOrder;
		private Tradable trader;
		private ExecutorService threadpool;
		private Kanban kanban;

		private TradeOrderWaiter(TradeOrder wto, Tradable trader,
				Kanban kanban, ExecutorService threadpool) {
			this.waitingTradeOrder = wto;
			this.trader = trader;
			this.kanban = kanban;
			this.threadpool = threadpool;
		}

		// 不等待
		// public TradeOrder canellAndEscpe(){
		// TradeOrder to=canell(1000);
		// waitingTradeOrder=this.trader.get(waitingTradeOrder.getId());
		// //escpe
		// return
		// escpe(waitingTradeOrder.getOrigAmount(),waitingTradeOrder.getTradeType());
		// }
		// private TradeOrder escpe(String amount,TradeTypeEnum type){
		// Depth d = kanban.nowDepth;
		// if(TradeTypeEnum.ASK.equals(type)){
		// if (d.bidList.size() <= 0) {
		// throw new RuntimeException("无参考价格用于下单。");
		// }
		// TradeOrder to = d.bidList.get(0);
		// return trader.sell(to.getSubmitPrice(), amount);
		// }else if(TradeTypeEnum.BID.equals(type)){
		// if (d.askList.size() <= 0) {
		// throw new RuntimeException("无参考价格用于下单。");
		// }
		// TradeOrder to = d.askList.get(0);
		// return trader.buy(to.getSubmitPrice(), amount);
		// }
		// return null;
		// }

		/**
		 * 取消该订单
		 * 
		 * @param timeout
		 * @return
		 */
		public TradeOrder canell(long timeout) {
			this.trader.cancel(waitingTradeOrder.getId());
			Future<TradeOrder> ft = this.threadpool
					.submit(new WaitingOrderCaller(this.kanban, this.trader,
							waitingTradeOrder.getId()));
			TradeOrder reTo = null;
			try {
				reTo = ft.get(timeout, TimeUnit.MILLISECONDS);
			} catch (TimeoutException toe) {
				System.out.println("等待订单撤销时超时。"); // 超时不用打印
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (reTo != null && ft.isDone()) {
				return reTo;
			}
			ft.cancel(true);// 不在检测
			return this.trader.get(waitingTradeOrder.getId());
		}

		/**
		 * 等待订单成交或取消
		 * 
		 * @return
		 */
		public TradeOrder await() {
			return this.await(Long.MAX_VALUE);
		}

		/**
		 * 等待订单成交，并有超时。 超时返回现有订单情况。
		 * 
		 * @return
		 */
		public TradeOrder await(long timeout) {
			Future<TradeOrder> ft = this.threadpool
					.submit(new WaitingOrderCaller(this.kanban, this.trader,
							waitingTradeOrder.getId()));
			TradeOrder reTo = null;
			try {
				reTo = ft.get(timeout, TimeUnit.MILLISECONDS);
			} catch (TimeoutException toe) {
				System.out.println("等待订单交易超时。" + toe.getMessage()); // 超时不用打印
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (reTo != null && ft.isDone()) {
				return reTo;
			}
			ft.cancel(true);// 不在检测
			return this.trader.get(waitingTradeOrder.getId()); //
			// return null;
		}

	}
	
	
	/**
	 * 
	 * @author wfeng007
	 * 
	 */
	public static class WaitingOrderCaller implements Callable<TradeOrder>{

		private Thread doingThread;
		private Kanban kanban;
		private Tradable trader;
		private Long id;
		public WaitingOrderCaller(Kanban kanban,Tradable trader,Long id){
			this.kanban=kanban;
			this.trader=trader;
			this.id=id;
		}
		
		/**
		 * 考虑可以让外部系统打断正在执行本Callable的机制
		 */
		public void interupt(){
			if(this.doingThread!=null)
				this.doingThread.interrupt();
		}
		
		/**
		 * 
		 */
		@Override
		public TradeOrder call() throws Exception {
			this.doingThread=Thread.currentThread();
			for (;;) {
				if(kanban.closedOrderMap.containsKey(id)){
					return kanban.closedOrderMap.get(id);
				}else{
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
//						e.printStackTrace();
//						break;
						return null;
					}
				}
			}
			//如果被打断则使用trader再访问一次啊
//			TradeOrder to=this.trader.get(id);
//			return to;
		}
		
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

	/**
	 * @return the threadpool
	 */
	public ExecutorService getThreadpool() {
		return threadpool;
	}

	/**
	 * @param threadpool the threadpool to set
	 */
	public void setThreadpool(ExecutorService threadpool) {
		this.threadpool = threadpool;
	}

}
