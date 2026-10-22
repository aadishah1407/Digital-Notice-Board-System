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
import java.util.List;
import com.digitalnoticeboard.auth.model.User;

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

        LocalDateTime publishedAt = request.getPublishedAt() == null ? LocalDateTime.now() : request.getPublishedAt();
        validateExpiryDate(expiryDate, publishedAt);

        User creator = userRepository.findByUsername(creatorUsername)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated admin not found"));

        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setDescription(description);
        notice.setDepartment(department);
        notice.setAcademicYear(academicYear);
        notice.setPublishedAt(publishedAt);
        notice.setExpiryDate(expiryDate);
        notice.setCreator(creator);

        Notice savedNotice = noticeRepository.save(notice);
        return NoticeResponse.fromEntity(savedNotice);
    }

    public List<NoticeResponse> getNotices() {
        return noticeRepository.findAllByOrderByPublishedAtDesc().stream()
            .map(NoticeResponse::fromEntity)
            .toList();
    }

    public List<NoticeResponse> getNoticesForStudent(User student) {
        if (student == null || student.getDepartment() == null || student.getAcademicYear() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student department and academic year are required");
        }
        return noticeRepository
            .findByDepartmentIgnoreCaseAndAcademicYearIgnoreCaseAndExpiryDateGreaterThanEqualAndPublishedAtLessThanEqualOrderByPublishedAtDesc(
                student.getDepartment(), student.getAcademicYear(), LocalDate.now(), LocalDateTime.now())
            .stream()
            .map(NoticeResponse::fromEntity)
            .toList();
    }

    public NoticeResponse updateNotice(Long id, NoticeRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Notice payload is required");
        }

        Notice notice = noticeRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notice not found."));

        String title = trimToNull(request.getTitle());
        String description = trimToNull(request.getDescription());
        String department = trimToNull(request.getDepartment());
        String academicYear = trimToNull(request.getAcademicYear());
        LocalDateTime publishedAt = request.getPublishedAt() == null ? notice.getPublishedAt() : request.getPublishedAt();
        LocalDate expiryDate = request.getExpiryDate();

        if (title == null || description == null || department == null || academicYear == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title, description, department and academic year are required");
        }
        if (expiryDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expiry date is required");
        }

        validateExpiryDate(expiryDate, publishedAt);

        notice.setTitle(title);
        notice.setDescription(description);
        notice.setDepartment(department);
        notice.setAcademicYear(academicYear);
        notice.setPublishedAt(publishedAt);
        notice.setExpiryDate(expiryDate);

        return NoticeResponse.fromEntity(noticeRepository.save(notice));
    }

    public void deleteNotice(Long id) {
        if (!noticeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notice not found.");
        }
        noticeRepository.deleteById(id);
    }

    private void validateExpiryDate(LocalDate expiryDate, LocalDateTime publishedAt) {
        if (!expiryDate.isAfter(publishedAt.toLocalDate())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Expiry date must be later than the publication date."
            );
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
