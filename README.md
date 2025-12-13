# 🏋️ 健身房智能管理系统 (Gym Management System)

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-red)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

一个基于 Java Swing 和 MySQL 开发的现代化健身房综合管理系统。系统采用分层架构 (DAO/Service/UI)，界面美观（FlatLaf 主题），功能覆盖会员服务、前台运营、教练排课及后台管理等全业务流程。

---

## ✨ 核心特性

### 🎨 现代化 UI
- **FlatLaf 主题**：采用扁平化设计，告别传统 Swing 的陈旧外观。
- **响应式布局**：根据角色（会员/前台/管理员）自动适配不同的窗口大小和布局。
- **可视化报表**：集成 **JFreeChart**，提供柱状图、饼状图等多维度经营数据分析。

### 👥 多角色权限管理
系统支持四种用户角色，拥有独立的业务视图：

1.  **会员 (Member)**
    * 👤 **个人中心**：查看余额、有效期、个人档案。
    * 📅 **课程预约**：浏览排期表，自助预约团课。
    * 📋 **预约管理**：查看已预约课程，支持双击取消预约。
    * 💳 **自助续费**：在线办理月卡/年卡续费。

2.  **前台 (Receptionist)**
    * ✅ **进场签到**：会员刷卡/手机号快速签到。
    * 🛒 **商品售卖 (POS)**：商品浏览、购物车结算、库存自动扣减。
    * 💰 **余额充值**：为会员账户充值（支持快捷金额）。
    * 👥 **会籍业务**：新会员注册开卡、老会员续费。
    * 📦 **库存查询**：查看商品库存状态。

3.  **教练 (Trainer)**
    * 📝 **上课点名**：查看负责课程的预约名单，进行上课核销。

4.  **管理员 (Admin)**
    * 🛡️ **全权管理**：拥有所有前台和教练的权限。
    * 👔 **员工管理**：员工入职（自动开户）、离职、重置密码、修改信息。
    * 📅 **排课管理**：发布新课程、修改排期、删除课程。
    * 📈 **经营报表**：查看总营收、会员增长、热销商品库存预警（支持图表切换）。

---

## 🛠️ 技术栈

* **编程语言**: Java 21
* **GUI 框架**: Java Swing
* **UI 主题库**: [FlatLaf 3.5.4](https://www.formdev.com/flatlaf/)
* **图表库**: [JFreeChart 1.5.3](https://www.jfree.org/jfreechart/)
* **数据库**: MySQL 8.0+
* **数据库连接**: JDBC (mysql-connector-j 9.3.0)
* **构建工具**: Maven
* **日期组件**: JCalendar 1.4

---

## 🚀 快速开始

### 1. 环境准备
* JDK 21 或更高版本
* MySQL 8.0 或更高版本
* Maven 3.6+
* IntelliJ IDEA (推荐)

### 2. 数据库配置
1.  在 MySQL 中创建一个名为 `gym_system` 的数据库。
2.  找到项目目录下的 `database` 文件夹。
3.  运行 **`gym_system_reset_v2.sql`** 脚本。
    * 该脚本会自动建表、修复结构并插入包含课程时间、会员余额的完美测试数据。
4.  检查 `src/main/java/utils/DBUtil.java` 中的数据库配置：
    ```java
    private static final String URL = "jdbc:mysql://localhost:3306/gym_system?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";     // 你的数据库账号
    private static final String PASSWORD = "your_password"; // 你的数据库密码
    ```

### 3. 构建与运行
1.  使用 IDEA 打开项目。
2.  等待 Maven 下载依赖（如果 `JFreeChart` 爆红，请刷新 Maven）。
3.  运行 `src/main/java/Main.java` 启动程序。

---

## 🔑 默认测试账号

系统初始化后，可使用以下账号登录（密码统一为 **123456**）：

| 角色       | 账号 (Username) | 密码     | 权限说明                      |
| :--------- | :-------------- | :------- | :---------------------------- |
| **管理员** | `admin`         | `123456` | 最高权限，可管理员工和报表    |
| **前台**   | `alice`         | `123456` | 负责日常运营、收银、开卡      |
| **教练**   | `bob`           | `123456` | 负责瑜伽/普拉提课程点名       |
| **会员**   | `zhangsan`      | `123456` | 余额500元，有年卡，有预约记录 |
| **会员**   | `lisi`          | `123456` | 余额0元，月卡已过期           |

> 💡 **提示**：员工登录请选择 "Employee / Staff"，会员登录请选择 "Member"。

---

## 📂 项目结构
