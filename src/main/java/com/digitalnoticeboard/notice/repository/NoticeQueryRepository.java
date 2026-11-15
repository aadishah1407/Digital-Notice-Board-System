package com.digitalnoticeboard.notice.repository;

import com.digitalnoticeboard.notice.model.Notice;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.regex.Pattern;

@Repository
public class NoticeQueryRepository {
    private static final ZoneId APP_TIME_ZONE = ZoneId.of("Asia/Kolkata");
    private final MongoTemplate mongoTemplate;

    public NoticeQueryRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Notice> findActiveForStudent(String department, String academicYear,
                                             LocalDate today, LocalDateTime now) {
        Query query = activeNoticesQuery(today, now);
        if (StringUtils.hasText(department)) {
            query.addCriteria(Criteria.where("department")
                .regex("^" + Pattern.quote(department.trim()) + "$", "i"));
        }
        if (StringUtils.hasText(academicYear)) {
            query.addCriteria(Criteria.where("academicYear")
                .regex("^" + Pattern.quote(academicYear.trim()) + "$", "i"));
        }
        query.with(Sort.by(Sort.Direction.DESC, "publishedAt"));
        return mongoTemplate.find(query, Notice.class);
    }

    public List<String> findActiveDepartments(LocalDate today, LocalDateTime now) {
        List<String> values = mongoTemplate.findDistinct(
            activeNoticesQuery(today, now), "department", Notice.class, String.class);
        values.sort(String.CASE_INSENSITIVE_ORDER);
        return values;
    }

    public List<String> findActiveAcademicYears(LocalDate today, LocalDateTime now) {
        List<String> values = mongoTemplate.findDistinct(
            activeNoticesQuery(today, now), "academicYear", Notice.class, String.class);
        values.sort(String.CASE_INSENSITIVE_ORDER);
        return values;
    }

    private Query activeNoticesQuery(LocalDate today, LocalDateTime now) {
        Instant publicationCutoff = now.atZone(APP_TIME_ZONE).toInstant();
        Criteria criteria = Criteria.where("expiryDate").gte(today)
            .and("publishedAt").lte(publicationCutoff);
        return new Query(criteria);
    }
}
