package com.example.mall.interaction;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TopicService {
    private final JdbcClient jdbc;

    public TopicService(JdbcClient jdbc) { this.jdbc = jdbc; }

    public TopicView create(long creatorId, String title) {
        jdbc.sql("INSERT INTO community_topic(creator_id, title) VALUES (:creatorId, :title)")
                .param("creatorId", creatorId).param("title", title).update();
        return jdbc.sql("SELECT topic_id, creator_id, title FROM community_topic ORDER BY topic_id DESC LIMIT 1")
                .query(TopicView.class).single();
    }

    public List<TopicView> list() {
        return jdbc.sql("SELECT topic_id, creator_id, title FROM community_topic ORDER BY topic_id")
                .query(TopicView.class).list();
    }

    public TopicView find(long id) {
        return jdbc.sql("SELECT topic_id, creator_id, title FROM community_topic WHERE topic_id=:id")
                .param("id", id).query(TopicView.class).optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "topic not found"));
    }

    public record TopicView(long topicId, long creatorId, String title) {}
}
