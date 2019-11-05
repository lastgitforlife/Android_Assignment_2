package com.example.assignment_2;

public class BloodPressure {
    String userId;
    String Systolic;
    String Diastolic;
    String school;

    public BloodPressure() {}

    public BloodPressure(String userId, String Systolic,
                         String Diastolic, String school) {
        this.userId = userId;
        this.Systolic = Systolic;
        this.Diastolic = Diastolic;
        this.school = school;
    }

    public String getUserId() { return userId; }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSystolic() {return Systolic;}

    public void setSystolic(String systolic) {
        this.Systolic = systolic;
    }

    public String getDiastolic() {return Diastolic;}

    public void setDiastolic(String diastolic) {
        this.Diastolic = diastolic;
    }

    public String getSchool() {return school;}

    public void setSchool(String school) {
        this.school = school;
    }
}
