package com.digitalnoticeboard.persistence;

import com.digitalnoticeboard.auth.model.User;
import com.digitalnoticeboard.auth.repository.UserRepository;
import com.digitalnoticeboard.notice.model.Notice;
import com.digitalnoticeboard.notice.repository.NoticeQueryRepository;
import com.digitalnoticeboard.notice.repository.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class MongoPersistenceIntegrationTest {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer(
        DockerImageName.parse("mongo:7.0")
    );

    @DynamicPropertySource
    static void configureMongo(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
        registry.add("spring.data.mongodb.database", () -> "digital_notice_board_test");
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private NoticeQueryRepository noticeQueryRepository;

    @BeforeEach
    void cleanCollections() {
        noticeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void persistsUsersAndNoticesWithStringDocumentIds() {
        User creator = userRepository.save(
            new User("admin", "bcrypt-hash", "ROLE_ADMIN")
        );

        Notice notice = activeNotice(
            "Exam timetable",
            "Computer Science",
            "2026-2027",
            LocalDateTime.now().minusMinutes(5),
            LocalDate.now().plusDays(7)
        );
        notice.setCreatorId(creator.getId());
        notice.setCreatorUsername(creator.getUsername());

        Notice saved = noticeRepository.save(notice);

        assertThat(creator.getId()).isNotBlank();
        assertThat(saved.getId()).isNotBlank();
        assertThat(saved.getCreatorId()).isEqualTo(creator.getId());
        assertThat(noticeRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void uniqueUsernameIndexPreventsDuplicateAccounts() {
        userRepository.save(new User("same-user", "hash-one", "ROLE_STUDENT"));

        assertThatThrownBy(() ->
            userRepository.save(new User("same-user", "hash-two", "ROLE_STUDENT"))
        ).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void studentFiltersMatchDepartmentAndYearAndExcludeExpiredOrUnpublishedNotices() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        noticeRepository.save(activeNotice(
            "CS Exam", "Computer Science", "2026-2027",
            now.minusHours(1), today.plusDays(4)
        ));
        noticeRepository.save(activeNotice(
            "IT Workshop", "Information Technology", "2025-2026",
            now.minusHours(2), today.plusDays(5)
        ));
        noticeRepository.save(activeNotice(
            "Expired CS Notice", "Computer Science", "2026-2027",
            now.minusDays(4), today.minusDays(1)
        ));
        noticeRepository.save(activeNotice(
            "Future CS Notice", "Computer Science", "2026-2027",
            now.plusDays(1), today.plusDays(8)
        ));

        List<Notice> filtered = noticeQueryRepository.findActiveForStudent(
            "computer science", "2026-2027", today, now
        );

        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).getTitle()).isEqualTo("CS Exam");
    }

    @Test
    void filterOptionsContainOnlyActiveNoticesAndAreSorted() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        noticeRepository.save(activeNotice(
            "CS Notice", "Computer Science", "2026-2027",
            now.minusHours(1), today.plusDays(4)
        ));
        noticeRepository.save(activeNotice(
            "IT Notice", "Information Technology", "2025-2026",
            now.minusHours(2), today.plusDays(5)
        ));
        noticeRepository.save(activeNotice(
            "Expired Notice", "Arts", "2024-2025",
            now.minusDays(3), today.minusDays(1)
        ));

        assertThat(noticeQueryRepository.findActiveDepartments(today, now))
            .containsExactly("Computer Science", "Information Technology");
        assertThat(noticeQueryRepository.findActiveAcademicYears(today, now))
            .containsExactly("2025-2026", "2026-2027");
    }

    private Notice activeNotice(String title, String department, String academicYear,
                                LocalDateTime publishedAt, LocalDate expiryDate) {
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setDescription(title + " details");
        notice.setDepartment(department);
        notice.setAcademicYear(academicYear);
        notice.setPublishedAt(publishedAt);
        notice.setExpiryDate(expiryDate);
        return notice;
    }
}
