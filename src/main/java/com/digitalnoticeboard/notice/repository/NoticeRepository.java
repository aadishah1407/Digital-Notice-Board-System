package com.digitalnoticeboard.notice.repository;

import com.digitalnoticeboard.notice.model.Notice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
	List<Notice> findAllByOrderByPublishedAtDesc();

	@Query("""
		SELECT n FROM Notice n
		WHERE n.expiryDate >= :today
		  AND n.publishedAt <= :now
		  AND (:department IS NULL OR LOWER(n.department) = LOWER(:department))
		  AND (:academicYear IS NULL OR LOWER(n.academicYear) = LOWER(:academicYear))
		ORDER BY n.publishedAt DESC
		""")
	List<Notice> findActiveForStudent(@Param("department") String department,
	                                 @Param("academicYear") String academicYear,
	                                 @Param("today") LocalDate today,
	                                 @Param("now") LocalDateTime now);

	@Query("SELECT DISTINCT n.department FROM Notice n WHERE n.expiryDate >= :today AND n.publishedAt <= :now ORDER BY n.department")
	List<String> findActiveDepartments(@Param("today") LocalDate today, @Param("now") LocalDateTime now);

	@Query("SELECT DISTINCT n.academicYear FROM Notice n WHERE n.expiryDate >= :today AND n.publishedAt <= :now ORDER BY n.academicYear")
	List<String> findActiveAcademicYears(@Param("today") LocalDate today, @Param("now") LocalDateTime now);
}
