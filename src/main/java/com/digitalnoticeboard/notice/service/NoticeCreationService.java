package com.digitalnoticeboard.notice.service;

import com.digitalnoticeboard.auth.model.User;
import com.digitalnoticeboard.auth.repository.UserRepository;
import com.digitalnoticeboard.notice.dto.NoticeRequest;
import com.digitalnoticeboard.notice.dto.NoticeResponse;
import com.digitalnoticeboard.notice.model.Notice;
import com.digitalnoticeboard.notice.repository.NoticeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class NoticeCreationService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    public NoticeCreationService(NoticeRepository noticeRepository, UserRepository userRepository) {
        this.noticeRepository = noticeRepository;
        this.userRepository = userRepository;
    }

    public NoticeResponse createNotice(NoticeRequest request, String creatorUsername) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Notice payload is required");
        }

        String title = trimToNull(request.getTitle());
        String description = trimToNull(request.getDescription());
        String department = trimToNull(request.getDepartment());
        String academicYear = trimToNull(request.getAcademicYear());
        LocalDate expiryDate = request.getExpiryDate();

        if (title == null || description == null || department == null || academicYear == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title, description, department and academic year are required");
        }

        if (expiryDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expiry date is required");
        }

        User creator = userRepository.findByUsername(creatorUsername)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated admin not found"));

        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setDescription(description);
        notice.setDepartment(department);
        notice.setAcademicYear(academicYear);
        notice.setPublishedAt(request.getPublishedAt() == null ? LocalDateTime.now() : request.getPublishedAt());
        notice.setExpiryDate(expiryDate);
        notice.setCreator(creator);

        Notice savedNotice = noticeRepository.save(notice);
        return NoticeResponse.fromEntity(savedNotice);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
