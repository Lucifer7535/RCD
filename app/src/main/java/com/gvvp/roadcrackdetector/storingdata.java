package com.gvvp.roadcrackdetector;

public class storingdata {
    String fullname, username, empno, email, phoneno, dob;

    public storingdata() {
    }

    public storingdata(String fullname, String username, String empno, String email, String phoneno, String dob) {
        this.fullname = fullname;
        this.username = username;
        this.empno = empno;
        this.email = email;
        this.phoneno = phoneno;
        this.dob = dob;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneno() {
        return phoneno;
    }

    public void setPhoneno(String phoneno) {
        this.phoneno = phoneno;
    }

    public String getEmpno() {return empno;}

    public void setEmpno(String empno) {this.empno = empno;}

    public String getDob() {return dob;}

    public void setDob(String dob) {this.dob = dob;}

}
