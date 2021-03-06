/**
 *
 */
package summ.btc.btclock;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sun.org.apache.xpath.internal.FoundIndex;

import summ.btc.btclock.Kanban.Depth;
import summ.btc.btclock.data.TickRecord;
import summ.btc.btclock.data.TradeOrder;
import summ.btc.btclock.data.TradeRecord;
import summ.btc.btclock.okcoin.OkcoinMarketProbe;
import summ.btc.btclock.okcoin.OkcoinTrader;

/**
 * 界面版方式交易器
 * 人工交易器
 * @author wangfeng
 */
public class SimpleTradePane {
	static AbstractApplicationContext appContext;
	static JPanel topPanel;
	static JPanel middlePanel;
	static JPanel bottomPanel;

	static BidAskPanel bidAskPanel;
	static JTextField amountTf;
	static Tradable trader;
	static Kanban kanban;
	static MarketProbable marketProbe;
	static DefaultTableModel btmLeft;
	static DefaultTableModel btmRight;
	static DefaultTableModel lftModel;
	static DefaultTableModel rgtModel;
	static String tradeAmount = "0.096";
	//
	static DefaultCategoryDataset depthChartData ;
	static JFreeChart depthChart;

    //    static JPanel middlePanel;

    /**
     * 功能：
     * 键盘操作买卖。
     *
     * @author wangfeng
     */
    static class TradeKeyListener implements KeyListener {
        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
            //            System.out.println("---" + e);
            int keyCode = e.getKeyCode();
            switch (keyCode) {//判断键盘值
                case KeyEvent.VK_A://买入 buy 1
                    System.out.println("键盘操作：买入");
                    offerBuyOrder();
                    break;
                case KeyEvent.VK_S://买入sell 1
                    System.out.println("键盘操作：买入");
                    offerBuyOrderSell1();
                    break;
                case KeyEvent.VK_D://卖出 buy 1
                    System.out.println("键盘操作：卖出");
                    offerSellOrderBuy1();
                    break;
                case KeyEvent.VK_F://卖出 sell 1
                    System.out.println("键盘操作：卖出");
                    offerSellOrder();
                    break;
                case KeyEvent.VK_ESCAPE://撤单 
                    System.out.println("键盘操作：撤单");
                    cancelAllOpenningOrder();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
            //            System.out.println("---" + e);
        }
    }
    
    /**
     * 功能：
     * 焦点变换到交易面板
     *
     * @author wangfeng
     */
    static class ToFoucsTradePaeKeyListener implements KeyListener {
        @Override
        public void keyPressed(KeyEvent e) {
        }
        @Override
        public void keyReleased(KeyEvent e) {
            //            System.out.println("---" + e);
            int keyCode = e.getKeyCode();
            switch (keyCode) {//判断键盘值
                case KeyEvent.VK_ESCAPE://买入 buy 1
                    topPanel.requestFocus();
                    break;
                default:
                    break;
            }
        }
        @Override
        public void keyTyped(KeyEvent e) {
        }
    }
    
    /*
     * 下买单  buy1
     */
    static void offerBuyOrder() {
    	try{
    		Depth d = kanban.nowDepth;
			if (d.bidList.size() <= 0) {
				// Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(topPanel, "无参考价格用于下单。",
						"ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
				return;
			}
			TradeOrder to = d.bidList.get(0);
			System.out.println("买入价：" + to.getSubmitPrice() + " 买入量："
					+ amountTf.getText());
    		trader.buy(to.getSubmitPrice(), amountTf.getText());
    	}catch(RuntimeException re){
//            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(topPanel, re.getMessage(), "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
            re.printStackTrace();
        }
    }
    
    /*
     * 下买单 sell1
     */
    static void offerBuyOrderSell1() {
    	try{
    		Depth d = kanban.nowDepth;
			if (d.askList.size() <= 0) {
				// Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(topPanel, "无参考价格用于下单。",
						"ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
				return;
			}
			TradeOrder to = d.askList.get(0);
			System.out.println("买入价：" + to.getSubmitPrice() + " 买入量："
					+ amountTf.getText());
    		trader.buy(to.getSubmitPrice(), amountTf.getText());
    	}catch(RuntimeException re){
//            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(topPanel, re.getMessage(), "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
            re.printStackTrace();
        }
    }
    
    /*
     * 下卖单 buy1
     */
	static void offerSellOrderBuy1() {
		try {
    		Depth d = kanban.nowDepth;
			if (d.bidList.size() <= 0) {
				// Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(topPanel, "无参考价格用于下单。",
						"ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
				return;
			}
			TradeOrder to = d.bidList.get(0);
			System.out.println("卖出价：" + to.getSubmitPrice() + " 卖出量："
					+ amountTf.getText());
			trader.sell(to.getSubmitPrice(), amountTf.getText());
//			trader.sell("5000.00", amountTf.getText());
		} catch (RuntimeException re) {
			// Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(topPanel, re.getMessage(),
					"ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
			re.printStackTrace();
		}
	}

    /*
     * 下卖单 sell1
     */
	static void offerSellOrder() {
		try {
			Depth d = kanban.nowDepth;
			if (d.askList.size() <= 0) {
				// Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(topPanel, "无参考价格用于下单。",
						"ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
				return;
			}
			TradeOrder to = d.askList.get(0);
			System.out.println("卖出价：" + to.getSubmitPrice() + " 卖出量："
					+ amountTf.getText());
			trader.sell(to.getSubmitPrice(), amountTf.getText());
//			trader.sell("5000.00", amountTf.getText());
		} catch (RuntimeException re) {
			// Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(topPanel, re.getMessage(),
					"ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
			re.printStackTrace();
		}
	}


    
    static void cancelAllOpenningOrder() {
    	try{
    		Set<Long> ks=kanban.openningCache.keySet(); //这里使用的是本地缓存中的数据，而不是远程服务器上的。
    		for (Long id : ks) {
    			if(id!=null)trader.cancel(id);
			}
    	}catch(Exception re){
//           JOptionPane.showMessageDialog(topPanel, re.getMessage(), "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
            re.printStackTrace();
        }
    }

    //    /*
    //     * 获取指定订单数据并放入table列表
    //     */
    //    static void addOrderData(String orderId) {
    //
    //        JSONObject orderJs;
    //        for (;;) {
    //            TradeData td = new TradeData();
    //            td.req.id = 2;
    //            td.req.method = "getOrder";
    //            td.req.params.add(Long.valueOf(orderId));
    //            trader.trade(td);
    //            System.out.println(td.resp.resultStr);
    //            String status = td.resp.result.optJSONObject("order").optString("status");
    //            String avg_price = td.resp.result.optJSONObject("order").optString("avg_price");
    //            if ("closed".equalsIgnoreCase(status) && !"0".equalsIgnoreCase(avg_price)) {
    //                orderJs = td.resp.result.optJSONObject("order");
    //                System.out.println("交易成功:" + orderJs);
    //                break;//直到交易成功恢复
    //            } else {
    //                System.out.println("交易状态：" + status + " avg_price:" + avg_price);
    //                try {
    //                    Thread.sleep(10);
    //                } catch (InterruptedException e) {
    //                    e.printStackTrace();
    //                }
    //            }
    //
    //        }
    //        //解析订单结果
    //        //        orderJs = td.resp.result.optJSONObject("order");
    //        /*
    //         * order内部 {"id":232477826,"type"
    //         * :"ask","price":"0.96","avg_price":"2580.27",
    //         * "currency":"CNY","amount"
    //         * :"0.00000000","amount_original":"0.00100000"
    //         * ,"date":1453620039,"status":"closed"}
    //         */
    //        String[] rowData = { orderJs.optString("id"), orderJs.optString("type"), orderJs.optString("price"),
    //                orderJs.optString("avg_price"), orderJs.optString("amount"), orderJs.optString("amount_original"),
    //                orderJs.optString("date"), orderJs.optString("status") };
    //        tableModel.addRow(rowData);
    //    }

    //买一卖一 面板
    static class BidAskPanel extends JPanel {
        public JLabel ask1PriceLb  = new JLabel("");
        public JLabel ask1AmountLb = new JLabel("");
        public JLabel bid1PriceLb  = new JLabel("");
        public JLabel bid1AmountLb = new JLabel("");

        public void setBidAsk(String ask1Price, String ask1Amount, String bid1Price, String bid1Amount) {
            ask1PriceLb.setText(ask1Price);
            ask1AmountLb.setText(ask1Amount);
            bid1PriceLb.setText(bid1Price);
            bid1AmountLb.setText(bid1Amount);
        }

        public void init() {
            this.setBorder(new TitledBorder("“买一卖一”面板"));
            GridLayout gl = new GridLayout(2, 2);
            gl.setHgap(0); //列间距
            gl.setVgap(0); //行间距
            this.setLayout(gl);

            List<JLabel> settingLs = new ArrayList<JLabel>();
            settingLs.add(ask1PriceLb);
            settingLs.add(ask1AmountLb);
            settingLs.add(bid1PriceLb);
            settingLs.add(bid1AmountLb);

            for (JLabel jL : settingLs) {
                jL.setPreferredSize(new Dimension(80, 20));
//                jL.setHorizontalAlignment(JLabel.CENTER);
                jL.setVerticalAlignment(JLabel.CENTER);
                jL.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                this.add(jL);
            }
            ask1PriceLb.setHorizontalAlignment(JLabel.RIGHT);
            ask1AmountLb.setHorizontalAlignment(JLabel.LEFT);
            bid1PriceLb.setHorizontalAlignment(JLabel.RIGHT);
            bid1AmountLb.setHorizontalAlignment(JLabel.LEFT);
            //            this.setVisible(true);
        }
    }

    static Thread screenFreshWorder = new Thread(new ScreenFreshRunner());

    static class ScreenFreshRunner implements Runnable {

        /*
         * (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            for (;;) {
            	try{
	                //刷新 买一 卖一
	            	Depth nowDe = kanban.nowDepth;
	                if (nowDe != null && nowDe.askList.size()>0 && nowDe.bidList.size()>0) {
	                    // System.out.println(nowGo);
	                	
	                	TradeOrder ask1= nowDe.askList.get(0);
	                	TradeOrder bid1= nowDe.bidList.get(0);
	                    SimpleTradePane.bidAskPanel.setBidAsk(ask1.getSubmitPrice(), ask1.getOrigAmount(),
	                            bid1.getSubmitPrice(), bid1.getOrigAmount());
	                }
	                //TODO　刷新市场深度tob
	                List<TradeOrder> askLs=nowDe.askList; 
//	                depthChartData=new DefaultCategoryDataset();
	                depthChartData.clear();
	                for (int i = 14<askLs.size()?14:askLs.size(); i >= 0; i--) {
	                	TradeOrder askOdr=askLs.get(i);
	                	String amo=askOdr.getOrigAmount();
//	                	new Integer(amo);
	                	depthChartData.addValue(Double.valueOf(amo), "ask(offer)" ,  askOdr.getSubmitPrice());
					}
	                List<TradeOrder> bidLs=nowDe.bidList;
	                for (int i = 0; i<15;  i++) {
	                	TradeOrder askOdr=bidLs.get(i);
	                	String amo=askOdr.getOrigAmount();
//	                	new Integer(amo);
	                	depthChartData.addValue(Double.valueOf(amo), "bid" ,  askOdr.getSubmitPrice());
					}
	                depthChart.fireChartChanged();//刷新展示
	
	                //刷新 实时交易数据
	//           // "trade_id", "price", "amount", "type", "date"
	                while (lftModel.getRowCount() > 0) {
	                    lftModel.removeRow(lftModel.getRowCount() - 1);
	                }
	                for (TradeRecord trade : kanban.tradeRecords) {
	                    String[] rowData = { ""+trade.getId(), trade.getStrikePrice(),
	                    		trade.getAmount(), ""+trade.getTradeType(),getTsStr(trade.getTradeTs()) };
	                    lftModel.addRow(rowData);
	                }
//	                for (int i = 0; i < kanban.tradeRecords.size(); i++) {
//	                	TradeRecord trade=kanban.tradeRecords.get(i);
//	                	lftModel.setValueAt(trade.getId(), i, 0);
//					}
	                
	
	                //
	                // { "id", "type", "price", "amount", "amount_original", "date", "status" };
	                //刷新open中的订单数据
	                while (rgtModel.getRowCount() > 0) {
	                    rgtModel.removeRow(rgtModel.getRowCount() - 1);
	                }
	                List<TradeOrder> openningLs = new ArrayList<TradeOrder>();
	                openningLs.addAll(kanban.openningCache.values());//
	                for (TradeOrder to : openningLs) {
	//                	System.out.println(FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss").format(to.getCreatedTs()));
	                  String[] rowData = { ""+to.getId(), ""+to.getTradeType(),
	                  to.getSubmitPrice(), to.getNowAmount(),
	                  to.getOrigAmount(),getTsStr(to.getCreatedTs()),
	                  ""+to.getStatus() };
	                    rgtModel.addRow(rowData);
	                }
	                
	                //
	                //解析订单结果
	                //{ "id", "type", "price", "avg_price", "amount", "amount_original", "date", "status" }
	                while (btmLeft.getRowCount() > 0) {
	                    btmLeft.removeRow(btmLeft.getRowCount() - 1);
	                }
	                for (TradeOrder to : kanban.closedOrders) {  //FIXME 重连后，kanban.closedOrders,kanban.openningCache 都没有数据更新了。 貌似是probe在重连时ok_cny_realtrades不连接的[{"channel":"ok_cny_realtrades","success":"true"}]
	//                	System.out.println(FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(to.getCreatedTs()));
	                    String[] rowData = { ""+to.getId(), ""+to.getTradeType(),
	                            to.getSubmitPrice(),to.getStrikePrice(), to.getNowAmount(),
	                            to.getOrigAmount(),getTsStr(to.getCreatedTs()),
	                            ""+to.getStatus() };
	                    btmLeft.addRow(rowData);
	                }
            	}catch (Exception e) {
					e.printStackTrace();
				}
            	//
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            	
            }

        }
    }
    
    static private String getTsStr(Date ts){
    	if(ts==null){
    		return null;
    	}
    	return FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(ts);
    }

    static void initTopPane() {
        topPanel = new JPanel();

        //交易按钮面板
        bidAskPanel = new BidAskPanel();
        //交易按钮
        JButton buyButton = new JButton("买入");//bid
        JButton sellButton = new JButton("卖出");//ask
        //不让按钮取得焦点
        buyButton.setFocusable(false);
        sellButton.setFocusable(false);
        //        buyButton.setEnabled(false);
        //按钮功能：
        buyButton.addActionListener(new ActionListener() {//市价买入行为
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        offerBuyOrder();
                    }
                });
        sellButton.addActionListener(new ActionListener() {//市价卖出行为
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        offerSellOrder();
                    }
                });
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS)); //水平布局
        buttonPanel.add(buyButton);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(sellButton);
        buttonPanel.setVisible(true);
        buttonPanel.setBorder(BorderFactory.createLineBorder(Color.red, 3));//红边框
        //        buttonPanel.setForeground(Color.GREEN);

        //买一卖一面板
        bidAskPanel.init();
        
        //深度面板 及数据体
        depthChartData = new DefaultCategoryDataset();
        depthChart = ChartFactory.createLineChart(null,
        null, null, depthChartData, PlotOrientation.HORIZONTAL,
        false, false, false);
//        depthChart.setTitle(new TextTitle("深度", new Font("黑体", Font.ITALIC, 22)));
		// 设置图表的子标题  
//        depthChart.addSubtitle(new TextTitle("精度0.01"));  
//        TextTitle texttitle = new TextTitle("日期: " + new Date());  
//        texttitle.setFont(new Font("黑体", 0, 10));  
//        texttitle.setPosition(RectangleEdge.BOTTOM);  
//        // 设置标题向右对齐  
//        texttitle.setHorizontalAlignment(HorizontalAlignment.RIGHT);  
//        // 添加图表的子标题  
//        depthChart.addSubtitle(texttitle); 
        
        // 设置图表的背景色为白色  
        depthChart.setBackgroundPaint(Color.white);  
        
        CategoryPlot plot = depthChart.getCategoryPlot();
        // 2,设置详细图表的显示细节部分的背景颜色
        plot.setBackgroundPaint(Color.PINK);
        // 3,设置垂直网格线颜色
        plot.setDomainGridlinePaint(Color.black);
        //4,设置是否显示垂直网格线
        plot.setDomainGridlinesVisible(true);
        //5,设置水平网格线颜色
        plot.setRangeGridlinePaint(Color.black);
        //6,设置是否显示水平网格线
        plot.setRangeGridlinesVisible(false);
        
       
        // 获显示线条对象  设置
        LineAndShapeRenderer lineandshaperenderer = (LineAndShapeRenderer) plot  
                .getRenderer();  
        //
        lineandshaperenderer.setSeriesPaint(0, Color.RED);
        lineandshaperenderer.setSeriesPaint(1, Color.green);
        //
//        lineandshaperenderer.setBaseShapesVisible(true);//折线拐点
//        lineandshaperenderer.setDrawOutlines(true);  //拐点是否有边框
//        lineandshaperenderer.setUseFillPaint(true);  //拐点是否填充
        lineandshaperenderer.setBaseFillPaint(Color.white);   //拐点填充颜色
        // 设置折线加粗  
        lineandshaperenderer.setSeriesStroke(0, new BasicStroke(2F));  
//        lineandshaperenderer.setSeriesOutlineStroke(0, new BasicStroke(2.0F));  
        lineandshaperenderer.setSeriesStroke(1, new BasicStroke(2F)); 
        // 设置折线拐点  series 表示不同分类系列的设置拐点样式
//        lineandshaperenderer.setSeriesShape(0,  
//                new java.awt.geom.Ellipse2D.Double(-5D, -5D, 5D, 5D));
//        lineandshaperenderer.setSeriesShape(1,  
//                new java.awt.geom.Ellipse2D.Double(-5D, -5D, 5D, 5D));  
        
//        plot.setRenderer(renderer); 
        
//        chart.fireChartChanged(); //数据变化刷新展示
        //绘制面板
        ChartPanel cpanel = new ChartPanel(depthChart,true);
        cpanel.setPreferredSize(new Dimension(200, 350));

        //放入top面板
        topPanel.add(Box.createVerticalStrut(10));//10px高空白
        topPanel.add(buttonPanel);
        topPanel.add(Box.createVerticalStrut(10));//10px高空白
        topPanel.add(bidAskPanel);
        topPanel.add(Box.createVerticalStrut(10));//10px高空白

        
        //交易btc量输入框
        amountTf=new JTextField();
        amountTf.setPreferredSize(new Dimension(100, 30));
        amountTf.setText(tradeAmount);
        amountTf.addKeyListener(new ToFoucsTradePaeKeyListener());
        topPanel.add(amountTf);
        //        topPanel.add
        //        topPanel.setBorder(BorderFactory.createLineBorder(Color.green, 3));
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "TOP操作区域");
        border.setTitlePosition(TitledBorder.DEFAULT_POSITION);
        border.setTitleJustification(TitledBorder.RIGHT);
        topPanel.setBorder(border);
        topPanel.setFocusable(true);//可接受焦点，才能触发键盘事件
        //        topPanel.requestFocus();
        //        topPanel.enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        //        topPanel.enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
        //        topPanel.enableEvents(AWTEvent.KEY_EVENT_MASK);
        
        
        topPanel.addFocusListener(new FocusListener() { //交易面板绿色时表示可通过按钮进行操作（有焦点）
			@Override
			public void focusLost(FocusEvent e) {
				Component c=e.getComponent();
				c.setBackground(Color.DARK_GRAY);
			}
			@Override
			public void focusGained(FocusEvent e) {
				Component c=e.getComponent();
				
				c.setBackground(Color.green);
			}
		});
        topPanel.addKeyListener(new TradeKeyListener());//
        
        
        
        //tob depth
        topPanel.add(Box.createVerticalStrut(10));//10px高空白
        topPanel.add(cpanel);//chart面板放入主面板
        
    }

    static void initMiddlePane() {
        middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.X_AXIS));
        //{"amount":1.2469,"market":"btccny","price":2540,"trade_id":54712633,"date":1454827558,"type":"buy"}
        String[] lftCn = { "trade_id", "price", "amount", "type", "date"};
        lftModel = new DefaultTableModel(null, lftCn);
        JTable lftTable = new JTable(lftModel);
        
        lftTable.setEnabled(false);
        lftTable.setFocusable(false);
        //        RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(lftModel);
        //        lftTable.setRowSorter(sorter);
        JScrollPane lftScrollPane = new JScrollPane(lftTable);
        lftScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //

        //{"amount":0.01,"id":252983867,"price":2535.37,"market":"btccny","status":"open","date":1454987281,"type":"ask","amount_original":0.01}
        String[] rgtCn = { "id", "type", "price", "amount", "amount_original", "date", "status" };
        rgtModel = new DefaultTableModel(null, rgtCn);
        JTable rgtTable = new JTable(rgtModel);
        rgtTable.setEnabled(false);
        rgtTable.setFocusable(false);
        JScrollPane rgtScrollPane = new JScrollPane(rgtTable);
        rgtScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //
        //        middlePanel.add(Box.createVerticalStrut(10));
        middlePanel.add(lftScrollPane);
        //        middlePanel.add(Box.createVerticalStrut(10));
        middlePanel.add(rgtScrollPane);
        //        middlePanel.add(Box.createVerticalStrut(10));
    }
    
    

    static void initBottomPane() {
        bottomPanel = new JPanel();
//        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS)); //上下排列
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));//左右排列
        //
        String[] columnName = { "id", "type", "price", "avg_price", "amount", "amount_original", "date", "status" };//
        btmLeft = new DefaultTableModel(null, columnName);
        JTable btmLeftTable = new JTable(btmLeft);
        btmLeftTable.setEnabled(false);
        btmLeftTable.setFocusable(false);
        JScrollPane btmLeftPanel = new JScrollPane(btmLeftTable);
        btmLeftPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      
        
        //
        String[] rightcolumnName = { "innings id","type", "bid avg_price", "bid amount", "ask avg_price", "ask amount", "profit","status",  };//
        btmRight = new DefaultTableModel(null, rightcolumnName);
        JTable btmRightTable = new JTable(btmRight);
        btmRightTable.setEnabled(false);
        btmRightTable.setFocusable(false);
        JScrollPane btmRightPanel = new JScrollPane(btmRightTable);
        btmRightPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//
//        bottomPanel.add(Box.createVerticalStrut(10));//垂直间隔
        bottomPanel.add(btmLeftPanel);
//        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(btmRightPanel);
    }

    public static void main(String[] args) {
    	appContext=new ClassPathXmlApplicationContext("applicationContext-beans.xml");
//        kanban = new Kanban();
//        kanban.init();
////        bmp=new BtcMarketProbe(); //btc okcoin
//        marketProbe=new OkcoinMarketProbe();
//        marketProbe.setKanban(kanban);
//        marketProbe.init();
////        trader = new BtcTrader();//btc okcoin
//        trader = new OkcoinTrader();
    	
		kanban = (Kanban)appContext.getBean("kanban");
		marketProbe = (OkcoinMarketProbe)appContext.getBean("okcoinMarketProbe");
		trader = (OkcoinTrader)appContext.getBean("okcoinTrader");
    	

        initTopPane();
        initMiddlePane();
        initBottomPane();

        // 主面板
        JPanel panelContainer = new JPanel();

        // 放置上方面板
        panelContainer.setLayout(new GridBagLayout()); //网格布局
        GridBagConstraints c1 = new GridBagConstraints();
        c1.gridx = 0;
        c1.gridy = 0;
        c1.weightx = 1.0;
        c1.weighty = 1.0;
        c1.fill = GridBagConstraints.BOTH;//内部组件水平及垂直方向适应边框变化
        panelContainer.add(topPanel, c1);

        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 0; //列
        c2.gridy = 1; //行
        c2.weightx = 1.0; //根据单元格变化
        c2.weighty = 1.0; //
        c2.fill = GridBagConstraints.BOTH; //内部组件水平方向适应边框变化
        panelContainer.add(middlePanel, c2);

        GridBagConstraints c3 = new GridBagConstraints();
        c3.gridx = 0; //列
        c3.gridy = 2; //行
        c3.weightx = 1.0; //根据单元格变化
        c3.weighty = 1.0; //
        c3.fill = GridBagConstraints.BOTH; //内部组件水平方向适应边框变化
        panelContainer.add(bottomPanel, c3);

        // 主窗口
		JFrame frame = new JFrame("BTC交易面板");
		WindowListener wndCloser = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				appContext.registerShutdownHook();
				e.getWindow().setVisible(false);
//				try {
//					Thread.sleep(5000);
//				} catch (InterruptedException e1) {
//					e1.printStackTrace();
//				}
				System.out.println("to exit");
				System.exit(0);
			}
		};
		frame.addWindowListener(wndCloser);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panelContainer.setOpaque(true);
        frame.setSize(new Dimension(720, 640));
        frame.setContentPane(panelContainer);
        frame.setVisible(true);

        //刷新内容
        screenFreshWorder.start();
    }
}
