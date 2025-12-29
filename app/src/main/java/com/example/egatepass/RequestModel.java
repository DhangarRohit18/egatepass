package com.example.egatepass;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class RequestModel {

    private String requestId;
    private String studentId;
    private String name;
    private String enrollmentNo;
    private String department;
    private String year;
    private String mobile;

    private String approvedBy;
    private long approvedAt;

    // both names present to match documents whether they use "qrData" or "qrCodeData"
    private String qrData;
    private String qrCodeData;

    private String type;
    private String reason;
    private String fromDate;
    private String toDate;
    private String destination;
    private String date;
    private String outTime;
    private String inTime;
    private String status;

    private long createdAt;
    private long updatedAt;

    public RequestModel() {}

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEnrollmentNo() { return enrollmentNo; }
    public void setEnrollmentNo(String enrollmentNo) { this.enrollmentNo = enrollmentNo; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public long getApprovedAt() { return approvedAt; }
    public void setApprovedAt(long approvedAt) { this.approvedAt = approvedAt; }

    public String getQrData() {
        if (qrData != null && !qrData.isEmpty()) return qrData;
        if (qrCodeData != null && !qrCodeData.isEmpty()) return qrCodeData;
        return "";
    }
    public void setQrData(String qrData) { this.qrData = qrData; }

    public String getQrCodeData() {
        if (qrCodeData != null && !qrCodeData.isEmpty()) return qrCodeData;
        if (qrData != null && !qrData.isEmpty()) return qrData;
        return "";
    }
    public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getFromDate() { return fromDate; }
    public void setFromDate(String fromDate) { this.fromDate = fromDate; }

    public String getToDate() { return toDate; }
    public void setToDate(String toDate) { this.toDate = toDate; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getOutTime() { return outTime; }
    public void setOutTime(String outTime) { this.outTime = outTime; }

    public String getInTime() { return inTime; }
    public void setInTime(String inTime) { this.inTime = inTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    // convert fromDate string to millis
    public long getFromDateMillis() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            return sdf.parse(fromDate).getTime();
        } catch (Exception e) {
            return 0;
        }
    }

    // convert toDate string to millis
    public long getToDateMillis() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            return sdf.parse(toDate).getTime();
        } catch (Exception e) {
            return 0;
        }
    }

}
