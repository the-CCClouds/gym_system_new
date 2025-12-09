package entity;

import java.util.Date;

public class Member implements Person {
    private int id;
    private String name;
    private String phone;
    private String email;
    private String gender;
    private Date birthDate;
    private Date registerDate;
    private String status;//active/frozen/inactive

    public Member() {
    }

    public Member(int id, String name, String phone, String email, String gender, Date birthDate, Date registerDate, String status) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.gender = gender;
        this.birthDate = birthDate;
        this.registerDate = registerDate;
        this.status = status;//active/frozen/inactive
    }

    public Member( String name, String phone, String email, String gender, Date birthDate, Date registerDate, String status) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.gender = gender;
        this.birthDate = birthDate;
        this.registerDate = registerDate;
        this.status = status;//active/frozen/inactive
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Date getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }



    public String getRole(){
        return "Member";
    }


    @Override
    public String getBasicInfo() {
        return id+" - "+name+" - "+phone+" - "+status+" - " + gender;
    }


    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", gender='" + gender + '\'' +
                ", birthDate=" + birthDate +
                ", registerDate=" + registerDate +
                ", status='" + status + '\'' +
                '}';
    }

}
