/**
 * 
 */
package summ.btc.btclock;

import com.sun.org.apache.xml.internal.security.Init;

/**
 * @author wfeng007
 * @date 2016-2-13 下午04:04:30
 */
public interface MarketProbable {
		void init();
		void setKanban(Kanban kanban);
		
//		void loadMyOrders();
}
