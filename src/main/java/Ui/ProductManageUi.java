package Ui;

import entity.Product;
import service.ProductService;
import utils.StyleUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProductManageUi extends JFrame {

    private ProductService productService;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public ProductManageUi() {
        this.productService = new ProductService(); // 初始化 Service

        // 1. 应用全局主题
        StyleUtils.initGlobalTheme();

        setTitle("📦 商品库存管理中心");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(new BorderLayout(10, 10));

        initView();
        loadProductsToTable();
        setVisible(true);
    }

    private void initView() {
        // === 顶部功能栏 ===
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        add(topPanel, BorderLayout.NORTH);

        // 搜索区
        topPanel.add(new JLabel("📦 商品名称:"));
        searchField = new JTextField(15);
        StyleUtils.styleTextField(searchField);
        topPanel.add(searchField);

        JButton searchBtn = new JButton("查询");
        StyleUtils.styleButton(searchBtn, StyleUtils.COLOR_PRIMARY);
        searchBtn.addActionListener(e -> searchProduct());
        topPanel.add(searchBtn);

        JButton refreshBtn = new JButton("🔄 刷新");
        StyleUtils.styleButton(refreshBtn, StyleUtils.COLOR_INFO);
        refreshBtn.addActionListener(e -> loadProductsToTable());
        topPanel.add(refreshBtn);

        // 分隔线
        topPanel.add(new JSeparator(SwingConstants.VERTICAL));

        // CRUD 操作区
        JButton addBtn = new JButton("➕ 新增商品");
        StyleUtils.styleButton(addBtn, StyleUtils.COLOR_SUCCESS);
        addBtn.addActionListener(e -> openAddEditDialog(null)); // null 表示新增
        topPanel.add(addBtn);

        JButton editBtn = new JButton("✏️ 修改信息");
        StyleUtils.styleButton(editBtn, StyleUtils.COLOR_WARNING);
        editBtn.addActionListener(e -> editProduct());
        topPanel.add(editBtn);

        JButton delBtn = new JButton("🗑️ 下架/删除");
        StyleUtils.styleButton(delBtn, StyleUtils.COLOR_DANGER);
        delBtn.addActionListener(e -> deleteProduct());
        topPanel.add(delBtn);

        // === 中间表格区域 ===
        // 注意：这里移除了 Description 列，因为 Product 实体类中没有该字段
        String[] columns = {"ID", "商品名称", "单价 (¥)", "当前库存"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        productTable = new JTable(tableModel);
        StyleUtils.styleTable(productTable); // 应用美化样式

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 四周留白
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);
    }

    // ==================== 核心业务方法 (保持原名) ====================

    private void loadProductsToTable() {
        tableModel.setRowCount(0);
        List<Product> list = productService.getAllProducts();
        for (Product p : list) {
            tableModel.addRow(new Object[]{
                    p.getProductId(),
                    p.getName(),
                    p.getPrice(),
                    p.getStock()
            });
        }
    }

    private void searchProduct() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadProductsToTable();
            return;
        }

        tableModel.setRowCount(0);
        // 假设 Service 有 searchProducts 方法，如果没有请检查 ProductService
        List<Product> list = productService.searchProducts(keyword);
        for (Product p : list) {
            tableModel.addRow(new Object[]{
                    p.getProductId(), p.getName(), p.getPrice(), p.getStock()
            });
        }
    }

    private void addProduct() {
        // 为了更好的体验，我们将 add 和 edit 逻辑合并到了 openAddEditDialog
        openAddEditDialog(null);
    }

    private void editProduct() {
        int row = productTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要修改的商品！");
            return;
        }

        // 从表格获取当前选中行的数据
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        double price = (double) tableModel.getValueAt(row, 2);
        int stock = (int) tableModel.getValueAt(row, 3);

        // 构造一个临时的 Product 对象传给对话框
        Product p = new Product();
        p.setProductId(id);
        p.setName(name);
        p.setPrice(price);
        p.setStock(stock);

        openAddEditDialog(p);
    }

    private void deleteProduct() {
        int row = productTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的商品！");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要下架并删除商品 [" + name + "] 吗？\n(注意：这将永久删除该商品数据)",
                "确认删除", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (productService.deleteProduct(id)) {
                JOptionPane.showMessageDialog(this, "✅ 删除成功！");
                loadProductsToTable();
            } else {
                JOptionPane.showMessageDialog(this, "❌ 删除失败，可能存在关联订单数据。");
            }
        }
    }

    // ==================== 辅助：弹窗对话框 ====================

    private void openAddEditDialog(Product product) {
        boolean isEdit = (product != null);
        String title = isEdit ? "修改商品信息" : "新增商品入库";

        // 使用 JPanel 构造弹窗内容
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        JTextField nameF = new JTextField(isEdit ? product.getName() : "");
        JTextField priceF = new JTextField(isEdit ? String.valueOf(product.getPrice()) : "");
        JTextField stockF = new JTextField(isEdit ? String.valueOf(product.getStock()) : "");

        panel.add(new JLabel("商品名称:"));
        panel.add(nameF);
        panel.add(new JLabel("销售单价 (¥):"));
        panel.add(priceF);
        panel.add(new JLabel("库存数量:"));
        panel.add(stockF);

        int opt = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (opt == JOptionPane.OK_OPTION) {
            try {
                String name = nameF.getText().trim();
                double price = Double.parseDouble(priceF.getText().trim());
                int stock = Integer.parseInt(stockF.getText().trim());

                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "商品名称不能为空！");
                    return;
                }

                // 构造对象
                Product newP = isEdit ? product : new Product();
                newP.setName(name);
                newP.setPrice(price);
                newP.setStock(stock);

                boolean success;
                if (isEdit) {
                    success = productService.updateProduct(newP);
                } else {
                    success = productService.addProduct(newP);
                }

                if (success) {
                    JOptionPane.showMessageDialog(this, "✅ 操作成功！");
                    loadProductsToTable();
                } else {
                    JOptionPane.showMessageDialog(this, "❌ 操作失败，请重试。");
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "输入错误：价格和库存必须是数字！");
            }
        }
    }
}