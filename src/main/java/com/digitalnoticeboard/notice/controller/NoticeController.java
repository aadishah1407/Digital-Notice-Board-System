package com.digitalnoticeboard.notice.controller;

import com.digitalnoticeboard.notice.dto.NoticeRequest;
import com.digitalnoticeboard.notice.dto.NoticeResponse;
import com.digitalnoticeboard.notice.service.NoticeCreationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class NoticeController {

    private final NoticeCreationService noticeCreationService;

    public NoticeController(NoticeCreationService noticeCreationService) {
        this.noticeCreationService = noticeCreationService;
    }

    @GetMapping("/notices")
    public List<NoticeResponse> getNotices() {
        return noticeCreationService.getNotices();
    }

    @PostMapping("/notices")
    public ResponseEntity<NoticeResponse> createNotice(@Valid @RequestBody NoticeRequest request,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        NoticeResponse response = noticeCreationService.createNotice(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/notices/{id}")
    public NoticeResponse updateNotice(@PathVariable Long id, @Valid @RequestBody NoticeRequest request) {
        return noticeCreationService.updateNotice(id, request);
    }

    @DeleteMapping("/notices/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        noticeCreationService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }
}
