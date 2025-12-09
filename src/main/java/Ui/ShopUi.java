package Ui;

import entity.Member;
import entity.Product;
import service.MemberService;
import service.ShopService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopUi extends JFrame {

    private ShopService shopService;
    private MemberService memberService;

    // 数据状态
    private Member currentMember = null; // 当前选中的会员，null表示散客
    private Map<Integer, Integer> shoppingCart = new HashMap<>(); // 购物车 <商品ID, 数量>
    private List<Product> allProducts; // 商品列表缓存

    // UI组件
    private JTextField memberSearchField;
    private JLabel memberInfoLabel;
    private JTable productTable;
    private JTable cartTable;
    private JLabel totalLabel;
    private DefaultTableModel cartModel;

    public ShopUi() {
        // 初始化服务层
        this.shopService = new ShopService();
        this.memberService = new MemberService();

        // 窗口设置
        this.setTitle("前台收银系统 (POS) - 商品售卖");
        this.setSize(1100, 700);
        this.setLocationRelativeTo(null); // 居中
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); // 关闭时只销毁当前窗口
        this.getContentPane().setLayout(null);

        initView();     // 初始化界面
        loadProducts(); // 加载商品数据

        this.setVisible(true);
    }

    private void initView() {
        // =================================================================
        // 区域 1：顶部会员栏 (Member Section)
        // =================================================================
        JPanel topPanel = new JPanel(null);
        topPanel.setBounds(10, 10, 1060, 70);
        topPanel.setBorder(BorderFactory.createTitledBorder("第一步：识别客户身份"));
        this.add(topPanel);

        JLabel searchLabel = new JLabel("会员手机/姓名:");
        searchLabel.setBounds(20, 30, 100, 25);
        topPanel.add(searchLabel);

        memberSearchField = new JTextField();
        memberSearchField.setBounds(120, 30, 200, 25);
        // 回车触发搜索
        memberSearchField.addActionListener(e -> searchMember());
        topPanel.add(memberSearchField);

        JButton searchBtn = new JButton("查询会员");
        searchBtn.setBounds(330, 30, 100, 25);
        searchBtn.addActionListener(e -> searchMember());
        topPanel.add(searchBtn);

        JButton guestBtn = new JButton("重置为散客");
        guestBtn.setBounds(440, 30, 100, 25);
        guestBtn.addActionListener(e -> resetToGuest());
        topPanel.add(guestBtn);

        memberInfoLabel = new JLabel("当前状态：[ 散客模式 ] (无关联会员)");
        memberInfoLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        memberInfoLabel.setForeground(new Color(255, 100, 0)); // 醒目的颜色
        memberInfoLabel.setBounds(600, 30, 450, 25);
        topPanel.add(memberInfoLabel);

        // =================================================================
        // 区域 2：左侧商品列表 (Product List)
        // =================================================================
        JLabel prodTitle = new JLabel("第二步：选择商品 (双击加入购物车)");
        prodTitle.setFont(new Font("微软雅黑", Font.BOLD, 14));
        prodTitle.setBounds(20, 90, 400, 20);
        this.add(prodTitle);

        // 表格列：ID, 名称, 单价, 库存
        String[] prodCols = {"ID", "商品名称", "单价 (¥)", "剩余库存"};
        DefaultTableModel prodModel = new DefaultTableModel(prodCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } // 禁止编辑
        };

        productTable = new JTable(prodModel);
        productTable.setRowHeight(30);
        productTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 双击事件监听
        productTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) { // 双击
                    addToCart();
                }
            }
        });

        JScrollPane prodScroll = new JScrollPane(productTable);
        prodScroll.setBounds(20, 120, 500, 520);
        this.add(prodScroll);

        // 刷新商品按钮
        JButton refreshBtn = new JButton("刷新库存");
        refreshBtn.setBounds(400, 85, 120, 30);
        refreshBtn.addActionListener(e -> loadProducts());
        this.add(refreshBtn);

        // =================================================================
        // 区域 3：右侧购物车 (Shopping Cart)
        // =================================================================
        JLabel cartTitle = new JLabel("第三步：购物车结算");
        cartTitle.setFont(new Font("微软雅黑", Font.BOLD, 14));
        cartTitle.setBounds(550, 90, 300, 20);
        this.add(cartTitle);

        // 表格列：ID, 名称, 单价, 数量, 小计
        String[] cartCols = {"ID", "商品名称", "单价", "数量", "小计 (¥)"};
        cartModel = new DefaultTableModel(cartCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(30);
        cartTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setBounds(550, 120, 500, 400);
        this.add(cartScroll);

        // --- 购物车控制按钮 ---
        JButton removeBtn = new JButton("删除选中项");
        removeBtn.setBounds(550, 530, 120, 35);
        removeBtn.addActionListener(e -> removeFromCart());
        this.add(removeBtn);

        JButton clearBtn = new JButton("清空购物车");
        clearBtn.setBounds(680, 530, 120, 35);
        clearBtn.addActionListener(e -> clearCart());
        this.add(clearBtn);

        // --- 金额统计 ---
        totalLabel = new JLabel("总计: ¥0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 36));
        totalLabel.setForeground(Color.RED);
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        totalLabel.setBounds(700, 530, 350, 50);
        this.add(totalLabel);

        // --- 结账大按钮 ---
        JButton checkoutBtn = new JButton("确认收款 & 出库");
        checkoutBtn.setFont(new Font("微软雅黑", Font.BOLD, 22));
        checkoutBtn.setBackground(new Color(34, 139, 34)); // 森林绿
        checkoutBtn.setForeground(Color.WHITE);
        checkoutBtn.setBounds(550, 590, 500, 50);
        checkoutBtn.addActionListener(e -> performCheckout());
        this.add(checkoutBtn);
    }

    // ================= 核心逻辑方法 =================

    /**
     * 从数据库加载最新商品列表
     */
    private void loadProducts() {
        allProducts = shopService.getAllProducts();
        DefaultTableModel model = (DefaultTableModel) productTable.getModel();
        model.setRowCount(0); // 清空旧数据

        for (Product p : allProducts) {
            model.addRow(new Object[]{
                    p.getProductId(),
                    p.getName(),
                    p.getPrice(),
                    p.getStock() // 显示当前库存
            });
        }
    }

    /**
     * 搜索会员逻辑
     */
    private void searchMember() {
        String keyword = memberSearchField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入手机号或姓名！");
            return;
        }

        List<Member> results = memberService.search(keyword);
        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this, "未找到会员！\n如果需要散客购买，请点击“重置为散客”。");
        } else {
            // 默认选中第一个结果
            currentMember = results.get(0);
            updateMemberInfo();
            if (results.size() > 1) {
                JOptionPane.showMessageDialog(this, "找到多个匹配项，已默认选择第一个：\n" + currentMember.getName());
            }
        }
    }

    /**
     * 重置为散客模式
     */
    private void resetToGuest() {
        currentMember = null;
        memberSearchField.setText("");
        updateMemberInfo();
    }

    /**
     * 更新顶部会员信息显示
     */
    private void updateMemberInfo() {
        if (currentMember == null) {
            memberInfoLabel.setText("当前状态：[ 散客模式 ] (无关联会员)");
            memberInfoLabel.setForeground(new Color(255, 100, 0)); // 橙色
        } else {
            memberInfoLabel.setText("当前会员： " + currentMember.getName() + " (ID: " + currentMember.getId() + ")");
            memberInfoLabel.setForeground(new Color(0, 100, 255)); // 蓝色
        }
    }

    /**
     * 将商品添加到购物车
     */
    private void addToCart() {
        int row = productTable.getSelectedRow();
        if (row == -1) return;

        // 获取选中的商品信息
        int productId = (int) productTable.getValueAt(row, 0);
        String name = (String) productTable.getValueAt(row, 1);
        int stock = (int) productTable.getValueAt(row, 3);

        if (stock <= 0) {
            JOptionPane.showMessageDialog(this, "商品 [" + name + "] 已售罄！");
            return;
        }

        // 检查购物车已有数量
        int inCart = shoppingCart.getOrDefault(productId, 0);
        if (inCart >= stock) {
            JOptionPane.showMessageDialog(this, "库存不足！\n购物车中已有 " + inCart + " 件，达到库存上限。");
            return;
        }

        // 弹窗询问数量
        String input = JOptionPane.showInputDialog(this, "请输入购买 [" + name + "] 的数量:", "1");
        if (input == null) return; // 用户取消

        try {
            int amount = Integer.parseInt(input);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "数量必须大于0");
                return;
            }
            if (inCart + amount > stock) {
                JOptionPane.showMessageDialog(this, "库存不足！最多还能买 " + (stock - inCart) + " 件。");
                return;
            }

            // 更新购物车数据
            shoppingCart.put(productId, inCart + amount);
            // 刷新视图
            renderCart();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的整数！");
        }
    }

    /**
     * 渲染购物车表格并计算总价
     */
    private void renderCart() {
        cartModel.setRowCount(0);
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (Map.Entry<Integer, Integer> entry : shoppingCart.entrySet()) {
            int pid = entry.getKey();
            int qty = entry.getValue();

            // 在内存列表中查找商品详细信息
            Product p = findProductById(pid);
            if (p != null) {
                // 单价转 BigDecimal
                BigDecimal price = BigDecimal.valueOf(p.getPrice());
                // 小计 = 单价 * 数量
                BigDecimal subTotal = price.multiply(BigDecimal.valueOf(qty));

                // 累加总金额
                grandTotal = grandTotal.add(subTotal);

                // 添加到表格
                cartModel.addRow(new Object[]{
                        p.getProductId(),
                        p.getName(),
                        p.getPrice(),
                        qty,
                        subTotal.doubleValue() // 显示用 double 即可
                });
            }
        }

        // 更新总金额标签
        totalLabel.setText("总计: ¥" + grandTotal.doubleValue());
    }

    private Product findProductById(int id) {
        for (Product p : allProducts) {
            if (p.getProductId() == id) return p;
        }
        return null;
    }

    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的行");
            return;
        }
        int pid = (int) cartTable.getValueAt(row, 0);
        shoppingCart.remove(pid);
        renderCart();
    }

    private void clearCart() {
        shoppingCart.clear();
        renderCart();
    }

    /**
     * 结账核心操作
     */
    private void performCheckout() {
        if (shoppingCart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "购物车是空的，无法结账！");
            return;
        }

        // 1. 确认对话框
        String customerName = (currentMember == null) ? "散客" : currentMember.getName();
        String amountStr = totalLabel.getText(); // "总计: ¥XXX"

        int confirm = JOptionPane.showConfirmDialog(this,
                "客户身份: " + customerName + "\n" +
                        "应收金额: " + amountStr + "\n\n" +
                        "确定要完成这笔交易吗？",
                "收银确认",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        // 2. 准备数据
        // 如果是散客传 0 (DAO层会处理成NULL), 是会员传ID
        int memberId = (currentMember == null) ? 0 : currentMember.getId();

        // 3. 调用 Service
        ShopService.ServiceResult<Void> result = shopService.checkout(memberId, shoppingCart);

        // 4. 处理结果
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, "✅ 交易成功！\n" + result.getMessage());

            // 成功后清理现场
            clearCart();
            loadProducts(); // 重新加载库存（库存减少了）
            resetToGuest(); // 可选：重置回散客状态方便下一位
        } else {
            JOptionPane.showMessageDialog(this, "❌ 交易失败:\n" + result.getMessage());
        }
    }
}