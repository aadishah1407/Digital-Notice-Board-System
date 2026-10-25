package com.digitalnoticeboard.notice.dto;

import com.digitalnoticeboard.notice.model.Notice;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class NoticeResponse {

    private Long id;
    private String title;
    private String description;
    private String department;
    private String academicYear;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime publishedAt;

    @JsonFormat(pattern = "yyyy-MM-dd")
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
        response.setExpired(notice.getExpiryDate().isBefore(LocalDate.now()));
        if (notice.getCreator() != null) {
            response.setCreatorUsername(notice.getCreator().getUsername());
        }
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public void setCreatorUsername(String creatorUsername) {
        this.creatorUsername = creatorUsername;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }
}
