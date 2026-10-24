package com.digitalnoticeboard.notice.controller;

import com.digitalnoticeboard.auth.model.User;
import com.digitalnoticeboard.auth.repository.UserRepository;
import com.digitalnoticeboard.notice.dto.NoticeResponse;
import com.digitalnoticeboard.notice.dto.StudentNoticeFiltersResponse;
import com.digitalnoticeboard.notice.service.NoticeCreationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentNoticeController {

    private final NoticeCreationService noticeCreationService;
    private final UserRepository userRepository;

    public StudentNoticeController(NoticeCreationService noticeCreationService, UserRepository userRepository) {
        this.noticeCreationService = noticeCreationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/notices")
    public List<NoticeResponse> getNotices(@AuthenticationPrincipal UserDetails userDetails,
                                           @RequestParam(required = false) String department,
                                           @RequestParam(required = false) String academicYear) {
        requireStudent(userDetails);
        return noticeCreationService.getNoticesForStudent(department, academicYear);
    }

    @GetMapping("/notices/filters")
    public StudentNoticeFiltersResponse getFilters(@AuthenticationPrincipal UserDetails userDetails) {
        return noticeCreationService.getStudentNoticeFilters(requireStudent(userDetails));
    }

    private User requireStudent(UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Student login is required");
        }
        User student = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Student account not found"));
        if (!"ROLE_STUDENT".equalsIgnoreCase(student.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access is required");
        }
        return student;
    }
}
