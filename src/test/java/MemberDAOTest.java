import dao.MemberDAO;
import entity.Member;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.DateUtils;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * MemberDAO 测试类
 * 
 * 测试前提：
 * - 数据库中存在会员数据
 * - 会员ID=1的张三存在
 */
public class MemberDAOTest {

    private MemberDAO memberDAO;
    private int testMemberId;  // 用于清理测试数据

    @Before
    public void setUp() {
        memberDAO = new MemberDAO();
        testMemberId = 0;
    }

    @After
    public void tearDown() {
        // 清理测试数据
        if (testMemberId > 0) {
            memberDAO.deleteMember(testMemberId);
            testMemberId = 0;
        }
    }

    // ==================== 基础查询测试 ====================

    @Test
    public void testGetMemberById() {
        Member member = memberDAO.getMemberById(1);
        assertNotNull(member);
        assertEquals(1, member.getId());
        assertEquals("张三", member.getName());
        assertEquals("13900001111", member.getPhone());
    }

    @Test
    public void testGetMemberByIdNotFound() {
        Member member = memberDAO.getMemberById(99999);
        assertNull(member);
    }

    @Test
    public void testGetAllMembers() {
        List<Member> members = memberDAO.getAllMembers();
        assertNotNull(members);
        assertTrue("数据库应有会员数据", members.size() > 0);
    }

    @Test
    public void testGetMembersByName() {
        List<Member> members = memberDAO.getMembersByName("张三");
        assertNotNull(members);
        assertTrue(members.size() > 0);
        assertEquals("张三", members.get(0).getName());
    }

    @Test
    public void testSearchMembersByName() {
        List<Member> members = memberDAO.searchMembersByName("张");
        assertNotNull(members);
        for (Member member : members) {
            assertTrue(member.getName().contains("张"));
        }
    }

    @Test
    public void testGetMemberByPhone() {
        Member member = memberDAO.getMemberByPhone("13900001111");
        assertNotNull(member);
        assertEquals("张三", member.getName());
    }

    @Test
    public void testGetMemberByPhoneNotFound() {
        Member member = memberDAO.getMemberByPhone("99999999999");
        assertNull(member);
    }

    @Test
    public void testSearchByPhone() {
        List<Member> members = memberDAO.searchByPhone("139");
        assertNotNull(members);
        for (Member member : members) {
            assertTrue(member.getPhone().contains("139"));
        }
    }

    @Test
    public void testGetMembersByGender() {
        List<Member> maleMembers = memberDAO.getMembersByGender(MemberDAO.GENDER_MALE);
        assertNotNull(maleMembers);
        for (Member member : maleMembers) {
            assertEquals(MemberDAO.GENDER_MALE, member.getGender());
        }

        List<Member> femaleMembers = memberDAO.getMembersByGender(MemberDAO.GENDER_FEMALE);
        assertNotNull(femaleMembers);
        for (Member member : femaleMembers) {
            assertEquals(MemberDAO.GENDER_FEMALE, member.getGender());
        }
    }

    @Test
    public void testGetMembersByStatus() {
        List<Member> activeMembers = memberDAO.getMembersByStatus(MemberDAO.STATUS_ACTIVE);
        assertNotNull(activeMembers);
        for (Member member : activeMembers) {
            assertEquals(MemberDAO.STATUS_ACTIVE, member.getStatus());
        }
    }

    @Test
    public void testGetActiveMembers() {
        List<Member> activeMembers = memberDAO.getActiveMembers();
        assertNotNull(activeMembers);
        for (Member member : activeMembers) {
            assertEquals(MemberDAO.STATUS_ACTIVE, member.getStatus());
        }
    }

    @Test
    public void testGetMembersByBirthDate() {
        Member member = memberDAO.getMemberById(1);
        assertNotNull(member);

        if (member.getBirthDate() != null) {
            List<Member> members = memberDAO.getMembersByBirthDate(member.getBirthDate());
            assertNotNull(members);
        }
    }

    @Test
    public void testGetMembersByAgeRange() {
        List<Member> members = memberDAO.getMembersByAgeRange(20, 40);
        assertNotNull(members);
        for (Member member : members) {
            int age = DateUtils.calculateAge(member.getBirthDate());
            assertTrue(age >= 20 && age <= 40);
        }
    }

    @Test
    public void testGetMembersWithValidCard() {
        List<Member> members = memberDAO.getMembersWithValidCard();
        assertNotNull(members);
        // 这些会员都应该有有效的会员卡
    }

    // ==================== 添加会员测试 ====================

    @Test
    public void testAddMember() {
        Member member = new Member();
        member.setName("测试用户");
        member.setPhone("13912345678");
        member.setEmail("test@email.com");
        member.setGender(MemberDAO.GENDER_MALE);
        member.setBirthDate(DateUtils.addYears(DateUtils.now(), -25));  // 25岁
        member.setRegisterDate(DateUtils.now());
        member.setStatus(MemberDAO.STATUS_ACTIVE);

        assertTrue(memberDAO.addMember(member));
        assertTrue(member.getId() > 0);
        testMemberId = member.getId();

        // 验证添加成功
        Member added = memberDAO.getMemberById(testMemberId);
        assertNotNull(added);
        assertEquals("测试用户", added.getName());
        assertEquals("13912345678", added.getPhone());
    }

    @Test
    public void testAddMemberWithDefaultRegisterDate() {
        Member member = new Member();
        member.setName("测试用户2");
        member.setPhone("13912345679");
        member.setEmail("test2@email.com");
        member.setGender(MemberDAO.GENDER_FEMALE);
        member.setBirthDate(DateUtils.addYears(DateUtils.now(), -30));
        // 不设置 registerDate
        member.setStatus(MemberDAO.STATUS_ACTIVE);

        assertTrue(memberDAO.addMember(member));
        testMemberId = member.getId();

        // 验证 registerDate 被自动设置
        Member added = memberDAO.getMemberById(testMemberId);
        assertNotNull(added.getRegisterDate());
    }

    @Test
    public void testAddMemberInvalidEmail() {
        Member member = new Member();
        member.setName("测试用户3");
        member.setPhone("13912345680");
        member.setEmail("invalid-email");  // 无效邮箱
        member.setGender(MemberDAO.GENDER_MALE);
        member.setBirthDate(DateUtils.now());
        member.setRegisterDate(DateUtils.now());
        member.setStatus(MemberDAO.STATUS_ACTIVE);

        assertFalse("无效邮箱应该添加失败", memberDAO.addMember(member));
    }

    @Test
    public void testAddMemberInvalidPhone() {
        Member member = new Member();
        member.setName("测试用户4");
        member.setPhone("123");  // 无效手机号
        member.setEmail("test4@email.com");
        member.setGender(MemberDAO.GENDER_MALE);
        member.setBirthDate(DateUtils.now());
        member.setRegisterDate(DateUtils.now());
        member.setStatus(MemberDAO.STATUS_ACTIVE);

        assertFalse("无效手机号应该添加失败", memberDAO.addMember(member));
    }

    @Test
    public void testAddMemberInvalidStatus() {
        Member member = new Member();
        member.setName("测试用户5");
        member.setPhone("13912345681");
        member.setEmail("test5@email.com");
        member.setGender(MemberDAO.GENDER_MALE);
        member.setBirthDate(DateUtils.now());
        member.setRegisterDate(DateUtils.now());
        member.setStatus("invalid_status");  // 无效状态

        assertFalse("无效状态应该添加失败", memberDAO.addMember(member));
    }

    @Test
    public void testAddMemberInvalidGender() {
        Member member = new Member();
        member.setName("测试用户6");
        member.setPhone("13912345682");
        member.setEmail("test6@email.com");
        member.setGender("invalid_gender");  // 无效性别
        member.setBirthDate(DateUtils.now());
        member.setRegisterDate(DateUtils.now());
        member.setStatus(MemberDAO.STATUS_ACTIVE);

        assertFalse("无效性别应该添加失败", memberDAO.addMember(member));
    }

    // ==================== 更新会员测试 ====================

    @Test
    public void testUpdateMember() {
        Member member = memberDAO.getMemberById(1);
        assertNotNull(member);

        String originalName = member.getName();
        member.setName("张三Updated");

        assertTrue(memberDAO.updateMember(member));

        Member updated = memberDAO.getMemberById(1);
        assertEquals("张三Updated", updated.getName());

        // 恢复原始数据
        member.setName(originalName);
        memberDAO.updateMember(member);
    }

    @Test
    public void testUpdateMemberStatus() {
        Member member = memberDAO.getMemberById(1);
        assertNotNull(member);

        String originalStatus = member.getStatus();

        assertTrue(memberDAO.updateMemberStatus(1, MemberDAO.STATUS_FROZEN));

        Member updated = memberDAO.getMemberById(1);
        assertEquals(MemberDAO.STATUS_FROZEN, updated.getStatus());

        // 恢复原始状态
        memberDAO.updateMemberStatus(1, originalStatus);
    }

    @Test
    public void testUpdateMemberStatusInvalid() {
        assertFalse("无效状态应该更新失败", memberDAO.updateMemberStatus(1, "invalid"));
    }

    @Test
    public void testFreezeMember() {
        Member member = memberDAO.getMemberById(1);
        String originalStatus = member.getStatus();

        assertTrue(memberDAO.freezeMember(1));

        Member frozen = memberDAO.getMemberById(1);
        assertEquals(MemberDAO.STATUS_FROZEN, frozen.getStatus());

        // 恢复
        memberDAO.updateMemberStatus(1, originalStatus);
    }

    @Test
    public void testActivateMember() {
        // 先冻结
        memberDAO.freezeMember(1);

        assertTrue(memberDAO.activateMember(1));

        Member activated = memberDAO.getMemberById(1);
        assertEquals(MemberDAO.STATUS_ACTIVE, activated.getStatus());
    }

    // ==================== 删除会员测试 ====================

    @Test
    public void testDeleteMemberById() {
        // 先添加测试数据
        Member member = new Member();
        member.setName("待删除用户");
        member.setPhone("13900099999");
        member.setEmail("delete@email.com");
        member.setGender(MemberDAO.GENDER_MALE);
        member.setBirthDate(DateUtils.now());
        member.setRegisterDate(DateUtils.now());
        member.setStatus(MemberDAO.STATUS_INACTIVE);

        assertTrue(memberDAO.addMember(member));
        int memberId = member.getId();

        // 删除
        assertTrue(memberDAO.deleteMember(memberId));

        // 验证已删除
        assertNull(memberDAO.getMemberById(memberId));

        testMemberId = 0;  // 已删除
    }

    @Test
    public void testDeleteMemberByObject() {
        // 先添加测试数据
        Member member = new Member();
        member.setName("待删除用户2");
        member.setPhone("13900099998");
        member.setEmail("delete2@email.com");
        member.setGender(MemberDAO.GENDER_FEMALE);
        member.setBirthDate(DateUtils.now());
        member.setRegisterDate(DateUtils.now());
        member.setStatus(MemberDAO.STATUS_INACTIVE);

        assertTrue(memberDAO.addMember(member));

        // 删除
        assertTrue(memberDAO.deleteMember(member));

        // 验证已删除
        assertNull(memberDAO.getMemberById(member.getId()));

        testMemberId = 0;
    }

    @Test
    public void testDeleteMemberNotFound() {
        assertFalse(memberDAO.deleteMember(99999));
    }

    // ==================== 统计功能测试 ====================

    @Test
    public void testGetTotalMemberCount() {
        int count = memberDAO.getTotalMemberCount();
        assertTrue(count > 0);

        // 验证与列表大小一致
        List<Member> members = memberDAO.getAllMembers();
        assertEquals(members.size(), count);
    }

    @Test
    public void testGetMemberCountByStatus() {
        Map<String, Integer> countMap = memberDAO.getMemberCountByStatus();
        assertNotNull(countMap);

        // 应该包含所有状态
        for (String status : MemberDAO.VALID_STATUSES) {
            assertTrue(countMap.containsKey(status));
            assertTrue(countMap.get(status) >= 0);
        }

        // 验证总数正确
        int total = 0;
        for (int count : countMap.values()) {
            total += count;
        }
        assertEquals(memberDAO.getTotalMemberCount(), total);
    }

    @Test
    public void testGetMemberCountByGender() {
        Map<String, Integer> countMap = memberDAO.getMemberCountByGender();
        assertNotNull(countMap);

        // 应该包含所有性别
        for (String gender : MemberDAO.VALID_GENDERS) {
            assertTrue(countMap.containsKey(gender));
            assertTrue(countMap.get(gender) >= 0);
        }
    }

    @Test
    public void testGetActiveMemberCount() {
        int count = memberDAO.getActiveMemberCount();
        assertTrue(count >= 0);

        // 验证与列表大小一致
        List<Member> activeMembers = memberDAO.getActiveMembers();
        assertEquals(activeMembers.size(), count);
    }

    @Test
    public void testGetTodayNewMemberCount() {
        int count = memberDAO.getTodayNewMemberCount();
        assertTrue(count >= 0);
    }

    @Test
    public void testGetMonthlyNewMemberCount() {
        int count = memberDAO.getMonthlyNewMemberCount();
        assertTrue(count >= 0);
    }

    // ==================== 年龄相关测试 ====================

    @Test
    public void testGetMemberAge() {
        int age = memberDAO.getMemberAge(1);
        assertTrue(age > 0);  // 会员应该有年龄

        // 验证计算正确
        Member member = memberDAO.getMemberById(1);
        assertEquals(DateUtils.calculateAge(member.getBirthDate()), age);
    }

    @Test
    public void testGetMemberAgeNotFound() {
        int age = memberDAO.getMemberAge(99999);
        assertEquals(-1, age);
    }

    @Test
    public void testGetMembershipDays() {
        long days = memberDAO.getMembershipDays(1);
        assertTrue(days >= 0);  // 会员应该有会籍天数
    }

    @Test
    public void testGetMembershipDaysNotFound() {
        long days = memberDAO.getMembershipDays(99999);
        assertEquals(-1, days);
    }

    // ==================== 校验方法测试 ====================

    @Test
    public void testIsValidEmail() {
        assertTrue(memberDAO.isValidEmail("test@email.com"));
        assertTrue(memberDAO.isValidEmail("user.name@domain.org"));
        assertFalse(memberDAO.isValidEmail("invalid"));
        assertFalse(memberDAO.isValidEmail("@domain.com"));
        assertFalse(memberDAO.isValidEmail(null));
    }

    @Test
    public void testIsValidPhone() {
        assertTrue(memberDAO.isValidPhone("13912345678"));
        assertTrue(memberDAO.isValidPhone("15012345678"));
        assertFalse(memberDAO.isValidPhone("123"));
        assertFalse(memberDAO.isValidPhone("12345678901"));  // 不是1开头
        assertFalse(memberDAO.isValidPhone(null));
    }

    @Test
    public void testIsValidStatus() {
        assertTrue(memberDAO.isValidStatus(MemberDAO.STATUS_ACTIVE));
        assertTrue(memberDAO.isValidStatus(MemberDAO.STATUS_FROZEN));
        assertTrue(memberDAO.isValidStatus(MemberDAO.STATUS_INACTIVE));
        assertFalse(memberDAO.isValidStatus("invalid"));
        assertFalse(memberDAO.isValidStatus(null));
    }

    @Test
    public void testIsValidGender() {
        assertTrue(memberDAO.isValidGender(MemberDAO.GENDER_MALE));
        assertTrue(memberDAO.isValidGender(MemberDAO.GENDER_FEMALE));
        assertFalse(memberDAO.isValidGender("invalid"));
        assertFalse(memberDAO.isValidGender(null));
    }

    @Test
    public void testIsPhoneExists() {
        assertTrue(memberDAO.isPhoneExists("13900001111"));  // 张三的手机号
        assertFalse(memberDAO.isPhoneExists("99999999999"));
    }

    @Test
    public void testIsEmailExists() {
        // 测试已存在的邮箱
        Member member = memberDAO.getMemberById(1);
        if (member != null && member.getEmail() != null) {
            assertTrue(memberDAO.isEmailExists(member.getEmail()));
        }
        assertFalse(memberDAO.isEmailExists("notexist@email.com"));
    }

    // ==================== 工具方法测试 ====================

    @Test
    public void testGetStatusDisplayName() {
        assertEquals("活跃", memberDAO.getStatusDisplayName(MemberDAO.STATUS_ACTIVE));
        assertEquals("冻结", memberDAO.getStatusDisplayName(MemberDAO.STATUS_FROZEN));
        assertEquals("停用", memberDAO.getStatusDisplayName(MemberDAO.STATUS_INACTIVE));
        assertEquals("未知", memberDAO.getStatusDisplayName("invalid"));
        assertEquals("未知", memberDAO.getStatusDisplayName(null));
    }

    @Test
    public void testGetGenderDisplayName() {
        assertEquals("男", memberDAO.getGenderDisplayName(MemberDAO.GENDER_MALE));
        assertEquals("女", memberDAO.getGenderDisplayName(MemberDAO.GENDER_FEMALE));
        assertEquals("未知", memberDAO.getGenderDisplayName("invalid"));
        assertEquals("未知", memberDAO.getGenderDisplayName(null));
    }

    // ==================== 常量测试 ====================

    @Test
    public void testStatusConstants() {
        assertEquals("active", MemberDAO.STATUS_ACTIVE);
        assertEquals("frozen", MemberDAO.STATUS_FROZEN);
        assertEquals("inactive", MemberDAO.STATUS_INACTIVE);
    }

    @Test
    public void testGenderConstants() {
        assertEquals("male", MemberDAO.GENDER_MALE);
        assertEquals("female", MemberDAO.GENDER_FEMALE);
    }

    @Test
    public void testValidStatusesArray() {
        assertEquals(3, MemberDAO.VALID_STATUSES.length);
    }

    @Test
    public void testValidGendersArray() {
        assertEquals(2, MemberDAO.VALID_GENDERS.length);
    }

    // ==================== 兼容旧方法名测试 ====================

    @Test
    @SuppressWarnings("deprecation")
    public void testDeprecatedMethods() {
        // 测试旧方法名是否仍然可用
        List<Member> members1 = memberDAO.getMemberByName("张三");
        List<Member> members2 = memberDAO.getMembersByName("张三");
        assertEquals(members1.size(), members2.size());

        List<Member> members3 = memberDAO.getMemberByGender(MemberDAO.GENDER_MALE);
        List<Member> members4 = memberDAO.getMembersByGender(MemberDAO.GENDER_MALE);
        assertEquals(members3.size(), members4.size());

        List<Member> members5 = memberDAO.getMemberByStatus(MemberDAO.STATUS_ACTIVE);
        List<Member> members6 = memberDAO.getMembersByStatus(MemberDAO.STATUS_ACTIVE);
        assertEquals(members5.size(), members6.size());
    }

    // ==================== 实体类测试 ====================

    @Test
    public void testMemberRole() {
        Member member = memberDAO.getMemberById(1);
        assertNotNull(member);
        assertEquals("Member", member.getRole());
    }

    @Test
    public void testMemberBasicInfo() {
        Member member = memberDAO.getMemberById(1);
        assertNotNull(member);

        String basicInfo = member.getBasicInfo();
        assertNotNull(basicInfo);
        assertTrue(basicInfo.contains("张三"));
        assertTrue(basicInfo.contains("13900001111"));
    }
}
