package com.digitalnoticeboard.notice.repository;

import com.digitalnoticeboard.notice.model.Notice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends MongoRepository<Notice, String> {
    List<Notice> findAllByOrderByPublishedAtDesc();
}
