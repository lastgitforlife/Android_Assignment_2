package com.example.assignment_2;

public class BloodPressure {
    String userId;
    String systolic;
    String diastolic;
    String date;
    String time;

    public BloodPressure() {}

    public BloodPressure(String userId, String systolic,
                         String diastolic, String date, String time) {
        this.userId = userId;
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.date = date;
        this.time = time;
    }

    public String getUserId() { return userId; }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSystolic() {return systolic;}

    public void setSystolic(String systolic) {
        this.systolic = systolic;
    }

    public String getDiastolic() {return diastolic;}

    public void setDiastolic(String diastolic) {
        this.diastolic = diastolic;
    }
}
