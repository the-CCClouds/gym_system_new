package utils;

import entity.Employee;
import service.EmployeeService;
import service.UserService;

/**
 * 这是一个临时工具类，用来生成测试用的员工账号。
 * 运行一次后就可以删掉，或者留着以后生成测试数据。
 */
public class DataInit {
    public static void main(String[] args) {
        System.out.println("开始生成测试员工账号...");

        EmployeeService employeeService = new EmployeeService();
        UserService userService = new UserService();

        // 1. 生成一个管理员 (Admin)
        // 姓名: 管理员, 手机: 13800000001, 角色: Admin (ID=3)
        System.out.println("正在创建管理员...");
        EmployeeService.ServiceResult<Employee> adminResult =
                employeeService.hireAdmin("管理员01", "13800000001");

        if (adminResult.isSuccess()) {
            int adminId = adminResult.getData().getId();
            // 为这个管理员注册登录账号: 用户名=admin, 密码=123456
            UserService.ServiceResult<Void> userResult =
                    userService.registerEmployeeUser(adminId, "admin", "123456");

            if (userResult.isSuccess()) {
                System.out.println("✅ 管理员账号生成成功！");
                System.out.println("   用户名: admin");
                System.out.println("   密码:   123456");
            } else {
                System.out.println("❌ 管理员账号注册失败: " + userResult.getMessage());
            }
        } else {
            System.out.println("❌ 管理员档案创建失败: " + adminResult.getMessage());
        }

        System.out.println("--------------------------------");

        // 2. 生成一个教练 (Trainer)
        // 姓名: 强森, 手机: 13800000002, 角色: Trainer (ID=1)
        System.out.println("正在创建教练...");
        EmployeeService.ServiceResult<Employee> trainerResult =
                employeeService.hireTrainer("强森教练", "13800000002");

        if (trainerResult.isSuccess()) {
            int trainerId = trainerResult.getData().getId();
            // 为这个教练注册登录账号: 用户名=trainer, 密码=123456
            UserService.ServiceResult<Void> userResult =
                    userService.registerEmployeeUser(trainerId, "trainer", "123456");

            if (userResult.isSuccess()) {
                System.out.println("✅ 教练账号生成成功！");
                System.out.println("   用户名: trainer");
                System.out.println("   密码:   123456");
            } else {
                System.out.println("❌ 教练账号注册失败: " + userResult.getMessage());
            }
        } else {
            System.out.println("❌ 教练档案创建失败: " + trainerResult.getMessage());
        }
    }
}