package com.digitalnoticeboard.notice.dto;

import com.digitalnoticeboard.notice.model.Notice;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class NoticeResponse {
    private String id;
    private String title;
    private String description;
    private String department;
    private String academicYear;
    private Instant publishedAt;
    private LocalDate expiryDate;
    private String creatorUsername;
    private boolean expired;

    public NoticeResponse() {
    }

    public static NoticeResponse fromEntity(Notice notice) {
        NoticeResponse response = new NoticeResponse();
        response.setId(notice.getId());
        response.setTitle(notice.getTitle());
        response.setDescription(notice.getDescription());
        response.setDepartment(notice.getDepartment());
        response.setAcademicYear(notice.getAcademicYear());
        response.setPublishedAt(notice.getPublishedAt());
        response.setExpiryDate(notice.getExpiryDate());
        response.setExpired(notice.getExpiryDate().isBefore(LocalDate.now(ZoneId.of("Asia/Kolkata"))));
        response.setCreatorUsername(notice.getCreatorUsername());
        return response;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt == null ? null : publishedAt.atZone(ZoneId.systemDefault()).toInstant();
    }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getCreatorUsername() { return creatorUsername; }
    public void setCreatorUsername(String creatorUsername) { this.creatorUsername = creatorUsername; }
    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }
}
