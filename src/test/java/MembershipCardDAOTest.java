//import dao.MembershipCardDAO;
//import dao.MembershipTypeDAO;
//import entity.MembershipCard;
//import entity.MembershipType;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import utils.DateUtils;
//
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.Assert.*;
//
///**
// * MembershipCardDAO 测试类
// *
// * 测试前提：
// * - 数据库中存在 member_id = 1 的会员
// * - 数据库中存在会员卡类型数据
// */
//public class MembershipCardDAOTest {
//
//    private MembershipCardDAO cardDAO;
//    private MembershipTypeDAO typeDAO;
//    private int testCardId;
//
//    @Before
//    public void setUp() {
//        cardDAO = new MembershipCardDAO();
//        typeDAO = new MembershipTypeDAO();
//        testCardId = 0;
//    }
//
//    @After
//    public void tearDown() {
//        // 清理测试数据
//        if (testCardId > 0) {
//            cardDAO.deleteMembershipCard(testCardId);
//            testCardId = 0;
//        }
//    }
//
//    // ==================== 基础查询测试 ====================
//
//    @Test
//    public void testGetById() {
//        MembershipCard card = cardDAO.getById(1);
//        if (card != null) {
//            assertEquals(1, card.getCardId());
//            assertTrue(card.getMemberId() > 0);
//            assertTrue(card.getTypeId() > 0);
//            assertNotNull(card.getMembershipType());
//        }
//    }
//
//    @Test
//    public void testGetByIdNotFound() {
//        MembershipCard card = cardDAO.getById(99999);
//        assertNull(card);
//    }
//
//    @Test
//    public void testGetAll() {
//        List<MembershipCard> cards = cardDAO.getAll();
//        assertNotNull(cards);
//        assertTrue("数据库应有会员卡数据", cards.size() > 0);
//
//        // 验证每张卡都有关联的类型对象
//        for (MembershipCard card : cards) {
//            assertNotNull(card.getMembershipType());
//        }
//    }
//
//    @Test
//    public void testGetByMemberId() {
//        List<MembershipCard> cards = cardDAO.getByMemberId(1);
//        assertNotNull(cards);
//        for (MembershipCard card : cards) {
//            assertEquals(1, card.getMemberId());
//        }
//    }
//
//    @Test
//    public void testGetByStatus() {
//        List<MembershipCard> activeCards = cardDAO.getByStatus(MembershipCardDAO.STATUS_ACTIVE);
//        assertNotNull(activeCards);
//        for (MembershipCard card : activeCards) {
//            assertEquals(MembershipCardDAO.STATUS_ACTIVE, card.getCardStatus());
//        }
//    }
//
//    @Test
//    public void testGetByTypeId() {
//        List<MembershipCard> monthlyCards = cardDAO.getByTypeId(MembershipCardDAO.TYPE_MONTHLY);
//        assertNotNull(monthlyCards);
//        for (MembershipCard card : monthlyCards) {
//            assertEquals(MembershipCardDAO.TYPE_MONTHLY, card.getTypeId());
//            assertTrue(card.isMonthly());
//        }
//    }
//
//    @Test
//    public void testGetMonthlyCards() {
//        List<MembershipCard> cards = cardDAO.getMonthlyCards();
//        assertNotNull(cards);
//        for (MembershipCard card : cards) {
//            assertTrue(card.isMonthly());
//        }
//    }
//
//    @Test
//    public void testGetYearlyCards() {
//        List<MembershipCard> cards = cardDAO.getYearlyCards();
//        assertNotNull(cards);
//        for (MembershipCard card : cards) {
//            assertTrue(card.isYearly());
//        }
//    }
//
//    @Test
//    public void testGetActiveCards() {
//        List<MembershipCard> cards = cardDAO.getActiveCards();
//        assertNotNull(cards);
//        for (MembershipCard card : cards) {
//            assertEquals(MembershipCardDAO.STATUS_ACTIVE, card.getCardStatus());
//        }
//    }
//
//    @Test
//    public void testGetActiveMembershipCard() {
//        MembershipCard card = cardDAO.getActiveMembershipCard(1);
//        if (card != null) {
//            assertEquals(1, card.getMemberId());
//            assertEquals(MembershipCardDAO.STATUS_ACTIVE, card.getCardStatus());
//            assertTrue(DateUtils.isNotExpired(card.getEndDate()));
//        }
//    }
//
//    @Test
//    public void testGetExpiringCards() {
//        List<MembershipCard> cards = cardDAO.getExpiringCards(30);  // 30天内过期
//        assertNotNull(cards);
//        for (MembershipCard card : cards) {
//            assertEquals(MembershipCardDAO.STATUS_ACTIVE, card.getCardStatus());
//            // 验证在30天内过期
//            long daysRemaining = DateUtils.daysRemaining(card.getEndDate());
//            assertTrue(daysRemaining >= 0 && daysRemaining <= 30);
//        }
//    }
//
//    // ==================== 添加会员卡测试 ====================
//
//    @Test
//    public void testAddMonthlyCard() {
//        MembershipCard card = new MembershipCard();
//        card.setMemberId(1);
//        card.setTypeId(MembershipCardDAO.TYPE_MONTHLY);
//        card.setStartDate(DateUtils.now());
//        card.setEndDate(DateUtils.getMonthlyCardEndDate());
//        card.setCardStatus(MembershipCardDAO.STATUS_ACTIVE);
//
//        assertTrue(cardDAO.addMembershipCard(card));
//        assertTrue(card.getCardId() > 0);
//        testCardId = card.getCardId();
//
//        // 验证
//        MembershipCard added = cardDAO.getById(testCardId);
//        assertNotNull(added);
//        assertEquals(MembershipCardDAO.TYPE_MONTHLY, added.getTypeId());
//        assertTrue(added.isMonthly());
//    }
//
//    @Test
//    public void testAddYearlyCard() {
//        MembershipCard card = new MembershipCard();
//        card.setMemberId(1);
//        card.setTypeId(MembershipCardDAO.TYPE_YEARLY);
//        card.setStartDate(DateUtils.now());
//        card.setEndDate(DateUtils.getYearlyCardEndDate());
//        card.setCardStatus(MembershipCardDAO.STATUS_ACTIVE);
//
//        assertTrue(cardDAO.addMembershipCard(card));
//        testCardId = card.getCardId();
//
//        MembershipCard added = cardDAO.getById(testCardId);
//        assertEquals(MembershipCardDAO.TYPE_YEARLY, added.getTypeId());
//        assertTrue(added.isYearly());
//    }
//
//    @Test
//    public void testCreateMonthlyCard() {
//        // 使用便捷方法创建月卡
//        assertTrue(cardDAO.createMonthlyCard(1));
//
//        // 获取刚创建的卡
//        List<MembershipCard> cards = cardDAO.getByMemberId(1);
//        assertNotNull(cards);
//        assertTrue(cards.size() > 0);
//
//        // 清理最新的卡
//        MembershipCard latestCard = cards.get(0);  // 按ID降序，第一个是最新的
//        testCardId = latestCard.getCardId();
//    }
//
//    @Test
//    public void testCreateYearlyCard() {
//        assertTrue(cardDAO.createYearlyCard(1));
//
//        List<MembershipCard> cards = cardDAO.getByMemberId(1);
//        MembershipCard latestCard = cards.get(0);
//        testCardId = latestCard.getCardId();
//    }
//
//    @Test
//    public void testAddCardInvalidMember() {
//        MembershipCard card = new MembershipCard();
//        card.setMemberId(99999);  // 不存在的会员
//        card.setTypeId(MembershipCardDAO.TYPE_MONTHLY);
//        card.setStartDate(DateUtils.now());
//        card.setEndDate(DateUtils.getMonthlyCardEndDate());
//        card.setCardStatus(MembershipCardDAO.STATUS_ACTIVE);
//
//        assertFalse("无效会员应该添加失败", cardDAO.addMembershipCard(card));
//    }
//
//    @Test
//    public void testAddCardInvalidType() {
//        MembershipCard card = new MembershipCard();
//        card.setMemberId(1);
//        card.setTypeId(99);  // 无效类型
//        card.setStartDate(DateUtils.now());
//        card.setEndDate(DateUtils.addMonths(DateUtils.now(), 1));
//        card.setCardStatus(MembershipCardDAO.STATUS_ACTIVE);
//
//        assertFalse("无效类型应该添加失败", cardDAO.addMembershipCard(card));
//    }
//
//    @Test
//    public void testAddCardInvalidStatus() {
//        MembershipCard card = new MembershipCard();
//        card.setMemberId(1);
//        card.setTypeId(MembershipCardDAO.TYPE_MONTHLY);
//        card.setStartDate(DateUtils.now());
//        card.setEndDate(DateUtils.getMonthlyCardEndDate());
//        card.setCardStatus("invalid_status");
//
//        assertFalse("无效状态应该添加失败", cardDAO.addMembershipCard(card));
//    }
//
//    @Test
//    public void testAddCardInvalidDateRange() {
//        MembershipCard card = new MembershipCard();
//        card.setMemberId(1);
//        card.setTypeId(MembershipCardDAO.TYPE_MONTHLY);
//        card.setStartDate(DateUtils.now());
//        card.setEndDate(DateUtils.addDays(DateUtils.now(), -1));  // 结束日期早于开始日期
//        card.setCardStatus(MembershipCardDAO.STATUS_ACTIVE);
//
//        assertFalse("无效日期范围应该添加失败", cardDAO.addMembershipCard(card));
//    }
//
//    // ==================== 更新会员卡测试 ====================
//
//    @Test
//    public void testUpdateMembershipCard() {
//        MembershipCard card = cardDAO.getById(1);
//        if (card == null) return;
//
//        String originalStatus = card.getCardStatus();
//        card.setCardStatus(MembershipCardDAO.STATUS_INACTIVE);
//
//        assertTrue(cardDAO.updateMembershipCard(card));
//
//        MembershipCard updated = cardDAO.getById(1);
//        assertEquals(MembershipCardDAO.STATUS_INACTIVE, updated.getCardStatus());
//
//        // 恢复
//        card.setCardStatus(originalStatus);
//        cardDAO.updateMembershipCard(card);
//    }
//
//    @Test
//    public void testUpdateCardStatus() {
//        MembershipCard card = cardDAO.getById(1);
//        if (card == null) return;
//
//        String originalStatus = card.getCardStatus();
//
//        assertTrue(cardDAO.updateCardStatus(1, MembershipCardDAO.STATUS_INACTIVE));
//
//        MembershipCard updated = cardDAO.getById(1);
//        assertEquals(MembershipCardDAO.STATUS_INACTIVE, updated.getCardStatus());
//
//        // 恢复
//        cardDAO.updateCardStatus(1, originalStatus);
//    }
//
//    @Test
//    public void testUpdateCardStatusInvalid() {
//        assertFalse("无效状态应该更新失败", cardDAO.updateCardStatus(1, "invalid"));
//    }
//
//    @Test
//    public void testUpdateExpiredCards() {
//        int count = cardDAO.updateExpiredCards();
//        assertTrue(count >= 0);
//    }
//
//    @Test
//    public void testRenewCard() {
//        // 先创建一张测试卡
//        MembershipCard card = new MembershipCard();
//        card.setMemberId(1);
//        card.setTypeId(MembershipCardDAO.TYPE_MONTHLY);
//        card.setStartDate(DateUtils.now());
//        card.setEndDate(DateUtils.addDays(DateUtils.now(), 10));
//        card.setCardStatus(MembershipCardDAO.STATUS_ACTIVE);
//
//        assertTrue(cardDAO.addMembershipCard(card));
//        testCardId = card.getCardId();
//
//        // 获取续费前的剩余天数
//        int beforeRenewDays = cardDAO.getRemainingDays(testCardId);
//
//        // 续费30天
//        assertTrue(cardDAO.renewCard(testCardId, 30));
//
//        // 获取续费后的剩余天数
//        int afterRenewDays = cardDAO.getRemainingDays(testCardId);
//
//        // 续费后的剩余天数应该比续费前多大约30天（允许1天的误差）
//        int daysDiff = afterRenewDays - beforeRenewDays;
//        assertTrue("续费后应该增加约30天", daysDiff >= 29 && daysDiff <= 31);
//    }
//
//    @Test
//    public void testRenewMonthlyCard() {
//        // 先创建一张测试卡
//        MembershipCard card = new MembershipCard();
//        card.setMemberId(1);
//        card.setTypeId(MembershipCardDAO.TYPE_MONTHLY);
//        card.setStartDate(DateUtils.now());
//        card.setEndDate(DateUtils.addDays(DateUtils.now(), 5));
//        card.setCardStatus(MembershipCardDAO.STATUS_ACTIVE);
//
//        assertTrue(cardDAO.addMembershipCard(card));
//        testCardId = card.getCardId();
//
//        assertTrue(cardDAO.renewMonthlyCard(testCardId));
//    }
//
//    @Test
//    public void testRenewYearlyCard() {
//        MembershipCard card = new MembershipCard();
//        card.setMemberId(1);
//        card.setTypeId(MembershipCardDAO.TYPE_YEARLY);
//        card.setStartDate(DateUtils.now());
//        card.setEndDate(DateUtils.addDays(DateUtils.now(), 5));
//        card.setCardStatus(MembershipCardDAO.STATUS_ACTIVE);
//
//        assertTrue(cardDAO.addMembershipCard(card));
//        testCardId = card.getCardId();
//
//        assertTrue(cardDAO.renewYearlyCard(testCardId));
//    }
//
//    @Test
//    public void testRenewCardInvalidDays() {
//        assertFalse("续费天数<=0应该失败", cardDAO.renewCard(1, 0));
//        assertFalse("续费天数<=0应该失败", cardDAO.renewCard(1, -10));
//    }
//
//    @Test
//    public void testRenewCardNotFound() {
//        assertFalse("不存在的卡续费应该失败", cardDAO.renewCard(99999, 30));
//    }
//
//    // ==================== 删除会员卡测试 ====================
//
//    @Test
//    public void testDeleteMembershipCard() {
//        // 先添加测试数据
//        MembershipCard card = new MembershipCard();
//        card.setMemberId(1);
//        card.setTypeId(MembershipCardDAO.TYPE_MONTHLY);
//        card.setStartDate(DateUtils.now());
//        card.setEndDate(DateUtils.getMonthlyCardEndDate());
//        card.setCardStatus(MembershipCardDAO.STATUS_ACTIVE);
//
//        assertTrue(cardDAO.addMembershipCard(card));
//        int cardId = card.getCardId();
//
//        // 删除
//        assertTrue(cardDAO.deleteMembershipCard(cardId));
//
//        // 验证已删除
//        assertNull(cardDAO.getById(cardId));
//
//        testCardId = 0;
//    }
//
//    @Test
//    public void testDeleteMembershipCardNotFound() {
//        assertFalse(cardDAO.deleteMembershipCard(99999));
//    }
//
//    // ==================== 时间相关查询测试 ====================
//
//    @Test
//    public void testGetRemainingDays() {
//        MembershipCard card = cardDAO.getById(1);
//        if (card != null) {
//            int days = cardDAO.getRemainingDays(1);
//            // 验证计算正确
//            long expected = DateUtils.daysRemaining(card.getEndDate());
//            assertEquals(expected, days);
//        }
//    }
//
//    @Test
//    public void testGetRemainingDaysNotFound() {
//        int days = cardDAO.getRemainingDays(99999);
//        assertEquals(-999, days);
//    }
//
//    @Test
//    public void testIsCardValid() {
//        MembershipCard card = cardDAO.getById(1);
//        if (card != null) {
//            boolean isValid = cardDAO.isCardValid(1);
//            // 验证与手动判断一致
//            boolean expected = MembershipCardDAO.STATUS_ACTIVE.equals(card.getCardStatus())
//                    && DateUtils.isNotExpired(card.getEndDate());
//            assertEquals(expected, isValid);
//        }
//    }
//
//    @Test
//    public void testIsCardValidNotFound() {
//        assertFalse(cardDAO.isCardValid(99999));
//    }
//
//    @Test
//    public void testHasMemberValidCard() {
//        boolean hasCard = cardDAO.hasMemberValidCard(1);
//        // 结果取决于数据库数据
//        assertNotNull(hasCard);
//    }
//
//    // ==================== 统计功能测试 ====================
//
//    @Test
//    public void testGetTotalCardCount() {
//        int count = cardDAO.getTotalCardCount();
//        assertTrue(count >= 0);
//
//        // 验证与列表大小一致
//        List<MembershipCard> cards = cardDAO.getAll();
//        assertEquals(cards.size(), count);
//    }
//
//    @Test
//    public void testGetCardCountByStatus() {
//        Map<String, Integer> countMap = cardDAO.getCardCountByStatus();
//        assertNotNull(countMap);
//
//        // 应该包含所有状态
//        for (String status : MembershipCardDAO.VALID_STATUSES) {
//            assertTrue(countMap.containsKey(status));
//            assertTrue(countMap.get(status) >= 0);
//        }
//    }
//
//    @Test
//    public void testGetCardCountByType() {
//        Map<Integer, Integer> countMap = cardDAO.getCardCountByType();
//        assertNotNull(countMap);
//
//        assertTrue(countMap.containsKey(MembershipCardDAO.TYPE_MONTHLY));
//        assertTrue(countMap.containsKey(MembershipCardDAO.TYPE_YEARLY));
//    }
//
//    @Test
//    public void testGetActiveCardCount() {
//        int count = cardDAO.getActiveCardCount();
//        assertTrue(count >= 0);
//    }
//
//    @Test
//    public void testGetExpiredCardCount() {
//        int count = cardDAO.getExpiredCardCount();
//        assertTrue(count >= 0);
//    }
//
//    // ==================== 校验方法测试 ====================
//
//    @Test
//    public void testIsValidTypeId() {
//        assertTrue(cardDAO.isValidTypeId(MembershipCardDAO.TYPE_MONTHLY));
//        assertTrue(cardDAO.isValidTypeId(MembershipCardDAO.TYPE_YEARLY));
//        assertFalse(cardDAO.isValidTypeId(99));
//    }
//
//    @Test
//    public void testIsValidCardStatus() {
//        assertTrue(cardDAO.isValidCardStatus(MembershipCardDAO.STATUS_ACTIVE));
//        assertTrue(cardDAO.isValidCardStatus(MembershipCardDAO.STATUS_INACTIVE));
//        assertTrue(cardDAO.isValidCardStatus(MembershipCardDAO.STATUS_EXPIRED));
//        assertFalse(cardDAO.isValidCardStatus("invalid"));
//        assertFalse(cardDAO.isValidCardStatus(null));
//    }
//
//    // ==================== 工具方法测试 ====================
//
//    @Test
//    public void testGetStatusDisplayName() {
//        assertEquals("有效", cardDAO.getStatusDisplayName(MembershipCardDAO.STATUS_ACTIVE));
//        assertEquals("停用", cardDAO.getStatusDisplayName(MembershipCardDAO.STATUS_INACTIVE));
//        assertEquals("过期", cardDAO.getStatusDisplayName(MembershipCardDAO.STATUS_EXPIRED));
//        assertEquals("未知", cardDAO.getStatusDisplayName("invalid"));
//        assertEquals("未知", cardDAO.getStatusDisplayName(null));
//    }
//
//    @Test
//    public void testGetTypeDisplayName() {
//        assertEquals("月卡", cardDAO.getTypeDisplayName(MembershipCardDAO.TYPE_MONTHLY));
//        assertEquals("年卡", cardDAO.getTypeDisplayName(MembershipCardDAO.TYPE_YEARLY));
//        assertEquals("未知", cardDAO.getTypeDisplayName(99));
//    }
//
//    // ==================== 常量测试 ====================
//
//    @Test
//    public void testStatusConstants() {
//        assertEquals("active", MembershipCardDAO.STATUS_ACTIVE);
//        assertEquals("inactive", MembershipCardDAO.STATUS_INACTIVE);
//        assertEquals("expired", MembershipCardDAO.STATUS_EXPIRED);
//    }
//
//    @Test
//    public void testTypeConstants() {
//        assertEquals(1, MembershipCardDAO.TYPE_MONTHLY);
//        assertEquals(2, MembershipCardDAO.TYPE_YEARLY);
//    }
//
//    // ==================== 类型关联测试 ====================
//
//    @Test
//    public void testMembershipTypeRelation() {
//        MembershipCard card = cardDAO.getById(1);
//        if (card == null) return;
//
//        MembershipType type = card.getMembershipType();
//        assertNotNull(type);
//
//        // 验证类型信息
//        assertTrue(type.getDurationDays() > 0);
//        assertTrue(type.getPrice() > 0);
//        assertNotNull(type.getTypeName());
//
//        // 验证卡的便捷方法
//        assertEquals(type.getPrice(), card.getPrice(), 0.01);
//        assertEquals(type.getDurationDays(), card.getDurationDays());
//    }
//
//    @Test
//    public void testCardTypeConsistency() {
//        // 验证数据库中所有卡的类型一致性
//        List<MembershipCard> allCards = cardDAO.getAll();
//        for (MembershipCard card : allCards) {
//            if (card.getTypeId() == MembershipCardDAO.TYPE_MONTHLY) {
//                assertEquals("Monthly", card.getCardType());
//                assertTrue(card.isMonthly());
//                assertFalse(card.isYearly());
//            } else if (card.getTypeId() == MembershipCardDAO.TYPE_YEARLY) {
//                assertEquals("Yearly", card.getCardType());
//                assertTrue(card.isYearly());
//                assertFalse(card.isMonthly());
//            }
//        }
//    }
//
//    // ==================== 兼容旧代码测试 ====================
//
//    @Test
//    public void testSetCardTypeCompatibility() {
//        MembershipCard card = new MembershipCard();
//        card.setMemberId(1);
//        card.setCardType("monthly");  // 使用字符串方式设置
//        card.setStartDate(DateUtils.now());
//        card.setEndDate(DateUtils.getMonthlyCardEndDate());
//        card.setCardStatus(MembershipCardDAO.STATUS_ACTIVE);
//
//        assertTrue(cardDAO.addMembershipCard(card));
//        testCardId = card.getCardId();
//
//        // 验证 typeId 被正确设置
//        assertEquals(MembershipCardDAO.TYPE_MONTHLY, card.getTypeId());
//    }
//}
