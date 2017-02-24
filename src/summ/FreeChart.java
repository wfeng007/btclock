/**
 * 
 */
package summ;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;



/**
 * @author wfeng007
 */
public class FreeChart extends JFrame{
	 //constructor
	 public FreeChart(){
	         DefaultCategoryDataset DataSet = new DefaultCategoryDataset();
	         
//	         DataSet.addValue(300, "number", "apple");
//	         DataSet.addValue(400, "number", "barara");
	         DataSet.addValue(250, "1", "offer");
	         DataSet.addValue(250, "2", "offer");
	         DataSet.addValue(250, "3", "offer");
	         DataSet.addValue(420, "1", "bid");
	         DataSet.addValue(420, "2", "bid");
	         DataSet.addValue(420, "3", "bid");
//	         JFreeChart chart = ChartFactory.createBarChart3D("Catogram",
//	                 "Fruit", "Sale", DataSet, PlotOrientation.HORIZONTAL,
//	                 false, false, false);
	         JFreeChart chart = ChartFactory.createBarChart("Catogram",
             "Fruit", "Sale", DataSet, PlotOrientation.HORIZONTAL,
             true, true, true);
	         CategoryPlot plot = chart.getCategoryPlot();
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
             
             
             
//	         BarRenderer renderer = (BarRenderer) plot.getRenderer();
	         BarRenderer renderer = new BarRenderer();
//	         IntervalBarRenderer renderer = new IntervalBarRenderer();
//	         renderer.setDrawBarOutline(true);
	         renderer.setSeriesPaint(1, new Color(0, 100, 255)); 
	         renderer.setSeriesPaint(1, new Color(0, 0, 255));
	         renderer.setSeriesPaint(2, new Color(255, 0, 0));
	         renderer.setSeriesOutlinePaint(1, Color.BLACK); 
	         

	         renderer.setMaximumBarWidth(1d);
//	         renderer.setItemLabelGenerator(new StandardCategoryItemLabelGenerator()); 
	         renderer.setItemMargin(0.01d); 
//	         renderer.setBaseOutlinePaint(Color.BLACK);
	         
	         renderer.setIncludeBaseInRange(true);
	         renderer.setBaseItemLabelGenerator(
	                 new StandardCategoryItemLabelGenerator());
	         renderer.setBaseItemLabelsVisible(true);
	         plot.setRenderer(renderer);
//             chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,
//                     RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
//	         
	         chart.fireChartChanged(); //数据变化
	         
	         ChartPanel panel = new ChartPanel(chart,true);
	        JPanel jp = new JPanel();
	        
	        jp.add(panel, BorderLayout.CENTER);
	        this.add(jp);
	        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	        this.setBounds(100, 100, 700, 500);
	        this.setVisible(true);
	 }
	 public static void main(String [] args){
	      new FreeChart();
	 }
	}