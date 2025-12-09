import dao.MembershipTypeDAO;
import entity.MembershipType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * MembershipTypeDAO 测试类
 * 
 * 测试前提：
 * - 数据库中存在月卡类型（type_id=1）和年卡类型（type_id=2）
 */
public class MembershipTypeDAOTest {

    private MembershipTypeDAO typeDAO;
    private int testTypeId;

    @Before
    public void setUp() {
        typeDAO = new MembershipTypeDAO();
        testTypeId = 0;
    }

    @After
    public void tearDown() {
        // 清理测试数据
        if (testTypeId > 0) {
            typeDAO.deleteType(testTypeId);
            testTypeId = 0;
        }
    }

    // ==================== 基础查询测试 ====================

    @Test
    public void testGetTypeById() {
        MembershipType type = typeDAO.getTypeById(MembershipTypeDAO.TYPE_ID_MONTHLY);
        assertNotNull(type);
        assertEquals(MembershipTypeDAO.TYPE_ID_MONTHLY, type.getTypeId());
        assertEquals(MembershipTypeDAO.TYPE_NAME_MONTHLY, type.getTypeName());
    }

    @Test
    public void testGetTypeByIdNotFound() {
        MembershipType type = typeDAO.getTypeById(99999);
        assertNull(type);
    }

    @Test
    public void testGetTypeByName() {
        MembershipType type = typeDAO.getTypeByName(MembershipTypeDAO.TYPE_NAME_MONTHLY);
        assertNotNull(type);
        assertEquals(MembershipTypeDAO.TYPE_NAME_MONTHLY, type.getTypeName());
    }

    @Test
    public void testGetTypeByNameNotFound() {
        MembershipType type = typeDAO.getTypeByName("NotExist");
        assertNull(type);
    }

    @Test
    public void testGetAllTypes() {
        List<MembershipType> types = typeDAO.getAllTypes();
        assertNotNull(types);
        assertTrue("数据库应有类型数据", types.size() >= 2);
    }

    @Test
    public void testGetMonthlyType() {
        MembershipType type = typeDAO.getMonthlyType();
        assertNotNull(type);
        assertEquals(MembershipTypeDAO.TYPE_ID_MONTHLY, type.getTypeId());
        assertTrue(type.isMonthly());
        assertFalse(type.isYearly());
    }

    @Test
    public void testGetYearlyType() {
        MembershipType type = typeDAO.getYearlyType();
        assertNotNull(type);
        assertEquals(MembershipTypeDAO.TYPE_ID_YEARLY, type.getTypeId());
        assertTrue(type.isYearly());
        assertFalse(type.isMonthly());
    }

    // ==================== 添加类型测试 ====================

    @Test
    public void testAddType() {
        // 由于数据库 type_id 没有 AUTO_INCREMENT，使用 addTypeWithId
        // 先删除可能存在的测试数据
        typeDAO.deleteType(100);

        MembershipType type = new MembershipType();
        type.setTypeId(100);  // 指定一个测试用的ID
        type.setTypeName("Quarterly");
        type.setDurationDays(90);
        type.setPrice(500.0);
        type.setDescription("季卡");

        assertTrue(typeDAO.addTypeWithId(type));
        testTypeId = type.getTypeId();

        // 验证添加成功
        MembershipType added = typeDAO.getTypeById(testTypeId);
        assertNotNull(added);
        assertEquals("Quarterly", added.getTypeName());
        assertEquals(90, added.getDurationDays());
        assertEquals(500.0, added.getPrice(), 0.01);
    }

    @Test
    public void testAddTypeEmptyName() {
        MembershipType type = new MembershipType();
        type.setTypeName("");  // 空名称
        type.setDurationDays(30);
        type.setPrice(100.0);

        assertFalse("空名称应该添加失败", typeDAO.addType(type));
    }

    @Test
    public void testAddTypeNullName() {
        MembershipType type = new MembershipType();
        type.setTypeName(null);  // null名称
        type.setDurationDays(30);
        type.setPrice(100.0);

        assertFalse("null名称应该添加失败", typeDAO.addType(type));
    }

    @Test
    public void testAddTypeInvalidDuration() {
        MembershipType type = new MembershipType();
        type.setTypeName("TestType");
        type.setDurationDays(0);  // 无效天数
        type.setPrice(100.0);

        assertFalse("无效天数应该添加失败", typeDAO.addType(type));
    }

    @Test
    public void testAddTypeNegativePrice() {
        MembershipType type = new MembershipType();
        type.setTypeName("TestType2");
        type.setDurationDays(30);
        type.setPrice(-100.0);  // 负价格

        assertFalse("负价格应该添加失败", typeDAO.addType(type));
    }

    @Test
    public void testAddTypeDuplicateName() {
        // 尝试添加已存在的名称
        MembershipType type = new MembershipType();
        type.setTypeName(MembershipTypeDAO.TYPE_NAME_MONTHLY);  // 已存在
        type.setDurationDays(30);
        type.setPrice(100.0);

        assertFalse("重复名称应该添加失败", typeDAO.addType(type));
    }

    @Test
    public void testAddTypeWithId() {
        // 先删除可能存在的测试数据
        typeDAO.deleteType(999);

        MembershipType type = new MembershipType();
        type.setTypeId(999);
        type.setTypeName("TestTypeWithId");
        type.setDurationDays(60);
        type.setPrice(300.0);
        type.setDescription("测试类型");

        assertTrue(typeDAO.addTypeWithId(type));
        testTypeId = 999;

        MembershipType added = typeDAO.getTypeById(999);
        assertNotNull(added);
        assertEquals(999, added.getTypeId());
    }

    // ==================== 更新类型测试 ====================

    @Test
    public void testUpdateType() {
        MembershipType type = typeDAO.getMonthlyType();
        assertNotNull(type);

        double originalPrice = type.getPrice();
        type.setPrice(250.0);

        assertTrue(typeDAO.updateType(type));

        MembershipType updated = typeDAO.getMonthlyType();
        assertEquals(250.0, updated.getPrice(), 0.01);

        // 恢复
        type.setPrice(originalPrice);
        typeDAO.updateType(type);
    }

    @Test
    public void testUpdateTypeInvalidDuration() {
        MembershipType type = typeDAO.getMonthlyType();
        assertNotNull(type);

        int originalDuration = type.getDurationDays();
        type.setDurationDays(0);  // 无效天数

        assertFalse("无效天数应该更新失败", typeDAO.updateType(type));

        // 恢复
        type.setDurationDays(originalDuration);
    }

    @Test
    public void testUpdateTypeNegativePrice() {
        MembershipType type = typeDAO.getMonthlyType();
        assertNotNull(type);

        double originalPrice = type.getPrice();
        type.setPrice(-100.0);  // 负价格

        assertFalse("负价格应该更新失败", typeDAO.updateType(type));

        // 恢复
        type.setPrice(originalPrice);
    }

    @Test
    public void testUpdatePrice() {
        MembershipType type = typeDAO.getMonthlyType();
        assertNotNull(type);

        double originalPrice = type.getPrice();

        assertTrue(typeDAO.updatePrice(MembershipTypeDAO.TYPE_ID_MONTHLY, 280.0));

        MembershipType updated = typeDAO.getMonthlyType();
        assertEquals(280.0, updated.getPrice(), 0.01);

        // 恢复
        typeDAO.updatePrice(MembershipTypeDAO.TYPE_ID_MONTHLY, originalPrice);
    }

    @Test
    public void testUpdatePriceNegative() {
        assertFalse("负价格应该更新失败", typeDAO.updatePrice(MembershipTypeDAO.TYPE_ID_MONTHLY, -100.0));
    }

    // ==================== 删除类型测试 ====================

    @Test
    public void testDeleteType() {
        // 先删除可能存在的测试数据
        typeDAO.deleteType(101);

        // 添加测试数据
        MembershipType type = new MembershipType();
        type.setTypeId(101);  // 指定一个测试用的ID
        type.setTypeName("ToDelete");
        type.setDurationDays(15);
        type.setPrice(80.0);

        assertTrue(typeDAO.addTypeWithId(type));
        int typeId = type.getTypeId();

        // 删除
        assertTrue(typeDAO.deleteType(typeId));

        // 验证已删除
        assertNull(typeDAO.getTypeById(typeId));

        testTypeId = 0;
    }

    @Test
    public void testDeleteTypeNotFound() {
        assertFalse(typeDAO.deleteType(99999));
    }

    @Test
    public void testDeleteTypeInUse() {
        // 月卡和年卡通常有会员卡在使用，不能删除
        // 这个测试取决于数据库数据
        int cardCount = typeDAO.getCardCountByType(MembershipTypeDAO.TYPE_ID_MONTHLY);
        if (cardCount > 0) {
            assertFalse("有会员卡使用的类型不能删除", typeDAO.deleteType(MembershipTypeDAO.TYPE_ID_MONTHLY));
        }
    }

    // ==================== 便捷查询测试 ====================

    @Test
    public void testGetPriceByTypeId() {
        double monthlyPrice = typeDAO.getPriceByTypeId(MembershipTypeDAO.TYPE_ID_MONTHLY);
        assertTrue(monthlyPrice > 0);

        double yearlyPrice = typeDAO.getPriceByTypeId(MembershipTypeDAO.TYPE_ID_YEARLY);
        assertTrue(yearlyPrice > 0);

        // 年卡价格应该比月卡高
        assertTrue(yearlyPrice > monthlyPrice);
    }

    @Test
    public void testGetPriceByTypeIdNotFound() {
        double price = typeDAO.getPriceByTypeId(99999);
        assertEquals(0.0, price, 0.01);
    }

    @Test
    public void testGetDurationDaysByTypeId() {
        int monthlyDays = typeDAO.getDurationDaysByTypeId(MembershipTypeDAO.TYPE_ID_MONTHLY);
        assertEquals(MembershipTypeDAO.DEFAULT_MONTHLY_DAYS, monthlyDays);

        int yearlyDays = typeDAO.getDurationDaysByTypeId(MembershipTypeDAO.TYPE_ID_YEARLY);
        assertEquals(MembershipTypeDAO.DEFAULT_YEARLY_DAYS, yearlyDays);
    }

    @Test
    public void testGetDurationDaysByTypeIdNotFound() {
        int days = typeDAO.getDurationDaysByTypeId(99999);
        assertEquals(0, days);
    }

    @Test
    public void testGetMonthlyPrice() {
        double price = typeDAO.getMonthlyPrice();
        assertTrue(price > 0);

        // 验证与直接查询一致
        assertEquals(typeDAO.getPriceByTypeId(MembershipTypeDAO.TYPE_ID_MONTHLY), price, 0.01);
    }

    @Test
    public void testGetYearlyPrice() {
        double price = typeDAO.getYearlyPrice();
        assertTrue(price > 0);

        assertEquals(typeDAO.getPriceByTypeId(MembershipTypeDAO.TYPE_ID_YEARLY), price, 0.01);
    }

    // ==================== 统计功能测试 ====================

    @Test
    public void testGetTotalTypeCount() {
        int count = typeDAO.getTotalTypeCount();
        assertTrue(count >= 2);

        // 验证与列表大小一致
        List<MembershipType> types = typeDAO.getAllTypes();
        assertEquals(types.size(), count);
    }

    @Test
    public void testGetCardCountByType() {
        int monthlyCount = typeDAO.getCardCountByType(MembershipTypeDAO.TYPE_ID_MONTHLY);
        assertTrue(monthlyCount >= 0);

        int yearlyCount = typeDAO.getCardCountByType(MembershipTypeDAO.TYPE_ID_YEARLY);
        assertTrue(yearlyCount >= 0);
    }

    @Test
    public void testGetMonthlyCardCount() {
        int count = typeDAO.getMonthlyCardCount();
        assertTrue(count >= 0);

        assertEquals(typeDAO.getCardCountByType(MembershipTypeDAO.TYPE_ID_MONTHLY), count);
    }

    @Test
    public void testGetYearlyCardCount() {
        int count = typeDAO.getYearlyCardCount();
        assertTrue(count >= 0);

        assertEquals(typeDAO.getCardCountByType(MembershipTypeDAO.TYPE_ID_YEARLY), count);
    }

    // ==================== 工具方法测试 ====================

    @Test
    public void testGetTypeDisplayNameById() {
        assertEquals("月卡", typeDAO.getTypeDisplayName(MembershipTypeDAO.TYPE_ID_MONTHLY));
        assertEquals("年卡", typeDAO.getTypeDisplayName(MembershipTypeDAO.TYPE_ID_YEARLY));
    }

    @Test
    public void testGetTypeDisplayNameByName() {
        assertEquals("月卡", typeDAO.getTypeDisplayName(MembershipTypeDAO.TYPE_NAME_MONTHLY));
        assertEquals("年卡", typeDAO.getTypeDisplayName(MembershipTypeDAO.TYPE_NAME_YEARLY));
        assertEquals("未知", typeDAO.getTypeDisplayName((String) null));
    }

    @Test
    public void testIsValidTypeId() {
        assertTrue(typeDAO.isValidTypeId(MembershipTypeDAO.TYPE_ID_MONTHLY));
        assertTrue(typeDAO.isValidTypeId(MembershipTypeDAO.TYPE_ID_YEARLY));
        assertFalse(typeDAO.isValidTypeId(99999));
    }

    @Test
    public void testIsValidTypeName() {
        assertTrue(typeDAO.isValidTypeName(MembershipTypeDAO.TYPE_NAME_MONTHLY));
        assertTrue(typeDAO.isValidTypeName(MembershipTypeDAO.TYPE_NAME_YEARLY));
        assertFalse(typeDAO.isValidTypeName("NotExist"));
    }

    @Test
    public void testIsMonthlyType() {
        assertTrue(typeDAO.isMonthlyType(MembershipTypeDAO.TYPE_ID_MONTHLY));
        assertFalse(typeDAO.isMonthlyType(MembershipTypeDAO.TYPE_ID_YEARLY));
        assertFalse(typeDAO.isMonthlyType(99));
    }

    @Test
    public void testIsYearlyType() {
        assertTrue(typeDAO.isYearlyType(MembershipTypeDAO.TYPE_ID_YEARLY));
        assertFalse(typeDAO.isYearlyType(MembershipTypeDAO.TYPE_ID_MONTHLY));
        assertFalse(typeDAO.isYearlyType(99));
    }

    // ==================== 常量测试 ====================

    @Test
    public void testTypeIdConstants() {
        assertEquals(1, MembershipTypeDAO.TYPE_ID_MONTHLY);
        assertEquals(2, MembershipTypeDAO.TYPE_ID_YEARLY);
    }

    @Test
    public void testTypeNameConstants() {
        assertEquals("Monthly", MembershipTypeDAO.TYPE_NAME_MONTHLY);
        assertEquals("Yearly", MembershipTypeDAO.TYPE_NAME_YEARLY);
    }

    @Test
    public void testDefaultDaysConstants() {
        assertEquals(30, MembershipTypeDAO.DEFAULT_MONTHLY_DAYS);
        assertEquals(365, MembershipTypeDAO.DEFAULT_YEARLY_DAYS);
    }

    // ==================== 实体类测试 ====================

    @Test
    public void testMembershipTypeEntity() {
        MembershipType type = typeDAO.getMonthlyType();
        assertNotNull(type);

        // 测试实体类方法
        assertTrue(type.isMonthly());
        assertFalse(type.isYearly());

        MembershipType yearlyType = typeDAO.getYearlyType();
        assertTrue(yearlyType.isYearly());
        assertFalse(yearlyType.isMonthly());
    }

    @Test
    public void testMembershipTypeToString() {
        MembershipType type = typeDAO.getMonthlyType();
        assertNotNull(type);

        String str = type.toString();
        assertNotNull(str);
        assertTrue(str.contains("typeId"));
        assertTrue(str.contains("typeName"));
    }
}
