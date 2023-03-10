package com.example.warehousemanage;

public class User {
    private String age,email,fullname,isusersof,phone;

    public User(){}

    public User(String age, String email, String fullname, String isusersof, String phone) {
        this.age = age;
        this.email = email;
        this.fullname = fullname;
        this.isusersof = isusersof;
        this.phone = phone;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getIsusersof() {
        return isusersof;
    }

    public void setIsusersof(String isusersof) {
        this.isusersof = isusersof;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
