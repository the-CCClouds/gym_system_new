package service;

import dao.BookingDAO;
import dao.CourseDAO;
import dao.EmployeeDAO;
import dao.EmployeeRoleDAO;
import entity.Booking;
import entity.Course;
import entity.Employee;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 课程服务类
 * 提供课程相关的业务逻辑处理
 *
 * 主要功能：
 * - 课程创建与管理
 * - 课程信息修改
 * - 课程查询（综合查询、按类型、按教练）
 * - 课程容量管理
 * - 课程统计报表
 * - 教练课程分配
 *
 * @author GymSystem
 * @version 1.0
 */
public class CourseService {

    // ==================== 依赖的DAO ====================

    private CourseDAO courseDAO;
    private BookingDAO bookingDAO;
    private EmployeeDAO employeeDAO;

    // ==================== 课程类型常量（引用DAO） ====================

    /** 课程类型：瑜伽 */
    public static final String TYPE_YOGA = CourseDAO.TYPE_YOGA;
    /** 课程类型：动感单车 */
    public static final String TYPE_SPINNING = CourseDAO.TYPE_SPINNING;
    /** 课程类型：普拉提 */
    public static final String TYPE_PILATES = CourseDAO.TYPE_PILATES;
    /** 课程类型：有氧操 */
    public static final String TYPE_AEROBICS = CourseDAO.TYPE_AEROBICS;
    /** 课程类型：力量训练 */
    public static final String TYPE_STRENGTH = CourseDAO.TYPE_STRENGTH;
    /** 课程类型：其他 */
    public static final String TYPE_OTHER = CourseDAO.TYPE_OTHER;

    // ==================== 构造方法 ====================

    public CourseService() {
        this.courseDAO = new CourseDAO();
        this.bookingDAO = new BookingDAO();
        this.employeeDAO = new EmployeeDAO();
    }

    // ==================== 课程创建 ====================

    /**
     * 创建课程
     *
     * @param name        课程名称
     * @param type        课程类型（yoga/spinning/pilates/aerobics/strength/other）
     * @param duration    时长（分钟）
     * @param maxCapacity 最大容量
     * @param trainerId   教练ID
     * @return 创建结果，包含成功/失败信息和课程对象
     */
    public ServiceResult<Course> createCourse(String name, String type, int duration,
                                              int maxCapacity, int trainerId, Date courseTime) {
        // 参数校验
        if (name == null || name.trim().isEmpty()) {
            return ServiceResult.failure("创建失败：课程名称不能为空");
        }
        if (!courseDAO.isValidType(type)) {
            return ServiceResult.failure("创建失败：无效的课程类型，有效类型为：yoga, spinning, pilates, aerobics, strength, other");
        }
        if (duration <= 0) {
            return ServiceResult.failure("创建失败：课程时长必须大于0");
        }
        if (maxCapacity <= 0) {
            return ServiceResult.failure("创建失败：最大容量必须大于0");
        }

        if (courseTime == null) {
            return ServiceResult.failure("创建失败：上课时间不能为空");
        }
        // 验证教练
        Employee trainer = employeeDAO.getEmployeeById(trainerId);
        if (trainer == null) {
            return ServiceResult.failure("创建失败：教练不存在");
        }
        if (trainer.getRoleId() != EmployeeRoleDAO.ROLE_ID_TRAINER) {
            return ServiceResult.failure("创建失败：指定的员工不是教练");
        }

        if (courseTime == null) {
            return ServiceResult.failure("创建失败：上课时间不能为空");
        }

        // 创建课程对象
        Course course = new Course();
        course.setName(name.trim());
        course.setType(type);
        course.setDuration(duration);
        course.setMaxCapacity(maxCapacity);
        course.setEmployeeId(trainerId);
        course.setCourseTime(courseTime);
        // 保存到数据库
        if (courseDAO.addCourse(course)) {
            String typeName = courseDAO.getTypeDisplayName(type);
            return ServiceResult.success("课程创建成功，类型：" + typeName, course);
        } else {
            return ServiceResult.failure("创建失败：数据库操作失败");
        }
    }

    /**
     * 创建瑜伽课程
     */
    public ServiceResult<Course> createYogaCourse(String name, String type, int duration,
                                                  int maxCapacity, int trainerId, Date courseTime) {
        return createCourse(name, TYPE_YOGA, duration, maxCapacity, trainerId, courseTime);
    }

    /**
     * 创建动感单车课程
     */
    public ServiceResult<Course> createSpinningCourse(String name, int duration, int maxCapacity, int trainerId,Date courseTime) {
        return createCourse(name, TYPE_SPINNING, duration, maxCapacity, trainerId,courseTime);
    }

    /**
     * 创建普拉提课程
     */
    public ServiceResult<Course> createPilatesCourse(String name, int duration, int maxCapacity, int trainerId, Date courseTime) {
        return createCourse(name, TYPE_PILATES, duration, maxCapacity, trainerId,courseTime);
    }

    /**
     * 创建有氧操课程
     */
    public ServiceResult<Course> createAerobicsCourse(String name, int duration, int maxCapacity, int trainerId,Date courseTime) {
        return createCourse(name, TYPE_AEROBICS, duration, maxCapacity, trainerId,courseTime);
    }

    /**
     * 创建力量训练课程
     */
    public ServiceResult<Course> createStrengthCourse(String name, int duration, int maxCapacity, int trainerId,Date courseTime) {
        return createCourse(name, TYPE_STRENGTH, duration, maxCapacity, trainerId,courseTime);
    }

    // ==================== 课程信息管理 ====================

    // ==================== 补充：通用课程更新 ====================

    /**
     * 更新课程所有信息 (用于编辑模式)
     * @param course 包含最新信息的课程对象
     * @return 是否更新成功
     */
    public boolean updateCourse(Course course) {
        if (course == null) {
            return false;
        }
        // 调用 DAO 层进行全字段更新
        return courseDAO.updateCourse(course);
    }


    /**
     * 更新课程基本信息
     *
     * @param courseId    课程ID
     * @param name        课程名称
     * @param type        课程类型
     * @param duration    时长（分钟）
     * @param maxCapacity 最大容量
     * @return 更新结果
     */
    public ServiceResult<Course> updateCourseInfo(int courseId, String name, String type,
                                                   int duration, int maxCapacity) {
        // 查询课程是否存在
        Course course = courseDAO.getCourseById(courseId);
        if (course == null) {
            return ServiceResult.failure("更新失败：课程不存在");
        }

        // 参数校验
        if (name == null || name.trim().isEmpty()) {
            return ServiceResult.failure("更新失败：课程名称不能为空");
        }
        if (!courseDAO.isValidType(type)) {
            return ServiceResult.failure("更新失败：无效的课程类型");
        }
        if (duration <= 0) {
            return ServiceResult.failure("更新失败：课程时长必须大于0");
        }
        if (maxCapacity <= 0) {
            return ServiceResult.failure("更新失败：最大容量必须大于0");
        }

        // 检查新容量是否小于已确认预约数
        int confirmedCount = courseDAO.getConfirmedBookingCount(courseId);
        if (maxCapacity < confirmedCount) {
            return ServiceResult.failure("更新失败：新容量（" + maxCapacity + "）不能小于已确认预约数（" + confirmedCount + "）");
        }

        // 更新信息
        course.setName(name.trim());
        course.setType(type);
        course.setDuration(duration);
        course.setMaxCapacity(maxCapacity);

        if (courseDAO.updateCourse(course)) {
            return ServiceResult.success("更新成功", course);
        } else {
            return ServiceResult.failure("更新失败：数据库操作失败");
        }
    }

    /**
     * 更新课程名称
     *
     * @param courseId 课程ID
     * @param newName  新名称
     * @return 更新结果
     */
    public ServiceResult<Course> updateCourseName(int courseId, String newName) {
        Course course = courseDAO.getCourseById(courseId);
        if (course == null) {
            return ServiceResult.failure("更新失败：课程不存在");
        }

        if (newName == null || newName.trim().isEmpty()) {
            return ServiceResult.failure("更新失败：课程名称不能为空");
        }

        course.setName(newName.trim());

        if (courseDAO.updateCourse(course)) {
            return ServiceResult.success("课程名称更新成功", course);
        } else {
            return ServiceResult.failure("更新失败：数据库操作失败");
        }
    }

    /**
     * 更新课程时长
     *
     * @param courseId    课程ID
     * @param newDuration 新时长（分钟）
     * @return 更新结果
     */
    public ServiceResult<Course> updateCourseDuration(int courseId, int newDuration) {
        Course course = courseDAO.getCourseById(courseId);
        if (course == null) {
            return ServiceResult.failure("更新失败：课程不存在");
        }

        if (newDuration <= 0) {
            return ServiceResult.failure("更新失败：课程时长必须大于0");
        }

        course.setDuration(newDuration);

        if (courseDAO.updateCourse(course)) {
            return ServiceResult.success("课程时长更新成功", course);
        } else {
            return ServiceResult.failure("更新失败：数据库操作失败");
        }
    }

    /**
     * 更新课程容量
     *
     * @param courseId    课程ID
     * @param newCapacity 新容量
     * @return 更新结果
     */
    public ServiceResult<Course> updateCourseCapacity(int courseId, int newCapacity) {
        Course course = courseDAO.getCourseById(courseId);
        if (course == null) {
            return ServiceResult.failure("更新失败：课程不存在");
        }

        if (newCapacity <= 0) {
            return ServiceResult.failure("更新失败：最大容量必须大于0");
        }

        // 检查新容量是否小于已确认预约数
        int confirmedCount = courseDAO.getConfirmedBookingCount(courseId);
        if (newCapacity < confirmedCount) {
            return ServiceResult.failure("更新失败：新容量（" + newCapacity + "）不能小于已确认预约数（" + confirmedCount + "）");
        }

        course.setMaxCapacity(newCapacity);

        if (courseDAO.updateCourse(course)) {
            return ServiceResult.success("课程容量更新成功", course);
        } else {
            return ServiceResult.failure("更新失败：数据库操作失败");
        }
    }

    // ==================== 教练分配 ====================

    /**
     * 更换课程教练
     *
     * @param courseId     课程ID
     * @param newTrainerId 新教练ID
     * @return 更新结果
     */
    public ServiceResult<Course> changeTrainer(int courseId, int newTrainerId) {
        Course course = courseDAO.getCourseById(courseId);
        if (course == null) {
            return ServiceResult.failure("操作失败：课程不存在");
        }

        // 验证新教练
        Employee newTrainer = employeeDAO.getEmployeeById(newTrainerId);
        if (newTrainer == null) {
            return ServiceResult.failure("操作失败：教练不存在");
        }
        if (newTrainer.getRoleId() != EmployeeRoleDAO.ROLE_ID_TRAINER) {
            return ServiceResult.failure("操作失败：指定的员工不是教练");
        }

        if (course.getEmployeeId() == newTrainerId) {
            return ServiceResult.failure("操作失败：该教练已是本课程的教练");
        }

        // 获取原教练信息
        Employee oldTrainer = employeeDAO.getEmployeeById(course.getEmployeeId());
        String oldTrainerName = oldTrainer != null ? oldTrainer.getName() : "未知";

        course.setEmployeeId(newTrainerId);

        if (courseDAO.updateCourse(course)) {
            return ServiceResult.success("教练更换成功：" + oldTrainerName + " → " + newTrainer.getName(), course);
        } else {
            return ServiceResult.failure("操作失败：数据库操作失败");
        }
    }

    // ==================== 课程删除 ====================

    /**
     * 删除课程
     *
     * @param courseId 课程ID
     * @param force    是否强制删除（忽略关联预约检查）
     * @return 操作结果
     */
    public ServiceResult<Void> deleteCourse(int courseId, boolean force) {
        Course course = courseDAO.getCourseById(courseId);
        if (course == null) {
            return ServiceResult.failure("操作失败：课程不存在");
        }

        if (!force) {
            // 检查是否有关联的预约
            List<Booking> bookings = bookingDAO.getBookingsByCourseId(courseId);
            if (!bookings.isEmpty()) {
                // 检查是否有未完成的预约
                long activeCount = bookings.stream()
                        .filter(b -> BookingDAO.STATUS_PENDING.equals(b.getBookingStatus()) ||
                                    BookingDAO.STATUS_CONFIRMED.equals(b.getBookingStatus()))
                        .count();
                if (activeCount > 0) {
                    return ServiceResult.failure("操作失败：课程有" + activeCount + "个未完成的预约，请先取消这些预约");
                }
            }
        }

        if (courseDAO.deleteCourse(courseId)) {
            return ServiceResult.success("课程「" + course.getName() + "」已删除");
        } else {
            return ServiceResult.failure("操作失败：数据库操作失败，可能存在关联数据");
        }
    }

    /**
     * 删除课程（非强制）
     */
    public ServiceResult<Void> deleteCourse(int courseId) {
        return deleteCourse(courseId, false);
    }

    // ==================== 课程查询 ====================

    /**
     * 根据ID查询课程
     *
     * @param courseId 课程ID
     * @return 课程对象
     */
    public Course getCourseById(int courseId) {
        return courseDAO.getCourseById(courseId);
    }

    /**
     * 查询所有课程
     *
     * @return 课程列表
     */
    public List<Course> getAllCourses() {
        return courseDAO.getAllCourses();
    }

    /**
     * 根据名称模糊搜索课程
     *
     * @param name 课程名称关键字
     * @return 课程列表
     */
    public List<Course> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return courseDAO.getAllCourses();
        }
        return courseDAO.searchCourseByName(name.trim());
    }

    /**
     * 根据类型查询课程
     *
     * @param type 课程类型
     * @return 课程列表
     */
    public List<Course> getCoursesByType(String type) {
        return courseDAO.getCoursesByType(type);
    }

    /**
     * 根据教练ID查询课程
     *
     * @param trainerId 教练ID
     * @return 课程列表
     */
    public List<Course> getCoursesByTrainer(int trainerId) {
        return courseDAO.getCoursesByEmployeeId(trainerId);
    }

    /**
     * 根据时长范围查询课程
     *
     * @param minDuration 最小时长（分钟）
     * @param maxDuration 最大时长（分钟）
     * @return 课程列表
     */
    public List<Course> getCoursesByDurationRange(int minDuration, int maxDuration) {
        return courseDAO.getCoursesByDurationRange(minDuration, maxDuration);
    }

    /**
     * 获取有空位的课程
     *
     * @return 课程列表
     */
    public List<Course> getAvailableCourses() {
        return courseDAO.getAvailableCourses();
    }

    /**
     * 获取已满的课程
     *
     * @return 课程列表
     */
    public List<Course> getFullCourses() {
        List<Course> allCourses = courseDAO.getAllCourses();
        List<Course> fullCourses = new ArrayList<>();
        for (Course course : allCourses) {
            if (courseDAO.isFull(course.getCourseId())) {
                fullCourses.add(course);
            }
        }
        return fullCourses;
    }

    /**
     * 综合搜索课程
     *
     * @param keyword 搜索关键字
     * @return 课程列表
     */
    public List<Course> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return courseDAO.getAllCourses();
        }

        keyword = keyword.trim().toLowerCase();

        // 如果是课程类型，按类型搜索
        if (courseDAO.isValidType(keyword)) {
            return courseDAO.getCoursesByType(keyword);
        }

        // 否则按名称搜索
        return courseDAO.searchCourseByName(keyword);
    }

    // ==================== 课程详情 ====================

    /**
     * 获取课程详细信息（包含教练、预约统计等）
     *
     * @param courseId 课程ID
     * @return 课程详情
     */
    public CourseDetail getCourseDetail(int courseId) {
        Course course = courseDAO.getCourseById(courseId);
        if (course == null) {
            return null;
        }

        CourseDetail detail = new CourseDetail();
        detail.setCourse(course);

        // 类型信息
        detail.setTypeDisplayName(courseDAO.getTypeDisplayName(course.getType()));
        detail.setDurationFormatted(courseDAO.formatDuration(course.getDuration()));

        // 教练信息
        Employee trainer = employeeDAO.getEmployeeById(course.getEmployeeId());
        detail.setTrainer(trainer);
        detail.setTrainerName(trainer != null ? trainer.getName() : "未知");

        // 预约统计
        int confirmedCount = courseDAO.getConfirmedBookingCount(courseId);
        int availableSlots = course.getMaxCapacity() - confirmedCount;
        detail.setConfirmedBookingCount(confirmedCount);
        detail.setAvailableSlots(availableSlots);
        detail.setFull(availableSlots <= 0);

        // 预约列表
        List<Booking> bookings = bookingDAO.getBookingsByCourseId(courseId);
        detail.setTotalBookingCount(bookings.size());

        // 统计各状态预约数
        int pendingCount = 0;
        int cancelledCount = 0;
        for (Booking booking : bookings) {
            if (BookingDAO.STATUS_PENDING.equals(booking.getBookingStatus())) {
                pendingCount++;
            } else if (BookingDAO.STATUS_CANCELLED.equals(booking.getBookingStatus())) {
                cancelledCount++;
            }
        }
        detail.setPendingBookingCount(pendingCount);
        detail.setCancelledBookingCount(cancelledCount);

        return detail;
    }

    // ==================== 课程容量管理 ====================

    /**
     * 获取课程剩余名额
     *
     * @param courseId 课程ID
     * @return 剩余名额，-1表示课程不存在
     */
    public int getAvailableSlots(int courseId) {
        return courseDAO.getAvailableSlots(courseId);
    }

    /**
     * 检查课程是否已满
     *
     * @param courseId 课程ID
     * @return true表示已满
     */
    public boolean isFull(int courseId) {
        return courseDAO.isFull(courseId);
    }

    /**
     * 获取课程已确认预约数
     *
     * @param courseId 课程ID
     * @return 已确认预约数
     */
    public int getConfirmedBookingCount(int courseId) {
        return courseDAO.getConfirmedBookingCount(courseId);
    }

    /**
     * 验证课程是否可以预约
     *
     * @param courseId 课程ID
     * @return 验证结果
     */
    public ServiceResult<Course> validateCourseAvailable(int courseId) {
        Course course = courseDAO.getCourseById(courseId);
        if (course == null) {
            return ServiceResult.failure("课程不存在");
        }

        if (courseDAO.isFull(courseId)) {
            return ServiceResult.failure("课程已满，无法预约");
        }

        return ServiceResult.success("课程可以预约", course);
    }

    // ==================== 课程统计 ====================

    /**
     * 获取课程总数
     *
     * @return 课程总数
     */
    public int getTotalCourseCount() {
        return courseDAO.getTotalCourseCount();
    }

    /**
     * 按类型统计课程数量
     *
     * @return Map<课程类型, 数量>
     */
    public Map<String, Integer> getCourseCountByType() {
        return courseDAO.getCourseCountByType();
    }

    /**
     * 按教练统计课程数量
     *
     * @return Map<教练ID, 数量>
     */
    public Map<Integer, Integer> getCourseCountByTrainer() {
        return courseDAO.getCourseCountByEmployee();
    }

    /**
     * 获取指定类型的课程数量
     *
     * @param type 课程类型
     * @return 课程数量
     */
    public int getCourseCountByType(String type) {
        return courseDAO.getCourseCountByType(type);
    }

    /**
     * 获取课程统计概览
     *
     * @return 统计概览
     */
    public CourseStatistics getStatistics() {
        CourseStatistics stats = new CourseStatistics();

        stats.setTotalCount(courseDAO.getTotalCourseCount());

        // 按类型统计
        Map<String, Integer> typeCount = courseDAO.getCourseCountByType();
        stats.setYogaCount(typeCount.getOrDefault(TYPE_YOGA, 0));
        stats.setSpinningCount(typeCount.getOrDefault(TYPE_SPINNING, 0));
        stats.setPilatesCount(typeCount.getOrDefault(TYPE_PILATES, 0));
        stats.setAerobicsCount(typeCount.getOrDefault(TYPE_AEROBICS, 0));
        stats.setStrengthCount(typeCount.getOrDefault(TYPE_STRENGTH, 0));
        stats.setOtherCount(typeCount.getOrDefault(TYPE_OTHER, 0));

        // 可用/已满统计
        List<Course> availableCourses = courseDAO.getAvailableCourses();
        stats.setAvailableCount(availableCourses.size());
        stats.setFullCount(stats.getTotalCount() - stats.getAvailableCount());

        // 教练数量
        Map<Integer, Integer> trainerCount = courseDAO.getCourseCountByEmployee();
        stats.setTrainerWithCourseCount(trainerCount.size());

        return stats;
    }

    // ==================== 工具方法 ====================

    /**
     * 获取所有课程类型
     *
     * @return 课程类型列表
     */
    public List<String> getAllCourseTypes() {
        return courseDAO.getAllCourseTypes();
    }

    /**
     * 检查课程类型是否有效
     *
     * @param type 课程类型
     * @return true表示有效
     */
    public boolean isValidType(String type) {
        return courseDAO.isValidType(type);
    }

    /**
     * 获取课程类型的中文名称
     *
     * @param type 课程类型
     * @return 中文名称
     */
    public String getTypeDisplayName(String type) {
        return courseDAO.getTypeDisplayName(type);
    }

    /**
     * 格式化课程时长显示
     *
     * @param duration 时长（分钟）
     * @return 格式化字符串
     */
    public String formatDuration(int duration) {
        return courseDAO.formatDuration(duration);
    }

    /**
     * 检查课程是否存在
     *
     * @param courseId 课程ID
     * @return true表示存在
     */
    public boolean isCourseExists(int courseId) {
        return courseDAO.getCourseById(courseId) != null;
    }

    // ==================== 内部类：服务结果 ====================

    /**
     * 服务操作结果
     *
     * @param <T> 数据类型
     */
    public static class ServiceResult<T> {
        private boolean success;
        private String message;
        private T data;

        private ServiceResult(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public static <T> ServiceResult<T> success(String message, T data) {
            return new ServiceResult<>(true, message, data);
        }

        public static <T> ServiceResult<T> success(String message) {
            return new ServiceResult<>(true, message, null);
        }

        public static <T> ServiceResult<T> failure(String message) {
            return new ServiceResult<>(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public T getData() {
            return data;
        }

        @Override
        public String toString() {
            return (success ? "成功" : "失败") + ": " + message;
        }
    }

    // ==================== 内部类：课程详情 ====================

    /**
     * 课程详细信息（包含关联数据和统计）
     */
    public static class CourseDetail {
        private Course course;
        private String typeDisplayName;
        private String durationFormatted;
        private Employee trainer;
        private String trainerName;
        private int confirmedBookingCount;
        private int pendingBookingCount;
        private int cancelledBookingCount;
        private int totalBookingCount;
        private int availableSlots;
        private boolean isFull;

        // Getters and Setters
        public Course getCourse() {
            return course;
        }

        public void setCourse(Course course) {
            this.course = course;
        }

        public String getTypeDisplayName() {
            return typeDisplayName;
        }

        public void setTypeDisplayName(String typeDisplayName) {
            this.typeDisplayName = typeDisplayName;
        }

        public String getDurationFormatted() {
            return durationFormatted;
        }

        public void setDurationFormatted(String durationFormatted) {
            this.durationFormatted = durationFormatted;
        }

        public Employee getTrainer() {
            return trainer;
        }

        public void setTrainer(Employee trainer) {
            this.trainer = trainer;
        }

        public String getTrainerName() {
            return trainerName;
        }

        public void setTrainerName(String trainerName) {
            this.trainerName = trainerName;
        }

        public int getConfirmedBookingCount() {
            return confirmedBookingCount;
        }

        public void setConfirmedBookingCount(int confirmedBookingCount) {
            this.confirmedBookingCount = confirmedBookingCount;
        }

        public int getPendingBookingCount() {
            return pendingBookingCount;
        }

        public void setPendingBookingCount(int pendingBookingCount) {
            this.pendingBookingCount = pendingBookingCount;
        }

        public int getCancelledBookingCount() {
            return cancelledBookingCount;
        }

        public void setCancelledBookingCount(int cancelledBookingCount) {
            this.cancelledBookingCount = cancelledBookingCount;
        }

        public int getTotalBookingCount() {
            return totalBookingCount;
        }

        public void setTotalBookingCount(int totalBookingCount) {
            this.totalBookingCount = totalBookingCount;
        }

        public int getAvailableSlots() {
            return availableSlots;
        }

        public void setAvailableSlots(int availableSlots) {
            this.availableSlots = availableSlots;
        }

        public boolean isFull() {
            return isFull;
        }

        public void setFull(boolean full) {
            isFull = full;
        }

        /**
         * 获取容量使用率
         */
        public double getCapacityUsageRate() {
            if (course == null || course.getMaxCapacity() == 0) {
                return 0;
            }
            return (double) confirmedBookingCount / course.getMaxCapacity() * 100;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("======== 课程详情 ========\n");
            sb.append("课程名称：").append(course.getName()).append("\n");
            sb.append("课程类型：").append(typeDisplayName).append("\n");
            sb.append("课程时长：").append(durationFormatted).append("\n");
            sb.append("授课教练：").append(trainerName).append("\n");
            sb.append("\n");
            sb.append("======== 容量信息 ========\n");
            sb.append("最大容量：").append(course.getMaxCapacity()).append("人\n");
            sb.append("已确认预约：").append(confirmedBookingCount).append("人\n");
            sb.append("剩余名额：").append(availableSlots).append("人\n");
            sb.append("容量使用率：").append(String.format("%.1f", getCapacityUsageRate())).append("%\n");
            sb.append("状态：").append(isFull ? "已满" : "可预约").append("\n");
            sb.append("\n");
            sb.append("======== 预约统计 ========\n");
            sb.append("总预约数：").append(totalBookingCount).append("\n");
            sb.append("  - 已确认：").append(confirmedBookingCount).append("\n");
            sb.append("  - 待确认：").append(pendingBookingCount).append("\n");
            sb.append("  - 已取消：").append(cancelledBookingCount).append("\n");
            return sb.toString();
        }
    }

    // ==================== 内部类：课程统计 ====================

    /**
     * 课程统计信息
     */
    public static class CourseStatistics {
        private int totalCount;
        private int yogaCount;
        private int spinningCount;
        private int pilatesCount;
        private int aerobicsCount;
        private int strengthCount;
        private int otherCount;
        private int availableCount;
        private int fullCount;
        private int trainerWithCourseCount;

        // Getters and Setters
        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getYogaCount() {
            return yogaCount;
        }

        public void setYogaCount(int yogaCount) {
            this.yogaCount = yogaCount;
        }

        public int getSpinningCount() {
            return spinningCount;
        }

        public void setSpinningCount(int spinningCount) {
            this.spinningCount = spinningCount;
        }

        public int getPilatesCount() {
            return pilatesCount;
        }

        public void setPilatesCount(int pilatesCount) {
            this.pilatesCount = pilatesCount;
        }

        public int getAerobicsCount() {
            return aerobicsCount;
        }

        public void setAerobicsCount(int aerobicsCount) {
            this.aerobicsCount = aerobicsCount;
        }

        public int getStrengthCount() {
            return strengthCount;
        }

        public void setStrengthCount(int strengthCount) {
            this.strengthCount = strengthCount;
        }

        public int getOtherCount() {
            return otherCount;
        }

        public void setOtherCount(int otherCount) {
            this.otherCount = otherCount;
        }

        public int getAvailableCount() {
            return availableCount;
        }

        public void setAvailableCount(int availableCount) {
            this.availableCount = availableCount;
        }

        public int getFullCount() {
            return fullCount;
        }

        public void setFullCount(int fullCount) {
            this.fullCount = fullCount;
        }

        public int getTrainerWithCourseCount() {
            return trainerWithCourseCount;
        }

        public void setTrainerWithCourseCount(int trainerWithCourseCount) {
            this.trainerWithCourseCount = trainerWithCourseCount;
        }

        /**
         * 获取课程可用率
         */
        public double getAvailableRate() {
            return totalCount > 0 ? (double) availableCount / totalCount * 100 : 0;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("======== 课程统计 ========\n");
            sb.append("课程总数：").append(totalCount).append("\n");
            sb.append("\n");
            sb.append("按类型分布：\n");
            sb.append("  - 瑜伽：").append(yogaCount).append("\n");
            sb.append("  - 动感单车：").append(spinningCount).append("\n");
            sb.append("  - 普拉提：").append(pilatesCount).append("\n");
            sb.append("  - 有氧操：").append(aerobicsCount).append("\n");
            sb.append("  - 力量训练：").append(strengthCount).append("\n");
            sb.append("  - 其他：").append(otherCount).append("\n");
            sb.append("\n");
            sb.append("容量状态：\n");
            sb.append("  - 可预约：").append(availableCount)
              .append(" (").append(String.format("%.1f", getAvailableRate())).append("%)\n");
            sb.append("  - 已满：").append(fullCount).append("\n");
            sb.append("\n");
            sb.append("授课教练数：").append(trainerWithCourseCount).append("\n");
            return sb.toString();
        }
    }
}

