package entity;

import java.util.Date;

public class Employee implements entity.Person {
    private int employeeId;
    private String name;
    private int roleId;  // 外键关联 employee_role 表
    private entity.EmployeeRole employeeRole;  // 关联的角色对象
    private String phone;
    private Date hireDate; // 入职日期

    public Employee() {
    }

    public Employee(int employeeId, String name, int roleId, String phone, Date hireDate) {
        this.employeeId = employeeId;
        this.name = name;
        this.roleId = roleId;
        this.phone = phone;
        this.hireDate = hireDate;
    }

    public Employee(int employeeId, String name, entity.EmployeeRole employeeRole, String phone, Date hireDate) {
        this.employeeId = employeeId;
        this.name = name;
        this.employeeRole = employeeRole;
        this.roleId = employeeRole != null ? employeeRole.getRoleId() : 0;
        this.phone = phone;
        this.hireDate = hireDate;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    @Override
    public int getId() {
        return employeeId;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public entity.EmployeeRole getEmployeeRole() {
        return employeeRole;
    }

    public void setEmployeeRole(entity.EmployeeRole employeeRole) {
        this.employeeRole = employeeRole;
        if (employeeRole != null) {
            this.roleId = employeeRole.getRoleId();
        }
    }

    @Override
    public String getRole() {
        // 优先返回关联对象的角色名称
        if (employeeRole != null) {
            return employeeRole.getRoleName();
        }
        // 根据 roleId 返回默认角色名称
        switch (roleId) {
            case 1: return "Trainer";
            case 2: return "Receptionist";
            case 3: return "Admin";
            default: return "Unknown";
        }
    }

    @Override
    public String getBasicInfo() {
        return employeeId + " - " + name + " - " + getRole() + " - " + phone;
    }

    @Override
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "employeeId=" + employeeId +
                ", name='" + name + '\'' +
                ", roleId=" + roleId +
                ", role='" + getRole() + '\'' +
                ", phone='" + phone + '\'' +
                ", hireDate=" + hireDate +
                '}';
    }

}
