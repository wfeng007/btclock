/**
 *
 */
package summ.btc.btclock;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.json.JSONObject;

import com.sun.org.apache.bcel.internal.generic.NEW;

//import summ.btc.btclock.BtcMarketProbe.DoMessage;
import summ.btc.CircularRotatingMap;
import summ.btc.btclock.data.TickRecord;
import summ.btc.btclock.data.TradeOrder;
import summ.btc.btclock.data.TradeRecord;

/**
 * @author wangfeng
 */
public class Kanban {

    //交易记录
    public CircularFifoQueue<TradeRecord> tradeRecords  = new CircularFifoQueue<TradeRecord>(10);
    //市场情况记录 （包括最基本的买一卖一信息） 打点记录
    public CircularFifoQueue<TickRecord> tickRecords = new CircularFifoQueue<TickRecord>(10);
    //当前买5卖5或市场深度
    public Depth                    nowDepth=new Depth();
    static public  class Depth{
    	public List<TradeOrder> bidList=new ArrayList<TradeOrder>();//0位置为买1
    	public List<TradeOrder> askList=new ArrayList<TradeOrder>();//0位置为卖1
//    	int size;
    	public Date timestamp;
    }
    //
    //
    //未成交的个人订单记录
    public Map<Long, TradeOrder>         openningCache = new ConcurrentHashMap<Long, TradeOrder>(); //HashMap
    //成交后的个人订单记录
    public CircularFifoQueue<TradeOrder> closedOrders  = new CircularFifoQueue<TradeOrder>(20);
    public CircularRotatingMap<Long, TradeOrder> closedOrderMap=new CircularRotatingMap<Long, TradeOrder>();
    
    //
//    public void offerClosedOrder(TradeOrder closedOrder){
//    	this.closedOrders.offer(closedOrder);
//		this.closedOrderMap.put(closedOrder.getId(), closedOrder);
//    }
    
    
//    public Map<Long,ReentrantLock> orderLockMap=new HashMap<Long,ReentrantLock>();

    //资金情况
    public void init() {
    }

    public void release() {
//        this.btcMarketProbe.shutdown();
        //clear
        tradeRecords.clear();
        tickRecords.clear();
        nowDepth = null;
    }

    public static void main(String[] args) {

    }
   

}
