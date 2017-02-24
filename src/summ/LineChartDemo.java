/**
 * 
 */
package summ;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

/**
 * @author wfeng007
 * @date 2017年2月24日 下午12:48:20
 */
public class LineChartDemo {

	/**
	 * 
	 * @param args
	 * 
	 * @throws IOException
	 * 
	 */

	public static void main(String[] args) throws IOException {

		// 生成折线图

		JFreeChart chart = ChartFactory.createLineChart(

				"深度", // 图表标题

				"价", // 目录轴的显示标签

				"深", // 数值轴的显示标签

				getDateSet(), // 数据

				// PlotOrientation.HORIZONTAL, //图表方向水平

				PlotOrientation.HORIZONTAL, // 图表方向垂直

				false, // 是否显示图例

				false, // 是否显示工具提示

				false // 是否生成URL

		);

		// 设置标题及标题字体
		chart.setTitle(new TextTitle("深度", new Font("黑体", Font.ITALIC, 22)));
		// 建一个图例
		// LegendTitle legendTitle = chart.getLegend(0);
		// 设置图例字体
		// legendTitle.setItemFont(new Font("宋体",Font.BOLD,14));
		// 获取折线图plot对象
//		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		// 设置折线的颜色
//		plot.getRenderer().setSeriesPaint(0, Color.blue);
		// 取得横轴
//		CategoryAxis categoryAxis = plot.getDomainAxis();
		// 设置横轴的字体
//		categoryAxis.setLabelFont(new Font("宋体", Font.BOLD, 22));
		// 设置分类标签以45度倾斜
		// categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		// 设置分类标签字体
//		categoryAxis.setTickLabelFont(new Font("宋体", Font.BOLD, 22));
		// 取得纵轴
//		NumberAxis numberAxis = (NumberAxis) plot.getRangeAxis();
		// 设置纵轴的字体
//		numberAxis.setLabelFont(new Font("宋体", Font.BOLD, 22));
		// 设置背景透明度（0~1）
//		plot.setBackgroundAlpha(0.9f);
		
		// 设置图表的子标题  
		chart.addSubtitle(new TextTitle("精度0.01"));  
        // 创建一个标题  
        TextTitle texttitle = new TextTitle("日期: " + new Date());  
        // 设置标题字体  
        texttitle.setFont(new Font("黑体", 0, 10));  
        // 设置标题向下对齐  
        texttitle.setPosition(RectangleEdge.BOTTOM);  
        // 设置标题向右对齐  
        texttitle.setHorizontalAlignment(HorizontalAlignment.RIGHT);  
        // 添加图表的子标题  
        chart.addSubtitle(texttitle);  
        
        // 设置图表的背景色为白色  
        chart.setBackgroundPaint(Color.white);  
        // 获得图表区域对象  
        CategoryPlot categoryplot = (CategoryPlot) chart.getPlot();  
        categoryplot.setBackgroundPaint(Color.lightGray);  
        categoryplot.setRangeGridlinesVisible(false);
        
        // 获显示线条对象  
        LineAndShapeRenderer lineandshaperenderer = (LineAndShapeRenderer) categoryplot  
                .getRenderer();  
        //
        lineandshaperenderer.setSeriesPaint(0, Color.RED);
        lineandshaperenderer.setSeriesPaint(1, Color.green);
        //
        lineandshaperenderer.setBaseShapesVisible(true);//折线拐点
        lineandshaperenderer.setDrawOutlines(true);  //拐点是否有边框
        lineandshaperenderer.setUseFillPaint(true);  //拐点是否填充
        lineandshaperenderer.setBaseFillPaint(Color.white);   //拐点填充颜色
        //根据不同分类来设置拐点填充颜色
        lineandshaperenderer.setSeriesFillPaint(0,Color.white);  
        lineandshaperenderer.setSeriesFillPaint(1, Color.BLACK);
//        lineandshaperenderer.setSeriesOutlinePaint(1, Color.green);
        
        // 设置折线加粗  
        lineandshaperenderer.setSeriesStroke(0, new BasicStroke(3F));  
        lineandshaperenderer.setSeriesOutlineStroke(0, new BasicStroke(2.0F));  
        lineandshaperenderer.setSeriesStroke(1, new BasicStroke(3F)); 
        // 设置折线拐点  series 表示不同分类系列的设置拐点样式
        lineandshaperenderer.setSeriesShape(0,  
                new java.awt.geom.Ellipse2D.Double(-5D, -5D, 10D, 10D));  
		
		

		// 输出文件
//		FileOutputStream fos = new FileOutputStream("book.jpg");
//		// 用ChartUtilities工具输出
//		ChartUtilities.writeChartAsJPEG(fos, chart, 800, 600);
//		fos.close();
		
		JFrame jf=new JFrame();
		

		
		ChartPanel panel = new ChartPanel(chart,true);
        // 设置chartPanel容器边框  
        CompoundBorder compoundBorder = BorderFactory.createCompoundBorder(  
                BorderFactory.createEmptyBorder(4, 4,4, 4),  
                BorderFactory.createEtchedBorder());  
		panel.setBorder(compoundBorder);  
		panel.setPreferredSize(new Dimension(200, 500)); //图表外部图盘尺寸	
		JPanel jp = new JPanel();
        jp.add(panel, BorderLayout.CENTER);
        
        
        jf.add(jp);
        jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jf.setBounds(100, 100, 700, 500);
        jf.setVisible(true);

	}

	private static CategoryDataset getDateSet() {

		// 提供生成折线图的数据

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (int i = 1; i <= 12; i++) {  
			dataset.addValue(getRandomData(), "bid", i + "");  
        }
		for (int i = 1; i <= 12; i++) { 
			dataset.addValue(getRandomData(), "ask", (i+12) + ""); 
		}
		return dataset;

	}
	
	private static Random random = new Random();  
    private static final int MAX_NUMBER = 100;  
	
    /** 
     *  随机在0到100间取数 
     * @return  
     */  
    public static int getRandomData() {  
        return random.nextInt(MAX_NUMBER);  
    }  

}
