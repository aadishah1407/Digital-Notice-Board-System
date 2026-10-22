package com.digitalnoticeboard.notice.repository;

import com.digitalnoticeboard.notice.model.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
	List<Notice> findAllByOrderByPublishedAtDesc();

	List<Notice> findByDepartmentIgnoreCaseAndAcademicYearIgnoreCaseAndExpiryDateGreaterThanEqualAndPublishedAtLessThanEqualOrderByPublishedAtDesc(
		String department, String academicYear, LocalDate expiryDate, LocalDateTime publishedAt);
}
