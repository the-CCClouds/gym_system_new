package Ui;

import dao.StatisticsDAO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import utils.StyleUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ReportUi extends JFrame {

    private StatisticsDAO statsDAO;

    // 顶部统计标签
    private JLabel revenueLabel, memberLabel, orderLabel, stockLabel;

    // 中间容器 (CardLayout)
    private JPanel centerPanel;
    private CardLayout cardLayout;

    // JFreeChart 数据集 (用于动态更新数据)
    private DefaultCategoryDataset barDataset;
    private DefaultPieDataset pieDataset;

    // 视图组件
    private JScrollPane tableScroll;

    public ReportUi() {
        this.statsDAO = new StatisticsDAO();
        StyleUtils.initGlobalTheme();

        setTitle("📊 经营数据分析仪表盘 (JFreeChart版)");
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(new BorderLayout(15, 15));

        initTopCards();
        initCenterViews();
        initBottomToolbar();

        refreshData();
        setVisible(true);
    }

    // === 1. 顶部 4 个核心指标卡片 ===
    private void initTopCards() {
        JPanel topPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        revenueLabel = new JLabel("Loading...");
        topPanel.add(createCard("💰 总营收", revenueLabel, new Color(108, 92, 231)));

        memberLabel = new JLabel("Loading...");
        topPanel.add(createCard("👥 会员总数", memberLabel, new Color(0, 184, 148)));

        orderLabel = new JLabel("Loading...");
        topPanel.add(createCard("📝 今日订单", orderLabel, new Color(253, 203, 110)));

        stockLabel = new JLabel("Loading...");
        topPanel.add(createCard("📦 库存预警", stockLabel, new Color(214, 48, 49)));

        add(topPanel, BorderLayout.NORTH);
    }

    private JPanel createCard(String title, JLabel valueLabel, Color barColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JPanel bar = new JPanel();
        bar.setPreferredSize(new Dimension(5, 0));
        bar.setBackground(barColor);
        card.add(bar, BorderLayout.WEST);

        JPanel content = new JPanel(new GridLayout(2, 1));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(StyleUtils.FONT_NORMAL);
        tLbl.setForeground(StyleUtils.COLOR_INFO);

        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(StyleUtils.COLOR_TEXT_MAIN);

        content.add(tLbl);
        content.add(valueLabel);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    // === 2. 中间视图切换 (集成 JFreeChart) ===
    private void initCenterViews() {
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        centerPanel.setOpaque(false);

        // --- View 1: 表格 ---
        JTable table = new JTable(new DefaultTableModel(new String[]{"订单ID", "会员", "类型", "金额", "时间", "状态"}, 0));
        StyleUtils.styleTable(table);
        tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        centerPanel.add(tableScroll, "TABLE");

        // --- View 2: 柱状图 (JFreeChart) ---
        barDataset = new DefaultCategoryDataset();
        JFreeChart barChart = ChartFactory.createBarChart(
                "各业务营收对比", // 标题
                "业务类型",      // X轴标签
                "金额 (元)",     // Y轴标签
                barDataset,     // 数据集
                PlotOrientation.VERTICAL,
                false, true, false
        );
        styleBarChart(barChart); // 美化
        ChartPanel barPanel = new ChartPanel(barChart);
        barPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        centerPanel.add(barPanel, "BAR");

        // --- View 3: 饼状图 (JFreeChart) ---
        pieDataset = new DefaultPieDataset();
        JFreeChart pieChart = ChartFactory.createPieChart(
                "营收占比分析",  // 标题
                pieDataset,     // 数据集
                true, true, false
        );
        stylePieChart(pieChart); // 美化
        ChartPanel piePanel = new ChartPanel(pieChart);
        piePanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        centerPanel.add(piePanel, "PIE");

        add(centerPanel, BorderLayout.CENTER);
    }

    // === 3. 底部工具栏 ===
    private void initBottomToolbar() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton btnTable = createSwitchBtn("📋 详细报表", "TABLE", StyleUtils.COLOR_PRIMARY);
        JButton btnBar = createSwitchBtn("📊 营收柱状图", "BAR", new Color(255, 159, 67));
        JButton btnPie = createSwitchBtn("🍰 占比饼状图", "PIE", new Color(72, 219, 251));

        bottomPanel.add(btnTable);
        bottomPanel.add(btnBar);
        bottomPanel.add(btnPie);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JButton createSwitchBtn(String text, String cardName, Color color) {
        JButton btn = new JButton(text);
        StyleUtils.styleButton(btn, color);
        btn.setPreferredSize(new Dimension(160, 45));
        btn.addActionListener(e -> cardLayout.show(centerPanel, cardName));
        return btn;
    }

    // === 4. 数据加载与刷新 ===
    private void refreshData() {
        // 1. 刷新顶部卡片
        revenueLabel.setText("¥ " + String.format("%,.2f", statsDAO.getTotalRevenue()));
        memberLabel.setText(String.valueOf(statsDAO.getTotalMembers()));
        orderLabel.setText(String.valueOf(statsDAO.getTodayOrderCount()));
        stockLabel.setText(String.valueOf(statsDAO.getLowStockProductCount()));

        // 2. 刷新表格
        List<Map<String, Object>> orders = statsDAO.getRecentOrders();
        JTable table = (JTable) tableScroll.getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (Map<String, Object> o : orders) {
            model.addRow(new Object[]{
                    o.get("id"), o.get("name"), o.get("type"),
                    String.format("¥ %.2f", o.get("amount")),
                    o.get("time"), o.get("status")
            });
        }

        // 3. 刷新 JFreeChart 数据
        // 如果 StatisticsDAO 还没有 getRevenueByType 方法，请务必先添加！
        // 如果报错，说明你忘了更新 DAO
        try {
            Map<String, Double> data = statsDAO.getRevenueByType();

            // 更新柱状图数据
            barDataset.clear();
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                barDataset.setValue(entry.getValue(), "营收", entry.getKey());
            }

            // 更新饼状图数据
            pieDataset.clear();
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                pieDataset.setValue(entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            System.err.println("Warning: StatisticsDAO.getRevenueByType() not found or failed. Charts will be empty.");
        }
    }

    // ==================== JFreeChart 美化方法 ====================

    private void styleBarChart(JFreeChart chart) {
        // 1. 设置背景色为白色（去灰）
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(220, 220, 220)); // 网格线颜色
        plot.setOutlineVisible(false); // 去掉边框

        // 2. 柱子扁平化 (去掉默认的 3D 渐变光效)
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, new Color(108, 92, 231)); // 设置柱子颜色
        renderer.setDrawBarOutline(false);
        renderer.setShadowVisible(false);

        // 3. 字体优化 (防止中文乱码)
        Font font = new Font("微软雅黑", Font.PLAIN, 12);
        chart.getTitle().setFont(new Font("微软雅黑", Font.BOLD, 18));
        plot.getDomainAxis().setLabelFont(font);
        plot.getDomainAxis().setTickLabelFont(font);
        plot.getRangeAxis().setLabelFont(font);
        plot.getRangeAxis().setTickLabelFont(font);
    }

    private void stylePieChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);

        // 标签格式：名称 = 数值 (百分比)
        plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(
                "{0} = {1} ({2})",
                new java.text.DecimalFormat("0"),
                new java.text.DecimalFormat("0.00%")));

        // 字体
        chart.getTitle().setFont(new Font("微软雅黑", Font.BOLD, 18));
        plot.setLabelFont(new Font("微软雅黑", Font.PLAIN, 12));
        chart.getLegend().setItemFont(new Font("微软雅黑", Font.PLAIN, 12));
    }
}