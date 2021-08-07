package com.starsky.backend.api.schedule;

public class ScheduleNotifyRequest {
    private final String managerName;
    private final String employeeName;
    private final String employeeEmail;
    private final String starskyHomeUrl;
    private final String scheduleDate;
    private final String shifts;

    public ScheduleNotifyRequest(String managerName, String employeeName, String employeeEmail, String starskyHomeUrl, String scheduleDate, String shifts) {
        this.managerName = managerName;
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
        this.starskyHomeUrl = starskyHomeUrl;
        this.scheduleDate = scheduleDate;
        this.shifts = shifts;
    }

    public String getManagerName() {
        return managerName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public String getStarskyHomeUrl() {
        return starskyHomeUrl;
    }

    public String getScheduleDate() {
        return scheduleDate;
    }

    public String getShifts() {
        return shifts;
    }

    @Override
    public String toString() {
        return "ScheduleNotifyRequest{" +
                "managerName='" + managerName + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", employeeEmail='" + employeeEmail + '\'' +
                ", starskyHomeUrl='" + starskyHomeUrl + '\'' +
                ", scheduleDate='" + scheduleDate + '\'' +
                ", shifts='" + shifts + '\'' +
                '}';
    }
}
