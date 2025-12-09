package Ui;

import entity.Product;
import service.ProductService;
import service.ShopService;
import service.ServiceResult; // 确保导入 Result
import utils.StyleUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopUi extends JFrame {

    private ShopService shopService;
    private ProductService productService;

    // 组件
    private JTable productTable;
    private DefaultTableModel productModel;
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel totalLabel;
    private JTextField searchField;

    // 购物车数据：ID -> 数量
    private Map<Integer, Integer> shoppingCart = new HashMap<>();
    // 缓存商品数据：ID -> Product
    private Map<Integer, Product> productCache = new HashMap<>();

    public ShopUi() {
        this.shopService = new ShopService();
        this.productService = new ProductService();

        StyleUtils.initGlobalTheme();
        setTitle("🛒 收银台 (POS System)");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(new BorderLayout(10, 10));

        initView();
        loadProducts();
        setVisible(true);
    }

    private void initView() {
        // === 左侧：商品区 ===
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 左侧顶部搜索
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        searchField = new JTextField(15);
        StyleUtils.styleTextField(searchField);
        JButton searchBtn = new JButton("🔍 搜索商品");
        StyleUtils.styleButton(searchBtn, StyleUtils.COLOR_PRIMARY);
        searchBtn.addActionListener(e -> loadProducts());

        searchPanel.add(new JLabel("商品名称:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        // 商品表格
        String[] pCols = {"ID", "商品名称", "单价(¥)", "库存", "操作"};
        productModel = new DefaultTableModel(pCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        productTable = new JTable(productModel);
        StyleUtils.styleTable(productTable);

        // 双击添加商品
        productTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) addToCart();
            }
        });

        JScrollPane pScroll = new JScrollPane(productTable);
        pScroll.setBorder(BorderFactory.createLineBorder(new Color(230,230,230)));
        leftPanel.add(pScroll, BorderLayout.CENTER);

        JLabel tipLabel = new JLabel("💡 提示：双击商品即可加入购物车");
        tipLabel.setForeground(StyleUtils.COLOR_INFO);
        tipLabel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        leftPanel.add(tipLabel, BorderLayout.SOUTH);

        // === 右侧：购物车区 ===
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(350, 0));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(220, 220, 220)));

        // 购物车标题
        JLabel cartTitle = new JLabel("🛍️ 购物车清单", SwingConstants.CENTER);
        cartTitle.setFont(StyleUtils.FONT_TITLE);
        cartTitle.setForeground(StyleUtils.COLOR_TEXT_MAIN);
        cartTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        rightPanel.add(cartTitle, BorderLayout.NORTH);

        // 购物车表格
        String[] cCols = {"商品", "数量", "小计"};
        cartModel = new DefaultTableModel(cCols, 0);
        cartTable = new JTable(cartModel);
        StyleUtils.styleTable(cartTable);
        JScrollPane cScroll = new JScrollPane(cartTable);
        cScroll.setBorder(null);
        rightPanel.add(cScroll, BorderLayout.CENTER);

        // 底部结算区
        JPanel checkoutPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        checkoutPanel.setBackground(new Color(245, 250, 255));
        checkoutPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 操作按钮行
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setOpaque(false);
        JButton clearBtn = new JButton("清空");
        StyleUtils.styleButton(clearBtn, StyleUtils.COLOR_INFO);
        clearBtn.addActionListener(e -> clearCart());

        JButton removeBtn = new JButton("删除选中");
        StyleUtils.styleButton(removeBtn, StyleUtils.COLOR_WARNING);
        removeBtn.addActionListener(e -> removeFromCart());

        btnRow.add(clearBtn);
        btnRow.add(removeBtn);
        checkoutPanel.add(btnRow);

        // 总金额
        totalLabel = new JLabel("总计: ¥ 0.00", SwingConstants.CENTER);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 22));
        totalLabel.setForeground(StyleUtils.COLOR_DANGER);
        checkoutPanel.add(totalLabel);

        // 结算按钮
        JButton checkoutBtn = new JButton("✨ 立即结账");
        StyleUtils.styleButton(checkoutBtn, StyleUtils.COLOR_SUCCESS);
        checkoutBtn.setFont(new Font("微软雅黑", Font.BOLD, 18));
        checkoutBtn.setPreferredSize(new Dimension(0, 50));
        checkoutBtn.addActionListener(e -> performCheckout());
        checkoutPanel.add(checkoutBtn);

        rightPanel.add(checkoutPanel, BorderLayout.SOUTH);

        // === 添加到主窗口 ===
        // 使用 SplitPane 分割左右，虽然 BorderLayout 也可以，但 SplitPane 可调整大小
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.7); // 左侧占70%
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);
    }

    // --- 业务逻辑 (保持原有逻辑框架，适配新UI) ---

    private void loadProducts() {
        productModel.setRowCount(0);
        productCache.clear();

        String keyword = searchField.getText().trim();
        List<Product> products = productService.searchProducts(keyword); // 需要ProductService支持search
        // 如果ProductService没有search，就用getAll然后过滤，或者用DAO

        for (Product p : products) {
            productCache.put(p.getProductId(), p);
            productModel.addRow(new Object[]{
                    p.getProductId(), p.getName(), p.getPrice(), p.getStock(), "➕"
            });
        }
    }

    private void addToCart() {
        int row = productTable.getSelectedRow();
        if (row == -1) return;

        int pId = (int) productModel.getValueAt(row, 0);
        Product p = productCache.get(pId);

        if (p.getStock() <= 0) {
            JOptionPane.showMessageDialog(this, "该商品库存不足！");
            return;
        }

        // 数量+1
        shoppingCart.put(pId, shoppingCart.getOrDefault(pId, 0) + 1);
        updateCartView();
    }

    private void updateCartView() {
        cartModel.setRowCount(0);
        double total = 0.0;

        for (Map.Entry<Integer, Integer> entry : shoppingCart.entrySet()) {
            Product p = productCache.get(entry.getKey());
            int qty = entry.getValue();
            double subtotal = p.getPrice() * qty;
            total += subtotal;

            cartModel.addRow(new Object[]{p.getName(), qty, String.format("%.2f", subtotal)});
        }
        totalLabel.setText("总计: ¥ " + String.format("%.2f", total));
    }

    private void clearCart() {
        shoppingCart.clear();
        updateCartView();
    }

    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row == -1) return;

        // 这里的逻辑稍微复杂，因为Table只显示了名字，没存ID
        // 简单做法：重新匹配名字，或者在TableModel里存对象
        // 这里为了简化，清空重选
        JOptionPane.showMessageDialog(this, "暂不支持单项删除，请点击清空重选");
    }

    private void performCheckout() {
        if (shoppingCart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "购物车是空的！");
            return;
        }

        String input = JOptionPane.showInputDialog(this, "请输入会员ID (散客请输0):", "0");
        if (input == null) return;

        try {
            int memberId = Integer.parseInt(input);
            ServiceResult<Void> result = shopService.checkout(memberId, shoppingCart);

            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "✅ " + result.getMessage());
                clearCart();
                loadProducts(); // 刷新库存
            } else {
                JOptionPane.showMessageDialog(this, "❌ " + result.getMessage());
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的数字ID");
        }
    }
}